/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
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

@Environment(value=EnvType.CLIENT)
public class ServerStatusPinger {
    private static final Splitter SPLITTER = Splitter.on('\u0000').limit(6);
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<Connection> connections = Collections.synchronizedList(Lists.newArrayList());

    public void pingServer(final ServerData serverData) throws UnknownHostException {
        ServerAddress serverAddress = ServerAddress.parseString(serverData.ip);
        final Connection connection = Connection.connectToServer(InetAddress.getByName(serverAddress.getHost()), serverAddress.getPort(), false);
        this.connections.add(connection);
        serverData.motd = I18n.get("multiplayer.status.pinging", new Object[0]);
        serverData.ping = -1L;
        serverData.playerList = null;
        connection.setListener(new ClientStatusPacketListener(){
            private boolean success;
            private boolean receivedPing;
            private long pingStart;

            @Override
            public void handleStatusResponse(ClientboundStatusResponsePacket clientboundStatusResponsePacket) {
                if (this.receivedPing) {
                    connection.disconnect(new TranslatableComponent("multiplayer.status.unrequested", new Object[0]));
                    return;
                }
                this.receivedPing = true;
                ServerStatus serverStatus = clientboundStatusResponsePacket.getStatus();
                serverData.motd = serverStatus.getDescription() != null ? serverStatus.getDescription().getColoredString() : "";
                if (serverStatus.getVersion() != null) {
                    serverData.version = serverStatus.getVersion().getName();
                    serverData.protocol = serverStatus.getVersion().getProtocol();
                } else {
                    serverData.version = I18n.get("multiplayer.status.old", new Object[0]);
                    serverData.protocol = 0;
                }
                if (serverStatus.getPlayers() != null) {
                    serverData.status = (Object)((Object)ChatFormatting.GRAY) + "" + serverStatus.getPlayers().getNumPlayers() + "" + (Object)((Object)ChatFormatting.DARK_GRAY) + "/" + (Object)((Object)ChatFormatting.GRAY) + serverStatus.getPlayers().getMaxPlayers();
                    if (ArrayUtils.isNotEmpty(serverStatus.getPlayers().getSample())) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (GameProfile gameProfile : serverStatus.getPlayers().getSample()) {
                            if (stringBuilder.length() > 0) {
                                stringBuilder.append("\n");
                            }
                            stringBuilder.append(gameProfile.getName());
                        }
                        if (serverStatus.getPlayers().getSample().length < serverStatus.getPlayers().getNumPlayers()) {
                            if (stringBuilder.length() > 0) {
                                stringBuilder.append("\n");
                            }
                            stringBuilder.append(I18n.get("multiplayer.status.and_more", serverStatus.getPlayers().getNumPlayers() - serverStatus.getPlayers().getSample().length));
                        }
                        serverData.playerList = stringBuilder.toString();
                    }
                } else {
                    serverData.status = (Object)((Object)ChatFormatting.DARK_GRAY) + I18n.get("multiplayer.status.unknown", new Object[0]);
                }
                if (serverStatus.getFavicon() != null) {
                    String string = serverStatus.getFavicon();
                    if (string.startsWith("data:image/png;base64,")) {
                        serverData.setIconB64(string.substring("data:image/png;base64,".length()));
                    } else {
                        LOGGER.error("Invalid server icon (unknown format)");
                    }
                } else {
                    serverData.setIconB64(null);
                }
                this.pingStart = Util.getMillis();
                connection.send(new ServerboundPingRequestPacket(this.pingStart));
                this.success = true;
            }

            @Override
            public void handlePongResponse(ClientboundPongResponsePacket clientboundPongResponsePacket) {
                long l = this.pingStart;
                long m = Util.getMillis();
                serverData.ping = m - l;
                connection.disconnect(new TranslatableComponent("multiplayer.status.finished", new Object[0]));
            }

            @Override
            public void onDisconnect(Component component) {
                if (!this.success) {
                    LOGGER.error("Can't ping {}: {}", (Object)serverData.ip, (Object)component.getString());
                    serverData.motd = (Object)((Object)ChatFormatting.DARK_RED) + I18n.get("multiplayer.status.cannot_connect", new Object[0]);
                    serverData.status = "";
                    ServerStatusPinger.this.pingLegacyServer(serverData);
                }
            }

            @Override
            public Connection getConnection() {
                return connection;
            }
        });
        try {
            connection.send(new ClientIntentionPacket(serverAddress.getHost(), serverAddress.getPort(), ConnectionProtocol.STATUS));
            connection.send(new ServerboundStatusRequestPacket());
        } catch (Throwable throwable) {
            LOGGER.error(throwable);
        }
    }

    private void pingLegacyServer(final ServerData serverData) {
        final ServerAddress serverAddress = ServerAddress.parseString(serverData.ip);
        ((Bootstrap)((Bootstrap)((Bootstrap)new Bootstrap().group(Connection.NETWORK_WORKER_GROUP.get())).handler(new ChannelInitializer<Channel>(){

            @Override
            protected void initChannel(Channel channel) throws Exception {
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException channelException) {
                    // empty catch block
                }
                channel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>(){

                    /*
                     * WARNING - Removed try catching itself - possible behaviour change.
                     */
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
                            byteBuf.writeShort(7 + 2 * serverAddress.getHost().length());
                            byteBuf.writeByte(127);
                            cs = serverAddress.getHost().toCharArray();
                            byteBuf.writeShort(cs.length);
                            for (char c : cs) {
                                byteBuf.writeChar(c);
                            }
                            byteBuf.writeInt(serverAddress.getPort());
                            channelHandlerContext.channel().writeAndFlush(byteBuf).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                        } finally {
                            byteBuf.release();
                        }
                    }

                    @Override
                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
                        short s = byteBuf.readUnsignedByte();
                        if (s == 255) {
                            String string = new String(byteBuf.readBytes(byteBuf.readShort() * 2).array(), StandardCharsets.UTF_16BE);
                            String[] strings = Iterables.toArray(SPLITTER.split(string), String.class);
                            if ("\u00a71".equals(strings[0])) {
                                int i = Mth.getInt(strings[1], 0);
                                String string2 = strings[2];
                                String string3 = strings[3];
                                int j = Mth.getInt(strings[4], -1);
                                int k = Mth.getInt(strings[5], -1);
                                serverData.protocol = -1;
                                serverData.version = string2;
                                serverData.motd = string3;
                                serverData.status = (Object)((Object)ChatFormatting.GRAY) + "" + j + "" + (Object)((Object)ChatFormatting.DARK_GRAY) + "/" + (Object)((Object)ChatFormatting.GRAY) + k;
                            }
                        }
                        channelHandlerContext.close();
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
                        channelHandlerContext.close();
                    }

                    @Override
                    protected /* synthetic */ void channelRead0(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
                        this.channelRead0(channelHandlerContext, (ByteBuf)object);
                    }
                });
            }
        })).channel(NioSocketChannel.class)).connect(serverAddress.getHost(), serverAddress.getPort());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void tick() {
        List<Connection> list = this.connections;
        synchronized (list) {
            Iterator<Connection> iterator = this.connections.iterator();
            while (iterator.hasNext()) {
                Connection connection = iterator.next();
                if (connection.isConnected()) {
                    connection.tick();
                    continue;
                }
                iterator.remove();
                connection.handleDisconnection();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeAll() {
        List<Connection> list = this.connections;
        synchronized (list) {
            Iterator<Connection> iterator = this.connections.iterator();
            while (iterator.hasNext()) {
                Connection connection = iterator.next();
                if (!connection.isConnected()) continue;
                iterator.remove();
                connection.disconnect(new TranslatableComponent("multiplayer.status.cancelled", new Object[0]));
            }
        }
    }
}

