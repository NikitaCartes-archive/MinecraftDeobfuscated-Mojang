package net.minecraft.network;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Queue;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class Connection extends SimpleChannelInboundHandler<Packet<?>> {
	private static final float AVERAGE_PACKETS_SMOOTHING = 0.75F;
	private static final Logger LOGGER = LogManager.getLogger();
	public static final Marker ROOT_MARKER = MarkerManager.getMarker("NETWORK");
	public static final Marker PACKET_MARKER = MarkerManager.getMarker("NETWORK_PACKETS", ROOT_MARKER);
	public static final AttributeKey<ConnectionProtocol> ATTRIBUTE_PROTOCOL = AttributeKey.valueOf("protocol");
	public static final LazyLoadedValue<NioEventLoopGroup> NETWORK_WORKER_GROUP = new LazyLoadedValue<>(
		() -> new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Client IO #%d").setDaemon(true).build())
	);
	public static final LazyLoadedValue<EpollEventLoopGroup> NETWORK_EPOLL_WORKER_GROUP = new LazyLoadedValue<>(
		() -> new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build())
	);
	public static final LazyLoadedValue<DefaultEventLoopGroup> LOCAL_WORKER_GROUP = new LazyLoadedValue<>(
		() -> new DefaultEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Local Client IO #%d").setDaemon(true).build())
	);
	private final PacketFlow receiving;
	private final Queue<Connection.PacketHolder> queue = Queues.<Connection.PacketHolder>newConcurrentLinkedQueue();
	private Channel channel;
	private SocketAddress address;
	private PacketListener packetListener;
	private Component disconnectedReason;
	private boolean encrypted;
	private boolean disconnectionHandled;
	private int receivedPackets;
	private int sentPackets;
	private float averageReceivedPackets;
	private float averageSentPackets;
	private int tickCount;
	private boolean handlingFault;

	public Connection(PacketFlow packetFlow) {
		this.receiving = packetFlow;
	}

	@Override
	public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
		super.channelActive(channelHandlerContext);
		this.channel = channelHandlerContext.channel();
		this.address = this.channel.remoteAddress();

		try {
			this.setProtocol(ConnectionProtocol.HANDSHAKING);
		} catch (Throwable var3) {
			LOGGER.fatal(var3);
		}
	}

	public void setProtocol(ConnectionProtocol connectionProtocol) {
		this.channel.attr(ATTRIBUTE_PROTOCOL).set(connectionProtocol);
		this.channel.config().setAutoRead(true);
		LOGGER.debug("Enabled auto read");
	}

	@Override
	public void channelInactive(ChannelHandlerContext channelHandlerContext) {
		this.disconnect(new TranslatableComponent("disconnect.endOfStream"));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {
		if (throwable instanceof SkipPacketException) {
			LOGGER.debug("Skipping packet due to errors", throwable.getCause());
		} else {
			boolean bl = !this.handlingFault;
			this.handlingFault = true;
			if (this.channel.isOpen()) {
				if (throwable instanceof TimeoutException) {
					LOGGER.debug("Timeout", throwable);
					this.disconnect(new TranslatableComponent("disconnect.timeout"));
				} else {
					Component component = new TranslatableComponent("disconnect.genericReason", "Internal Exception: " + throwable);
					if (bl) {
						LOGGER.debug("Failed to sent packet", throwable);
						ConnectionProtocol connectionProtocol = this.getCurrentProtocol();
						Packet<?> packet = (Packet<?>)(connectionProtocol == ConnectionProtocol.LOGIN
							? new ClientboundLoginDisconnectPacket(component)
							: new ClientboundDisconnectPacket(component));
						this.send(packet, future -> this.disconnect(component));
						this.setReadOnly();
					} else {
						LOGGER.debug("Double fault", throwable);
						this.disconnect(component);
					}
				}
			}
		}
	}

	protected void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet) {
		if (this.channel.isOpen()) {
			try {
				genericsFtw(packet, this.packetListener);
			} catch (RunningOnDifferentThreadException var4) {
			} catch (ClassCastException var5) {
				LOGGER.error("Received {} that couldn't be processed", packet.getClass(), var5);
				this.disconnect(new TranslatableComponent("multiplayer.disconnect.invalid_packet"));
			}

			this.receivedPackets++;
		}
	}

	private static <T extends PacketListener> void genericsFtw(Packet<T> packet, PacketListener packetListener) {
		packet.handle((T)packetListener);
	}

	public void setListener(PacketListener packetListener) {
		Validate.notNull(packetListener, "packetListener");
		this.packetListener = packetListener;
	}

	public void send(Packet<?> packet) {
		this.send(packet, null);
	}

	public void send(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> genericFutureListener) {
		if (this.isConnected()) {
			this.flushQueue();
			this.sendPacket(packet, genericFutureListener);
		} else {
			this.queue.add(new Connection.PacketHolder(packet, genericFutureListener));
		}
	}

	private void sendPacket(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> genericFutureListener) {
		ConnectionProtocol connectionProtocol = ConnectionProtocol.getProtocolForPacket(packet);
		ConnectionProtocol connectionProtocol2 = this.getCurrentProtocol();
		this.sentPackets++;
		if (connectionProtocol2 != connectionProtocol) {
			LOGGER.debug("Disabled auto read");
			this.channel.config().setAutoRead(false);
		}

		if (this.channel.eventLoop().inEventLoop()) {
			this.doSendPacket(packet, genericFutureListener, connectionProtocol, connectionProtocol2);
		} else {
			this.channel.eventLoop().execute(() -> this.doSendPacket(packet, genericFutureListener, connectionProtocol, connectionProtocol2));
		}
	}

	private void doSendPacket(
		Packet<?> packet,
		@Nullable GenericFutureListener<? extends Future<? super Void>> genericFutureListener,
		ConnectionProtocol connectionProtocol,
		ConnectionProtocol connectionProtocol2
	) {
		if (connectionProtocol != connectionProtocol2) {
			this.setProtocol(connectionProtocol);
		}

		ChannelFuture channelFuture = this.channel.writeAndFlush(packet);
		if (genericFutureListener != null) {
			channelFuture.addListener(genericFutureListener);
		}

		channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}

	private ConnectionProtocol getCurrentProtocol() {
		return this.channel.attr(ATTRIBUTE_PROTOCOL).get();
	}

	private void flushQueue() {
		if (this.channel != null && this.channel.isOpen()) {
			synchronized (this.queue) {
				Connection.PacketHolder packetHolder;
				while ((packetHolder = (Connection.PacketHolder)this.queue.poll()) != null) {
					this.sendPacket(packetHolder.packet, packetHolder.listener);
				}
			}
		}
	}

	public void tick() {
		this.flushQueue();
		if (this.packetListener instanceof ServerLoginPacketListenerImpl) {
			((ServerLoginPacketListenerImpl)this.packetListener).tick();
		}

		if (this.packetListener instanceof ServerGamePacketListenerImpl) {
			((ServerGamePacketListenerImpl)this.packetListener).tick();
		}

		if (!this.isConnected() && !this.disconnectionHandled) {
			this.handleDisconnection();
		}

		if (this.channel != null) {
			this.channel.flush();
		}

		if (this.tickCount++ % 20 == 0) {
			this.tickSecond();
		}
	}

	protected void tickSecond() {
		this.averageSentPackets = Mth.lerp(0.75F, (float)this.sentPackets, this.averageSentPackets);
		this.averageReceivedPackets = Mth.lerp(0.75F, (float)this.receivedPackets, this.averageReceivedPackets);
		this.sentPackets = 0;
		this.receivedPackets = 0;
	}

	public SocketAddress getRemoteAddress() {
		return this.address;
	}

	public void disconnect(Component component) {
		if (this.channel.isOpen()) {
			this.channel.close().awaitUninterruptibly();
			this.disconnectedReason = component;
		}
	}

	public boolean isMemoryConnection() {
		return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
	}

	public PacketFlow getReceiving() {
		return this.receiving;
	}

	public PacketFlow getSending() {
		return this.receiving.getOpposite();
	}

	public static Connection connectToServer(InetSocketAddress inetSocketAddress, boolean bl) {
		final Connection connection = new Connection(PacketFlow.CLIENTBOUND);
		Class<? extends SocketChannel> class_;
		LazyLoadedValue<? extends EventLoopGroup> lazyLoadedValue;
		if (Epoll.isAvailable() && bl) {
			class_ = EpollSocketChannel.class;
			lazyLoadedValue = NETWORK_EPOLL_WORKER_GROUP;
		} else {
			class_ = NioSocketChannel.class;
			lazyLoadedValue = NETWORK_WORKER_GROUP;
		}

		new Bootstrap()
			.group(lazyLoadedValue.get())
			.handler(
				new ChannelInitializer<Channel>() {
					@Override
					protected void initChannel(Channel channel) {
						try {
							channel.config().setOption(ChannelOption.TCP_NODELAY, true);
						} catch (ChannelException var3) {
						}

						channel.pipeline()
							.addLast("timeout", new ReadTimeoutHandler(30))
							.addLast("splitter", new Varint21FrameDecoder())
							.addLast("decoder", new PacketDecoder(PacketFlow.CLIENTBOUND))
							.addLast("prepender", new Varint21LengthFieldPrepender())
							.addLast("encoder", new PacketEncoder(PacketFlow.SERVERBOUND))
							.addLast("packet_handler", connection);
					}
				}
			)
			.channel(class_)
			.connect(inetSocketAddress.getAddress(), inetSocketAddress.getPort())
			.syncUninterruptibly();
		return connection;
	}

	public static Connection connectToLocalServer(SocketAddress socketAddress) {
		final Connection connection = new Connection(PacketFlow.CLIENTBOUND);
		new Bootstrap().group(LOCAL_WORKER_GROUP.get()).handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel channel) {
				channel.pipeline().addLast("packet_handler", connection);
			}
		}).channel(LocalChannel.class).connect(socketAddress).syncUninterruptibly();
		return connection;
	}

	public void setEncryptionKey(Cipher cipher, Cipher cipher2) {
		this.encrypted = true;
		this.channel.pipeline().addBefore("splitter", "decrypt", new CipherDecoder(cipher));
		this.channel.pipeline().addBefore("prepender", "encrypt", new CipherEncoder(cipher2));
	}

	public boolean isEncrypted() {
		return this.encrypted;
	}

	public boolean isConnected() {
		return this.channel != null && this.channel.isOpen();
	}

	public boolean isConnecting() {
		return this.channel == null;
	}

	public PacketListener getPacketListener() {
		return this.packetListener;
	}

	@Nullable
	public Component getDisconnectedReason() {
		return this.disconnectedReason;
	}

	public void setReadOnly() {
		this.channel.config().setAutoRead(false);
	}

	public void setupCompression(int i, boolean bl) {
		if (i >= 0) {
			if (this.channel.pipeline().get("decompress") instanceof CompressionDecoder) {
				((CompressionDecoder)this.channel.pipeline().get("decompress")).setThreshold(i, bl);
			} else {
				this.channel.pipeline().addBefore("decoder", "decompress", new CompressionDecoder(i, bl));
			}

			if (this.channel.pipeline().get("compress") instanceof CompressionEncoder) {
				((CompressionEncoder)this.channel.pipeline().get("compress")).setThreshold(i);
			} else {
				this.channel.pipeline().addBefore("encoder", "compress", new CompressionEncoder(i));
			}
		} else {
			if (this.channel.pipeline().get("decompress") instanceof CompressionDecoder) {
				this.channel.pipeline().remove("decompress");
			}

			if (this.channel.pipeline().get("compress") instanceof CompressionEncoder) {
				this.channel.pipeline().remove("compress");
			}
		}
	}

	public void handleDisconnection() {
		if (this.channel != null && !this.channel.isOpen()) {
			if (this.disconnectionHandled) {
				LOGGER.warn("handleDisconnection() called twice");
			} else {
				this.disconnectionHandled = true;
				if (this.getDisconnectedReason() != null) {
					this.getPacketListener().onDisconnect(this.getDisconnectedReason());
				} else if (this.getPacketListener() != null) {
					this.getPacketListener().onDisconnect(new TranslatableComponent("multiplayer.disconnect.generic"));
				}
			}
		}
	}

	public float getAverageReceivedPackets() {
		return this.averageReceivedPackets;
	}

	public float getAverageSentPackets() {
		return this.averageSentPackets;
	}

	static class PacketHolder {
		final Packet<?> packet;
		@Nullable
		final GenericFutureListener<? extends Future<? super Void>> listener;

		public PacketHolder(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> genericFutureListener) {
			this.packet = packet;
			this.listener = genericFutureListener;
		}
	}
}
