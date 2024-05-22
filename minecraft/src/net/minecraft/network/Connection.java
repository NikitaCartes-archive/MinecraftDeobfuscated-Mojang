package net.minecraft.network;

import com.google.common.base.Suppliers;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
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
import io.netty.handler.flow.FlowControlHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.HandshakeProtocols;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.network.protocol.status.StatusProtocols;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.util.Mth;
import net.minecraft.util.debugchart.LocalSampleLogger;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Connection extends SimpleChannelInboundHandler<Packet<?>> {
	private static final float AVERAGE_PACKETS_SMOOTHING = 0.75F;
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Marker ROOT_MARKER = MarkerFactory.getMarker("NETWORK");
	public static final Marker PACKET_MARKER = Util.make(MarkerFactory.getMarker("NETWORK_PACKETS"), marker -> marker.add(ROOT_MARKER));
	public static final Marker PACKET_RECEIVED_MARKER = Util.make(MarkerFactory.getMarker("PACKET_RECEIVED"), marker -> marker.add(PACKET_MARKER));
	public static final Marker PACKET_SENT_MARKER = Util.make(MarkerFactory.getMarker("PACKET_SENT"), marker -> marker.add(PACKET_MARKER));
	public static final Supplier<NioEventLoopGroup> NETWORK_WORKER_GROUP = Suppliers.memoize(
		() -> new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Client IO #%d").setDaemon(true).build())
	);
	public static final Supplier<EpollEventLoopGroup> NETWORK_EPOLL_WORKER_GROUP = Suppliers.memoize(
		() -> new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build())
	);
	public static final Supplier<DefaultEventLoopGroup> LOCAL_WORKER_GROUP = Suppliers.memoize(
		() -> new DefaultEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Local Client IO #%d").setDaemon(true).build())
	);
	private static final ProtocolInfo<ServerHandshakePacketListener> INITIAL_PROTOCOL = HandshakeProtocols.SERVERBOUND;
	private final PacketFlow receiving;
	private volatile boolean sendLoginDisconnect = true;
	private final Queue<Consumer<Connection>> pendingActions = Queues.<Consumer<Connection>>newConcurrentLinkedQueue();
	private Channel channel;
	private SocketAddress address;
	@Nullable
	private volatile PacketListener disconnectListener;
	@Nullable
	private volatile PacketListener packetListener;
	@Nullable
	private DisconnectionDetails disconnectionDetails;
	private boolean encrypted;
	private boolean disconnectionHandled;
	private int receivedPackets;
	private int sentPackets;
	private float averageReceivedPackets;
	private float averageSentPackets;
	private int tickCount;
	private boolean handlingFault;
	@Nullable
	private volatile DisconnectionDetails delayedDisconnect;
	@Nullable
	BandwidthDebugMonitor bandwidthDebugMonitor;

	public Connection(PacketFlow packetFlow) {
		this.receiving = packetFlow;
	}

	@Override
	public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
		super.channelActive(channelHandlerContext);
		this.channel = channelHandlerContext.channel();
		this.address = this.channel.remoteAddress();
		if (this.delayedDisconnect != null) {
			this.disconnect(this.delayedDisconnect);
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext channelHandlerContext) {
		this.disconnect(Component.translatable("disconnect.endOfStream"));
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
					this.disconnect(Component.translatable("disconnect.timeout"));
				} else {
					Component component = Component.translatable("disconnect.genericReason", "Internal Exception: " + throwable);
					PacketListener packetListener = this.packetListener;
					DisconnectionDetails disconnectionDetails;
					if (packetListener != null) {
						disconnectionDetails = packetListener.createDisconnectionInfo(component, throwable);
					} else {
						disconnectionDetails = new DisconnectionDetails(component);
					}

					if (bl) {
						LOGGER.debug("Failed to sent packet", throwable);
						if (this.getSending() == PacketFlow.CLIENTBOUND) {
							Packet<?> packet = (Packet<?>)(this.sendLoginDisconnect ? new ClientboundLoginDisconnectPacket(component) : new ClientboundDisconnectPacket(component));
							this.send(packet, PacketSendListener.thenRun(() -> this.disconnect(disconnectionDetails)));
						} else {
							this.disconnect(disconnectionDetails);
						}

						this.setReadOnly();
					} else {
						LOGGER.debug("Double fault", throwable);
						this.disconnect(disconnectionDetails);
					}
				}
			}
		}
	}

	protected void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet) {
		if (this.channel.isOpen()) {
			PacketListener packetListener = this.packetListener;
			if (packetListener == null) {
				throw new IllegalStateException("Received a packet before the packet listener was initialized");
			} else {
				if (packetListener.shouldHandleMessage(packet)) {
					try {
						genericsFtw(packet, packetListener);
					} catch (RunningOnDifferentThreadException var5) {
					} catch (RejectedExecutionException var6) {
						this.disconnect(Component.translatable("multiplayer.disconnect.server_shutdown"));
					} catch (ClassCastException var7) {
						LOGGER.error("Received {} that couldn't be processed", packet.getClass(), var7);
						this.disconnect(Component.translatable("multiplayer.disconnect.invalid_packet"));
					}

					this.receivedPackets++;
				}
			}
		}
	}

	private static <T extends PacketListener> void genericsFtw(Packet<T> packet, PacketListener packetListener) {
		packet.handle((T)packetListener);
	}

	private void validateListener(ProtocolInfo<?> protocolInfo, PacketListener packetListener) {
		Validate.notNull(packetListener, "packetListener");
		PacketFlow packetFlow = packetListener.flow();
		if (packetFlow != this.receiving) {
			throw new IllegalStateException("Trying to set listener for wrong side: connection is " + this.receiving + ", but listener is " + packetFlow);
		} else {
			ConnectionProtocol connectionProtocol = packetListener.protocol();
			if (protocolInfo.id() != connectionProtocol) {
				throw new IllegalStateException("Listener protocol (" + connectionProtocol + ") does not match requested one " + protocolInfo);
			}
		}
	}

	private static void syncAfterConfigurationChange(ChannelFuture channelFuture) {
		try {
			channelFuture.syncUninterruptibly();
		} catch (Exception var2) {
			if (var2 instanceof ClosedChannelException) {
				LOGGER.info("Connection closed during protocol change");
			} else {
				throw var2;
			}
		}
	}

	public <T extends PacketListener> void setupInboundProtocol(ProtocolInfo<T> protocolInfo, T packetListener) {
		this.validateListener(protocolInfo, packetListener);
		if (protocolInfo.flow() != this.getReceiving()) {
			throw new IllegalStateException("Invalid inbound protocol: " + protocolInfo.id());
		} else {
			this.packetListener = packetListener;
			this.disconnectListener = null;
			UnconfiguredPipelineHandler.InboundConfigurationTask inboundConfigurationTask = UnconfiguredPipelineHandler.setupInboundProtocol(protocolInfo);
			BundlerInfo bundlerInfo = protocolInfo.bundlerInfo();
			if (bundlerInfo != null) {
				PacketBundlePacker packetBundlePacker = new PacketBundlePacker(bundlerInfo);
				inboundConfigurationTask = inboundConfigurationTask.andThen(
					channelHandlerContext -> channelHandlerContext.pipeline().addAfter("decoder", "bundler", packetBundlePacker)
				);
			}

			syncAfterConfigurationChange(this.channel.writeAndFlush(inboundConfigurationTask));
		}
	}

	public void setupOutboundProtocol(ProtocolInfo<?> protocolInfo) {
		if (protocolInfo.flow() != this.getSending()) {
			throw new IllegalStateException("Invalid outbound protocol: " + protocolInfo.id());
		} else {
			UnconfiguredPipelineHandler.OutboundConfigurationTask outboundConfigurationTask = UnconfiguredPipelineHandler.setupOutboundProtocol(protocolInfo);
			BundlerInfo bundlerInfo = protocolInfo.bundlerInfo();
			if (bundlerInfo != null) {
				PacketBundleUnpacker packetBundleUnpacker = new PacketBundleUnpacker(bundlerInfo);
				outboundConfigurationTask = outboundConfigurationTask.andThen(
					channelHandlerContext -> channelHandlerContext.pipeline().addAfter("encoder", "unbundler", packetBundleUnpacker)
				);
			}

			boolean bl = protocolInfo.id() == ConnectionProtocol.LOGIN;
			syncAfterConfigurationChange(this.channel.writeAndFlush(outboundConfigurationTask.andThen(channelHandlerContext -> this.sendLoginDisconnect = bl)));
		}
	}

	public void setListenerForServerboundHandshake(PacketListener packetListener) {
		if (this.packetListener != null) {
			throw new IllegalStateException("Listener already set");
		} else if (this.receiving == PacketFlow.SERVERBOUND && packetListener.flow() == PacketFlow.SERVERBOUND && packetListener.protocol() == INITIAL_PROTOCOL.id()) {
			this.packetListener = packetListener;
		} else {
			throw new IllegalStateException("Invalid initial listener");
		}
	}

	public void initiateServerboundStatusConnection(String string, int i, ClientStatusPacketListener clientStatusPacketListener) {
		this.initiateServerboundConnection(string, i, StatusProtocols.SERVERBOUND, StatusProtocols.CLIENTBOUND, clientStatusPacketListener, ClientIntent.STATUS);
	}

	public void initiateServerboundPlayConnection(String string, int i, ClientLoginPacketListener clientLoginPacketListener) {
		this.initiateServerboundConnection(string, i, LoginProtocols.SERVERBOUND, LoginProtocols.CLIENTBOUND, clientLoginPacketListener, ClientIntent.LOGIN);
	}

	public <S extends ServerboundPacketListener, C extends ClientboundPacketListener> void initiateServerboundPlayConnection(
		String string, int i, ProtocolInfo<S> protocolInfo, ProtocolInfo<C> protocolInfo2, C clientboundPacketListener, boolean bl
	) {
		this.initiateServerboundConnection(string, i, protocolInfo, protocolInfo2, clientboundPacketListener, bl ? ClientIntent.TRANSFER : ClientIntent.LOGIN);
	}

	private <S extends ServerboundPacketListener, C extends ClientboundPacketListener> void initiateServerboundConnection(
		String string, int i, ProtocolInfo<S> protocolInfo, ProtocolInfo<C> protocolInfo2, C clientboundPacketListener, ClientIntent clientIntent
	) {
		if (protocolInfo.id() != protocolInfo2.id()) {
			throw new IllegalStateException("Mismatched initial protocols");
		} else {
			this.disconnectListener = clientboundPacketListener;
			this.runOnceConnected(connection -> {
				this.setupInboundProtocol(protocolInfo2, clientboundPacketListener);
				connection.sendPacket(new ClientIntentionPacket(SharedConstants.getCurrentVersion().getProtocolVersion(), string, i, clientIntent), null, true);
				this.setupOutboundProtocol(protocolInfo);
			});
		}
	}

	public void send(Packet<?> packet) {
		this.send(packet, null);
	}

	public void send(Packet<?> packet, @Nullable PacketSendListener packetSendListener) {
		this.send(packet, packetSendListener, true);
	}

	public void send(Packet<?> packet, @Nullable PacketSendListener packetSendListener, boolean bl) {
		if (this.isConnected()) {
			this.flushQueue();
			this.sendPacket(packet, packetSendListener, bl);
		} else {
			this.pendingActions.add((Consumer)connection -> connection.sendPacket(packet, packetSendListener, bl));
		}
	}

	public void runOnceConnected(Consumer<Connection> consumer) {
		if (this.isConnected()) {
			this.flushQueue();
			consumer.accept(this);
		} else {
			this.pendingActions.add(consumer);
		}
	}

	private void sendPacket(Packet<?> packet, @Nullable PacketSendListener packetSendListener, boolean bl) {
		this.sentPackets++;
		if (this.channel.eventLoop().inEventLoop()) {
			this.doSendPacket(packet, packetSendListener, bl);
		} else {
			this.channel.eventLoop().execute(() -> this.doSendPacket(packet, packetSendListener, bl));
		}
	}

	private void doSendPacket(Packet<?> packet, @Nullable PacketSendListener packetSendListener, boolean bl) {
		ChannelFuture channelFuture = bl ? this.channel.writeAndFlush(packet) : this.channel.write(packet);
		if (packetSendListener != null) {
			channelFuture.addListener(future -> {
				if (future.isSuccess()) {
					packetSendListener.onSuccess();
				} else {
					Packet<?> packetx = packetSendListener.onFailure();
					if (packetx != null) {
						ChannelFuture channelFuturex = this.channel.writeAndFlush(packetx);
						channelFuturex.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
					}
				}
			});
		}

		channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}

	public void flushChannel() {
		if (this.isConnected()) {
			this.flush();
		} else {
			this.pendingActions.add(Connection::flush);
		}
	}

	private void flush() {
		if (this.channel.eventLoop().inEventLoop()) {
			this.channel.flush();
		} else {
			this.channel.eventLoop().execute(() -> this.channel.flush());
		}
	}

	private void flushQueue() {
		if (this.channel != null && this.channel.isOpen()) {
			synchronized (this.pendingActions) {
				Consumer<Connection> consumer;
				while ((consumer = (Consumer<Connection>)this.pendingActions.poll()) != null) {
					consumer.accept(this);
				}
			}
		}
	}

	public void tick() {
		this.flushQueue();
		if (this.packetListener instanceof TickablePacketListener tickablePacketListener) {
			tickablePacketListener.tick();
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

		if (this.bandwidthDebugMonitor != null) {
			this.bandwidthDebugMonitor.tick();
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

	public String getLoggableAddress(boolean bl) {
		if (this.address == null) {
			return "local";
		} else {
			return bl ? this.address.toString() : "IP hidden";
		}
	}

	public void disconnect(Component component) {
		this.disconnect(new DisconnectionDetails(component));
	}

	public void disconnect(DisconnectionDetails disconnectionDetails) {
		if (this.channel == null) {
			this.delayedDisconnect = disconnectionDetails;
		}

		if (this.isConnected()) {
			this.channel.close().awaitUninterruptibly();
			this.disconnectionDetails = disconnectionDetails;
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

	public static Connection connectToServer(InetSocketAddress inetSocketAddress, boolean bl, @Nullable LocalSampleLogger localSampleLogger) {
		Connection connection = new Connection(PacketFlow.CLIENTBOUND);
		if (localSampleLogger != null) {
			connection.setBandwidthLogger(localSampleLogger);
		}

		ChannelFuture channelFuture = connect(inetSocketAddress, bl, connection);
		channelFuture.syncUninterruptibly();
		return connection;
	}

	public static ChannelFuture connect(InetSocketAddress inetSocketAddress, boolean bl, Connection connection) {
		Class<? extends SocketChannel> class_;
		EventLoopGroup eventLoopGroup;
		if (Epoll.isAvailable() && bl) {
			class_ = EpollSocketChannel.class;
			eventLoopGroup = (EventLoopGroup)NETWORK_EPOLL_WORKER_GROUP.get();
		} else {
			class_ = NioSocketChannel.class;
			eventLoopGroup = (EventLoopGroup)NETWORK_WORKER_GROUP.get();
		}

		return new Bootstrap().group(eventLoopGroup).handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel channel) {
				try {
					channel.config().setOption(ChannelOption.TCP_NODELAY, true);
				} catch (ChannelException var3) {
				}

				ChannelPipeline channelPipeline = channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
				Connection.configureSerialization(channelPipeline, PacketFlow.CLIENTBOUND, false, connection.bandwidthDebugMonitor);
				connection.configurePacketHandler(channelPipeline);
			}
		}).channel(class_).connect(inetSocketAddress.getAddress(), inetSocketAddress.getPort());
	}

	private static String outboundHandlerName(boolean bl) {
		return bl ? "encoder" : "outbound_config";
	}

	private static String inboundHandlerName(boolean bl) {
		return bl ? "decoder" : "inbound_config";
	}

	public void configurePacketHandler(ChannelPipeline channelPipeline) {
		channelPipeline.addLast("hackfix", new ChannelOutboundHandlerAdapter() {
			@Override
			public void write(ChannelHandlerContext channelHandlerContext, Object object, ChannelPromise channelPromise) throws Exception {
				super.write(channelHandlerContext, object, channelPromise);
			}
		}).addLast("packet_handler", this);
	}

	public static void configureSerialization(
		ChannelPipeline channelPipeline, PacketFlow packetFlow, boolean bl, @Nullable BandwidthDebugMonitor bandwidthDebugMonitor
	) {
		PacketFlow packetFlow2 = packetFlow.getOpposite();
		boolean bl2 = packetFlow == PacketFlow.SERVERBOUND;
		boolean bl3 = packetFlow2 == PacketFlow.SERVERBOUND;
		channelPipeline.addLast("splitter", createFrameDecoder(bandwidthDebugMonitor, bl))
			.addLast(new FlowControlHandler())
			.addLast(inboundHandlerName(bl2), (ChannelHandler)(bl2 ? new PacketDecoder<>(INITIAL_PROTOCOL) : new UnconfiguredPipelineHandler.Inbound()))
			.addLast("prepender", createFrameEncoder(bl))
			.addLast(outboundHandlerName(bl3), (ChannelHandler)(bl3 ? new PacketEncoder<>(INITIAL_PROTOCOL) : new UnconfiguredPipelineHandler.Outbound()));
	}

	private static ChannelOutboundHandler createFrameEncoder(boolean bl) {
		return (ChannelOutboundHandler)(bl ? new NoOpFrameEncoder() : new Varint21LengthFieldPrepender());
	}

	private static ChannelInboundHandler createFrameDecoder(@Nullable BandwidthDebugMonitor bandwidthDebugMonitor, boolean bl) {
		if (!bl) {
			return new Varint21FrameDecoder(bandwidthDebugMonitor);
		} else {
			return (ChannelInboundHandler)(bandwidthDebugMonitor != null ? new MonitorFrameDecoder(bandwidthDebugMonitor) : new NoOpFrameDecoder());
		}
	}

	public static void configureInMemoryPipeline(ChannelPipeline channelPipeline, PacketFlow packetFlow) {
		configureSerialization(channelPipeline, packetFlow, true, null);
	}

	public static Connection connectToLocalServer(SocketAddress socketAddress) {
		final Connection connection = new Connection(PacketFlow.CLIENTBOUND);
		new Bootstrap().group((EventLoopGroup)LOCAL_WORKER_GROUP.get()).handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel channel) {
				ChannelPipeline channelPipeline = channel.pipeline();
				Connection.configureInMemoryPipeline(channelPipeline, PacketFlow.CLIENTBOUND);
				connection.configurePacketHandler(channelPipeline);
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

	@Nullable
	public PacketListener getPacketListener() {
		return this.packetListener;
	}

	@Nullable
	public DisconnectionDetails getDisconnectionDetails() {
		return this.disconnectionDetails;
	}

	public void setReadOnly() {
		if (this.channel != null) {
			this.channel.config().setAutoRead(false);
		}
	}

	public void setupCompression(int i, boolean bl) {
		if (i >= 0) {
			if (this.channel.pipeline().get("decompress") instanceof CompressionDecoder compressionDecoder) {
				compressionDecoder.setThreshold(i, bl);
			} else {
				this.channel.pipeline().addAfter("splitter", "decompress", new CompressionDecoder(i, bl));
			}

			if (this.channel.pipeline().get("compress") instanceof CompressionEncoder compressionEncoder) {
				compressionEncoder.setThreshold(i);
			} else {
				this.channel.pipeline().addAfter("prepender", "compress", new CompressionEncoder(i));
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
				PacketListener packetListener = this.getPacketListener();
				PacketListener packetListener2 = packetListener != null ? packetListener : this.disconnectListener;
				if (packetListener2 != null) {
					DisconnectionDetails disconnectionDetails = (DisconnectionDetails)Objects.requireNonNullElseGet(
						this.getDisconnectionDetails(), () -> new DisconnectionDetails(Component.translatable("multiplayer.disconnect.generic"))
					);
					packetListener2.onDisconnect(disconnectionDetails);
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

	public void setBandwidthLogger(LocalSampleLogger localSampleLogger) {
		this.bandwidthDebugMonitor = new BandwidthDebugMonitor(localSampleLogger);
	}
}
