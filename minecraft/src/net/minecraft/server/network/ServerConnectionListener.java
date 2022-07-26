package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.RateKickingConnection;
import net.minecraft.network.Varint21FrameDecoder;
import net.minecraft.network.Varint21LengthFieldPrepender;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.LazyLoadedValue;
import org.slf4j.Logger;

public class ServerConnectionListener {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final LazyLoadedValue<NioEventLoopGroup> SERVER_EVENT_GROUP = new LazyLoadedValue<>(
		() -> new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Server IO #%d").setDaemon(true).build())
	);
	public static final LazyLoadedValue<EpollEventLoopGroup> SERVER_EPOLL_EVENT_GROUP = new LazyLoadedValue<>(
		() -> new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Epoll Server IO #%d").setDaemon(true).build())
	);
	final MinecraftServer server;
	public volatile boolean running;
	private final List<ChannelFuture> channels = Collections.synchronizedList(Lists.newArrayList());
	final List<Connection> connections = Collections.synchronizedList(Lists.newArrayList());

	public ServerConnectionListener(MinecraftServer minecraftServer) {
		this.server = minecraftServer;
		this.running = true;
	}

	public void startTcpServerListener(@Nullable InetAddress inetAddress, int i) throws IOException {
		synchronized (this.channels) {
			Class<? extends ServerSocketChannel> class_;
			LazyLoadedValue<? extends EventLoopGroup> lazyLoadedValue;
			if (Epoll.isAvailable() && this.server.isEpollEnabled()) {
				class_ = EpollServerSocketChannel.class;
				lazyLoadedValue = SERVER_EPOLL_EVENT_GROUP;
				LOGGER.info("Using epoll channel type");
			} else {
				class_ = NioServerSocketChannel.class;
				lazyLoadedValue = SERVER_EVENT_GROUP;
				LOGGER.info("Using default channel type");
			}

			this.channels
				.add(
					new ServerBootstrap()
						.channel(class_)
						.childHandler(
							new ChannelInitializer<Channel>() {
								@Override
								protected void initChannel(Channel channel) {
									try {
										channel.config().setOption(ChannelOption.TCP_NODELAY, true);
									} catch (ChannelException var4) {
									}

									channel.pipeline()
										.addLast("timeout", new ReadTimeoutHandler(30))
										.addLast("legacy_query", new LegacyQueryHandler(ServerConnectionListener.this))
										.addLast("splitter", new Varint21FrameDecoder())
										.addLast("decoder", new PacketDecoder(PacketFlow.SERVERBOUND))
										.addLast("prepender", new Varint21LengthFieldPrepender())
										.addLast("encoder", new PacketEncoder(PacketFlow.CLIENTBOUND));
									int i = ServerConnectionListener.this.server.getRateLimitPacketsPerSecond();
									Connection connection = (Connection)(i > 0 ? new RateKickingConnection(i) : new Connection(PacketFlow.SERVERBOUND));
									ServerConnectionListener.this.connections.add(connection);
									channel.pipeline().addLast("packet_handler", connection);
									connection.setListener(new ServerHandshakePacketListenerImpl(ServerConnectionListener.this.server, connection));
								}
							}
						)
						.group(lazyLoadedValue.get())
						.localAddress(inetAddress, i)
						.bind()
						.syncUninterruptibly()
				);
		}
	}

	public SocketAddress startMemoryChannel() {
		ChannelFuture channelFuture;
		synchronized (this.channels) {
			channelFuture = new ServerBootstrap().channel(LocalServerChannel.class).childHandler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel channel) {
					Connection connection = new Connection(PacketFlow.SERVERBOUND);
					connection.setListener(new MemoryServerHandshakePacketListenerImpl(ServerConnectionListener.this.server, connection));
					ServerConnectionListener.this.connections.add(connection);
					channel.pipeline().addLast("packet_handler", connection);
				}
			}).group(SERVER_EVENT_GROUP.get()).localAddress(LocalAddress.ANY).bind().syncUninterruptibly();
			this.channels.add(channelFuture);
		}

		return channelFuture.channel().localAddress();
	}

	public void stop() {
		this.running = false;

		for (ChannelFuture channelFuture : this.channels) {
			try {
				channelFuture.channel().close().sync();
			} catch (InterruptedException var4) {
				LOGGER.error("Interrupted whilst closing channel");
			}
		}
	}

	public void tick() {
		synchronized (this.connections) {
			Iterator<Connection> iterator = this.connections.iterator();

			while (iterator.hasNext()) {
				Connection connection = (Connection)iterator.next();
				if (!connection.isConnecting()) {
					if (connection.isConnected()) {
						try {
							connection.tick();
						} catch (Exception var7) {
							if (connection.isMemoryConnection()) {
								throw new ReportedException(CrashReport.forThrowable(var7, "Ticking memory connection"));
							}

							LOGGER.warn("Failed to handle packet for {}", connection.getRemoteAddress(), var7);
							Component component = Component.literal("Internal server error");
							connection.send(new ClientboundDisconnectPacket(component), PacketSendListener.thenRun(() -> connection.disconnect(component)));
							connection.setReadOnly();
						}
					} else {
						iterator.remove();
						connection.handleDisconnection();
					}
				}
			}
		}
	}

	public MinecraftServer getServer() {
		return this.server;
	}

	public List<Connection> getConnections() {
		return this.connections;
	}

	static class LatencySimulator extends ChannelInboundHandlerAdapter {
		private static final Timer TIMER = new HashedWheelTimer();
		private final int delay;
		private final int jitter;
		private final List<ServerConnectionListener.LatencySimulator.DelayedMessage> queuedMessages = Lists.<ServerConnectionListener.LatencySimulator.DelayedMessage>newArrayList();

		public LatencySimulator(int i, int j) {
			this.delay = i;
			this.jitter = j;
		}

		@Override
		public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) {
			this.delayDownstream(channelHandlerContext, object);
		}

		private void delayDownstream(ChannelHandlerContext channelHandlerContext, Object object) {
			int i = this.delay + (int)(Math.random() * (double)this.jitter);
			this.queuedMessages.add(new ServerConnectionListener.LatencySimulator.DelayedMessage(channelHandlerContext, object));
			TIMER.newTimeout(this::onTimeout, (long)i, TimeUnit.MILLISECONDS);
		}

		private void onTimeout(Timeout timeout) {
			ServerConnectionListener.LatencySimulator.DelayedMessage delayedMessage = (ServerConnectionListener.LatencySimulator.DelayedMessage)this.queuedMessages
				.remove(0);
			delayedMessage.ctx.fireChannelRead(delayedMessage.msg);
		}

		static class DelayedMessage {
			public final ChannelHandlerContext ctx;
			public final Object msg;

			public DelayedMessage(ChannelHandlerContext channelHandlerContext, Object object) {
				this.ctx = channelHandlerContext;
				this.msg = object;
			}
		}
	}
}
