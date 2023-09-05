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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
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
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.util.Mth;
import net.minecraft.util.SampleLogger;
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
	public static final AttributeKey<ConnectionProtocol.CodecData<?>> ATTRIBUTE_SERVERBOUND_PROTOCOL = AttributeKey.valueOf("serverbound_protocol");
	public static final AttributeKey<ConnectionProtocol.CodecData<?>> ATTRIBUTE_CLIENTBOUND_PROTOCOL = AttributeKey.valueOf("clientbound_protocol");
	public static final Supplier<NioEventLoopGroup> NETWORK_WORKER_GROUP = Suppliers.memoize(
		() -> new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Client IO #%d").setDaemon(true).build())
	);
	public static final Supplier<EpollEventLoopGroup> NETWORK_EPOLL_WORKER_GROUP = Suppliers.memoize(
		() -> new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build())
	);
	public static final Supplier<DefaultEventLoopGroup> LOCAL_WORKER_GROUP = Suppliers.memoize(
		() -> new DefaultEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Local Client IO #%d").setDaemon(true).build())
	);
	private final PacketFlow receiving;
	private final Queue<Consumer<Connection>> pendingActions = Queues.<Consumer<Connection>>newConcurrentLinkedQueue();
	private Channel channel;
	private SocketAddress address;
	@Nullable
	private volatile PacketListener disconnectListener;
	@Nullable
	private volatile PacketListener packetListener;
	@Nullable
	private Component disconnectedReason;
	private boolean encrypted;
	private boolean disconnectionHandled;
	private int receivedPackets;
	private int sentPackets;
	private float averageReceivedPackets;
	private float averageSentPackets;
	private int tickCount;
	private boolean handlingFault;
	@Nullable
	private volatile Component delayedDisconnect;
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

	public static void setInitialProtocolAttributes(Channel channel) {
		channel.attr(ATTRIBUTE_SERVERBOUND_PROTOCOL).set(ConnectionProtocol.HANDSHAKING.codec(PacketFlow.SERVERBOUND));
		channel.attr(ATTRIBUTE_CLIENTBOUND_PROTOCOL).set(ConnectionProtocol.HANDSHAKING.codec(PacketFlow.CLIENTBOUND));
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
					if (bl) {
						LOGGER.debug("Failed to sent packet", throwable);
						if (this.getSending() == PacketFlow.CLIENTBOUND) {
							ConnectionProtocol connectionProtocol = this.channel.attr(ATTRIBUTE_CLIENTBOUND_PROTOCOL).get().protocol();
							Packet<?> packet = (Packet<?>)(connectionProtocol == ConnectionProtocol.LOGIN
								? new ClientboundLoginDisconnectPacket(component)
								: new ClientboundDisconnectPacket(component));
							this.send(packet, PacketSendListener.thenRun(() -> this.disconnect(component)));
						} else {
							this.disconnect(component);
						}

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

	public void suspendInboundAfterProtocolChange() {
		this.channel.config().setAutoRead(false);
	}

	public void resumeInboundAfterProtocolChange() {
		this.channel.config().setAutoRead(true);
	}

	public void setListener(PacketListener packetListener) {
		Validate.notNull(packetListener, "packetListener");
		PacketFlow packetFlow = packetListener.flow();
		if (packetFlow != this.receiving) {
			throw new IllegalStateException("Trying to set listener for wrong side: connection is " + this.receiving + ", but listener is " + packetFlow);
		} else {
			ConnectionProtocol connectionProtocol = packetListener.protocol();
			ConnectionProtocol connectionProtocol2 = this.channel.attr(getProtocolKey(packetFlow)).get().protocol();
			if (connectionProtocol2 != connectionProtocol) {
				throw new IllegalStateException(
					"Trying to set listener for protocol " + connectionProtocol.id() + ", but current " + packetFlow + " protocol is " + connectionProtocol2.id()
				);
			} else {
				this.packetListener = packetListener;
				this.disconnectListener = null;
			}
		}
	}

	public void setListenerForServerboundHandshake(PacketListener packetListener) {
		if (this.packetListener != null) {
			throw new IllegalStateException("Listener already set");
		} else if (this.receiving == PacketFlow.SERVERBOUND
			&& packetListener.flow() == PacketFlow.SERVERBOUND
			&& packetListener.protocol() == ConnectionProtocol.HANDSHAKING) {
			this.packetListener = packetListener;
		} else {
			throw new IllegalStateException("Invalid initial listener");
		}
	}

	public void initiateServerboundStatusConnection(String string, int i, ClientStatusPacketListener clientStatusPacketListener) {
		this.initiateServerboundConnection(string, i, clientStatusPacketListener, ClientIntent.STATUS);
	}

	public void initiateServerboundPlayConnection(String string, int i, ClientLoginPacketListener clientLoginPacketListener) {
		this.initiateServerboundConnection(string, i, clientLoginPacketListener, ClientIntent.LOGIN);
	}

	private void initiateServerboundConnection(String string, int i, PacketListener packetListener, ClientIntent clientIntent) {
		this.disconnectListener = packetListener;
		this.runOnceConnected(connection -> {
			connection.setClientboundProtocolAfterHandshake(clientIntent);
			this.setListener(packetListener);
			connection.sendPacket(new ClientIntentionPacket(SharedConstants.getCurrentVersion().getProtocolVersion(), string, i, clientIntent), null, true);
		});
	}

	public void setClientboundProtocolAfterHandshake(ClientIntent clientIntent) {
		this.channel.attr(ATTRIBUTE_CLIENTBOUND_PROTOCOL).set(clientIntent.protocol().codec(PacketFlow.CLIENTBOUND));
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

	private static AttributeKey<ConnectionProtocol.CodecData<?>> getProtocolKey(PacketFlow packetFlow) {
		return switch (packetFlow) {
			case CLIENTBOUND -> ATTRIBUTE_CLIENTBOUND_PROTOCOL;
			case SERVERBOUND -> ATTRIBUTE_SERVERBOUND_PROTOCOL;
		};
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
		if (this.channel == null) {
			this.delayedDisconnect = component;
		}

		if (this.isConnected()) {
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

	public static Connection connectToServer(InetSocketAddress inetSocketAddress, boolean bl, @Nullable SampleLogger sampleLogger) {
		Connection connection = new Connection(PacketFlow.CLIENTBOUND);
		if (sampleLogger != null) {
			connection.setBandwidthLogger(sampleLogger);
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
				Connection.setInitialProtocolAttributes(channel);

				try {
					channel.config().setOption(ChannelOption.TCP_NODELAY, true);
				} catch (ChannelException var3) {
				}

				ChannelPipeline channelPipeline = channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
				Connection.configureSerialization(channelPipeline, PacketFlow.CLIENTBOUND, connection.bandwidthDebugMonitor);
				connection.configurePacketHandler(channelPipeline);
			}
		}).channel(class_).connect(inetSocketAddress.getAddress(), inetSocketAddress.getPort());
	}

	public static void configureSerialization(ChannelPipeline channelPipeline, PacketFlow packetFlow, @Nullable BandwidthDebugMonitor bandwidthDebugMonitor) {
		PacketFlow packetFlow2 = packetFlow.getOpposite();
		AttributeKey<ConnectionProtocol.CodecData<?>> attributeKey = getProtocolKey(packetFlow);
		AttributeKey<ConnectionProtocol.CodecData<?>> attributeKey2 = getProtocolKey(packetFlow2);
		channelPipeline.addLast("splitter", new Varint21FrameDecoder(bandwidthDebugMonitor))
			.addLast("decoder", new PacketDecoder(attributeKey))
			.addLast("prepender", new Varint21LengthFieldPrepender())
			.addLast("encoder", new PacketEncoder(attributeKey2))
			.addLast("unbundler", new PacketBundleUnpacker(attributeKey2))
			.addLast("bundler", new PacketBundlePacker(attributeKey));
	}

	public void configurePacketHandler(ChannelPipeline channelPipeline) {
		channelPipeline.addLast(new FlowControlHandler()).addLast("packet_handler", this);
	}

	private static void configureInMemoryPacketValidation(ChannelPipeline channelPipeline, PacketFlow packetFlow) {
		PacketFlow packetFlow2 = packetFlow.getOpposite();
		AttributeKey<ConnectionProtocol.CodecData<?>> attributeKey = getProtocolKey(packetFlow);
		AttributeKey<ConnectionProtocol.CodecData<?>> attributeKey2 = getProtocolKey(packetFlow2);
		channelPipeline.addLast("validator", new PacketFlowValidator(attributeKey, attributeKey2));
	}

	public static void configureInMemoryPipeline(ChannelPipeline channelPipeline, PacketFlow packetFlow) {
		configureInMemoryPacketValidation(channelPipeline, packetFlow);
	}

	public static Connection connectToLocalServer(SocketAddress socketAddress) {
		final Connection connection = new Connection(PacketFlow.CLIENTBOUND);
		new Bootstrap().group((EventLoopGroup)LOCAL_WORKER_GROUP.get()).handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel channel) {
				Connection.setInitialProtocolAttributes(channel);
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
	public Component getDisconnectedReason() {
		return this.disconnectedReason;
	}

	public void setReadOnly() {
		if (this.channel != null) {
			this.channel.config().setAutoRead(false);
		}
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
				PacketListener packetListener = this.getPacketListener();
				PacketListener packetListener2 = packetListener != null ? packetListener : this.disconnectListener;
				if (packetListener2 != null) {
					Component component = (Component)Objects.requireNonNullElseGet(
						this.getDisconnectedReason(), () -> Component.translatable("multiplayer.disconnect.generic")
					);
					packetListener2.onDisconnect(component);
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

	public void setBandwidthLogger(SampleLogger sampleLogger) {
		this.bandwidthDebugMonitor = new BandwidthDebugMonitor(sampleLogger);
	}
}
