package net.minecraft.client.multiplayer;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.network.protocol.status.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ServerStatusPinger {
	static final Splitter SPLITTER = Splitter.on('\u0000').limit(6);
	static final Logger LOGGER = LogManager.getLogger();
	private static final Component CANT_CONNECT_MESSAGE = new TranslatableComponent("multiplayer.status.cannot_connect").withStyle(ChatFormatting.DARK_RED);
	private final List<Connection> connections = Collections.synchronizedList(Lists.newArrayList());

	public void pingServer(ServerData serverData, Runnable runnable) throws UnknownHostException {
		ServerAddress serverAddress = ServerAddress.parseString(serverData.ip);
		Optional<InetSocketAddress> optional = ServerNameResolver.DEFAULT.resolveAddress(serverAddress).map(ResolvedServerAddress::asInetSocketAddress);
		if (!optional.isPresent()) {
			this.onPingFailed(ConnectScreen.UNKNOWN_HOST_MESSAGE, serverData);
		} else {
			final InetSocketAddress inetSocketAddress = (InetSocketAddress)optional.get();
			final Connection connection = Connection.connectToServer(inetSocketAddress, false);
			this.connections.add(connection);
			serverData.motd = new TranslatableComponent("multiplayer.status.pinging");
			serverData.ping = -1L;
			serverData.playerList = null;
			connection.setListener(
				new ClientStatusPacketListener() {
					private boolean success;
					private boolean receivedPing;
					private long pingStart;

					@Override
					public void handleStatusResponse(ClientboundStatusResponsePacket clientboundStatusResponsePacket) {
						if (this.receivedPing) {
							connection.disconnect(new TranslatableComponent("multiplayer.status.unrequested"));
						} else {
							this.receivedPing = true;
							ServerStatus serverStatus = clientboundStatusResponsePacket.getStatus();
							if (serverStatus.getDescription() != null) {
								serverData.motd = serverStatus.getDescription();
							} else {
								serverData.motd = TextComponent.EMPTY;
							}

							if (serverStatus.getVersion() != null) {
								serverData.version = new TextComponent(serverStatus.getVersion().getName());
								serverData.protocol = serverStatus.getVersion().getProtocol();
							} else {
								serverData.version = new TranslatableComponent("multiplayer.status.old");
								serverData.protocol = 0;
							}

							if (serverStatus.getPlayers() != null) {
								serverData.status = ServerStatusPinger.formatPlayerCount(serverStatus.getPlayers().getNumPlayers(), serverStatus.getPlayers().getMaxPlayers());
								List<Component> list = Lists.<Component>newArrayList();
								if (ArrayUtils.isNotEmpty(serverStatus.getPlayers().getSample())) {
									for (GameProfile gameProfile : serverStatus.getPlayers().getSample()) {
										list.add(new TextComponent(gameProfile.getName()));
									}

									if (serverStatus.getPlayers().getSample().length < serverStatus.getPlayers().getNumPlayers()) {
										list.add(
											new TranslatableComponent("multiplayer.status.and_more", serverStatus.getPlayers().getNumPlayers() - serverStatus.getPlayers().getSample().length)
										);
									}

									serverData.playerList = list;
								}
							} else {
								serverData.status = new TranslatableComponent("multiplayer.status.unknown").withStyle(ChatFormatting.DARK_GRAY);
							}

							String string = null;
							if (serverStatus.getFavicon() != null) {
								String string2 = serverStatus.getFavicon();
								if (string2.startsWith("data:image/png;base64,")) {
									string = string2.substring("data:image/png;base64,".length());
								} else {
									ServerStatusPinger.LOGGER.error("Invalid server icon (unknown format)");
								}
							}

							if (!Objects.equals(string, serverData.getIconB64())) {
								serverData.setIconB64(string);
								runnable.run();
							}

							this.pingStart = Util.getMillis();
							connection.send(new ServerboundPingRequestPacket(this.pingStart));
							this.success = true;
						}
					}

					@Override
					public void handlePongResponse(ClientboundPongResponsePacket clientboundPongResponsePacket) {
						long l = this.pingStart;
						long m = Util.getMillis();
						serverData.ping = m - l;
						connection.disconnect(new TranslatableComponent("multiplayer.status.finished"));
					}

					@Override
					public void onDisconnect(Component component) {
						if (!this.success) {
							ServerStatusPinger.this.onPingFailed(component, serverData);
							ServerStatusPinger.this.pingLegacyServer(inetSocketAddress, serverData);
						}
					}

					@Override
					public Connection getConnection() {
						return connection;
					}
				}
			);

			try {
				connection.send(new ClientIntentionPacket(serverAddress.getHost(), serverAddress.getPort(), ConnectionProtocol.STATUS));
				connection.send(new ServerboundStatusRequestPacket());
			} catch (Throwable var8) {
				LOGGER.error(var8);
			}
		}
	}

	void onPingFailed(Component component, ServerData serverData) {
		LOGGER.error("Can't ping {}: {}", serverData.ip, component.getString());
		serverData.motd = CANT_CONNECT_MESSAGE;
		serverData.status = TextComponent.EMPTY;
	}

	void pingLegacyServer(InetSocketAddress inetSocketAddress, ServerData serverData) {
		new Bootstrap().group(Connection.NETWORK_WORKER_GROUP.get()).handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel channel) {
				try {
					channel.config().setOption(ChannelOption.TCP_NODELAY, true);
				} catch (ChannelException var3) {
				}

				channel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
					@Override
					public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
						super.channelActive(channelHandlerContext);
						ByteBuf byteBuf = Unpooled.buffer();

						try {
							byteBuf.writeByte(254);
							byteBuf.writeByte(1);
							byteBuf.writeByte(250);
							char[] cs = "MC|PingHost".toCharArray();
							byteBuf.writeShort(cs.length);

							for (char c : cs) {
								byteBuf.writeChar(c);
							}

							byteBuf.writeShort(7 + 2 * inetSocketAddress.getHostName().length());
							byteBuf.writeByte(127);
							cs = inetSocketAddress.getHostName().toCharArray();
							byteBuf.writeShort(cs.length);

							for (char c : cs) {
								byteBuf.writeChar(c);
							}

							byteBuf.writeInt(inetSocketAddress.getPort());
							channelHandlerContext.channel().writeAndFlush(byteBuf).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
						} finally {
							byteBuf.release();
						}
					}

					protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
						short s = byteBuf.readUnsignedByte();
						if (s == 255) {
							String string = new String(byteBuf.readBytes(byteBuf.readShort() * 2).array(), StandardCharsets.UTF_16BE);
							String[] strings = Iterables.toArray(ServerStatusPinger.SPLITTER.split(string), String.class);
							if ("§1".equals(strings[0])) {
								int i = Mth.getInt(strings[1], 0);
								String string2 = strings[2];
								String string3 = strings[3];
								int j = Mth.getInt(strings[4], -1);
								int k = Mth.getInt(strings[5], -1);
								serverData.protocol = -1;
								serverData.version = new TextComponent(string2);
								serverData.motd = new TextComponent(string3);
								serverData.status = ServerStatusPinger.formatPlayerCount(j, k);
							}
						}

						channelHandlerContext.close();
					}

					@Override
					public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {
						channelHandlerContext.close();
					}
				});
			}
		}).channel(NioSocketChannel.class).connect(inetSocketAddress.getAddress(), inetSocketAddress.getPort());
	}

	static Component formatPlayerCount(int i, int j) {
		return new TextComponent(Integer.toString(i))
			.append(new TextComponent("/").withStyle(ChatFormatting.DARK_GRAY))
			.append(Integer.toString(j))
			.withStyle(ChatFormatting.GRAY);
	}

	public void tick() {
		synchronized (this.connections) {
			Iterator<Connection> iterator = this.connections.iterator();

			while (iterator.hasNext()) {
				Connection connection = (Connection)iterator.next();
				if (connection.isConnected()) {
					connection.tick();
				} else {
					iterator.remove();
					connection.handleDisconnection();
				}
			}
		}
	}

	public void removeAll() {
		synchronized (this.connections) {
			Iterator<Connection> iterator = this.connections.iterator();

			while (iterator.hasNext()) {
				Connection connection = (Connection)iterator.next();
				if (connection.isConnected()) {
					iterator.remove();
					connection.disconnect(new TranslatableComponent("multiplayer.status.cancelled"));
				}
			}
		}
	}
}
