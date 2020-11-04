/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.RateKickingConnection;
import net.minecraft.network.Varint21FrameDecoder;
import net.minecraft.network.Varint21LengthFieldPrepender;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.LegacyQueryHandler;
import net.minecraft.server.network.MemoryServerHandshakePacketListenerImpl;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import net.minecraft.util.LazyLoadedValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ServerConnectionListener {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final LazyLoadedValue<NioEventLoopGroup> SERVER_EVENT_GROUP = new LazyLoadedValue<NioEventLoopGroup>(() -> new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Server IO #%d").setDaemon(true).build()));
    public static final LazyLoadedValue<EpollEventLoopGroup> SERVER_EPOLL_EVENT_GROUP = new LazyLoadedValue<EpollEventLoopGroup>(() -> new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Epoll Server IO #%d").setDaemon(true).build()));
    private final MinecraftServer server;
    public volatile boolean running;
    private final List<ChannelFuture> channels = Collections.synchronizedList(Lists.newArrayList());
    private final List<Connection> connections = Collections.synchronizedList(Lists.newArrayList());

    public ServerConnectionListener(MinecraftServer minecraftServer) {
        this.server = minecraftServer;
        this.running = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void startTcpServerListener(@Nullable InetAddress inetAddress, int i) throws IOException {
        List<ChannelFuture> list = this.channels;
        synchronized (list) {
            LazyLoadedValue<MultithreadEventLoopGroup> lazyLoadedValue;
            Class class_;
            if (Epoll.isAvailable() && this.server.isEpollEnabled()) {
                class_ = EpollServerSocketChannel.class;
                lazyLoadedValue = SERVER_EPOLL_EVENT_GROUP;
                LOGGER.info("Using epoll channel type");
            } else {
                class_ = NioServerSocketChannel.class;
                lazyLoadedValue = SERVER_EVENT_GROUP;
                LOGGER.info("Using default channel type");
            }
            this.channels.add(((ServerBootstrap)((ServerBootstrap)new ServerBootstrap().channel(class_)).childHandler(new ChannelInitializer<Channel>(){

                @Override
                protected void initChannel(Channel channel) {
                    try {
                        channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                    } catch (ChannelException channelException) {
                        // empty catch block
                    }
                    channel.pipeline().addLast("timeout", (ChannelHandler)new ReadTimeoutHandler(30)).addLast("legacy_query", (ChannelHandler)new LegacyQueryHandler(ServerConnectionListener.this)).addLast("splitter", (ChannelHandler)new Varint21FrameDecoder()).addLast("decoder", (ChannelHandler)new PacketDecoder(PacketFlow.SERVERBOUND)).addLast("prepender", (ChannelHandler)new Varint21LengthFieldPrepender()).addLast("encoder", (ChannelHandler)new PacketEncoder(PacketFlow.CLIENTBOUND));
                    int i = ServerConnectionListener.this.server.getRateLimitPacketsPerSecond();
                    Connection connection = i > 0 ? new RateKickingConnection(i) : new Connection(PacketFlow.SERVERBOUND);
                    ServerConnectionListener.this.connections.add(connection);
                    channel.pipeline().addLast("packet_handler", (ChannelHandler)connection);
                    connection.setListener(new ServerHandshakePacketListenerImpl(ServerConnectionListener.this.server, connection));
                }
            }).group(lazyLoadedValue.get()).localAddress(inetAddress, i)).bind().syncUninterruptibly());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Environment(value=EnvType.CLIENT)
    public SocketAddress startMemoryChannel() {
        ChannelFuture channelFuture;
        List<ChannelFuture> list = this.channels;
        synchronized (list) {
            channelFuture = ((ServerBootstrap)((ServerBootstrap)new ServerBootstrap().channel(LocalServerChannel.class)).childHandler(new ChannelInitializer<Channel>(){

                @Override
                protected void initChannel(Channel channel) {
                    Connection connection = new Connection(PacketFlow.SERVERBOUND);
                    connection.setListener(new MemoryServerHandshakePacketListenerImpl(ServerConnectionListener.this.server, connection));
                    ServerConnectionListener.this.connections.add(connection);
                    channel.pipeline().addLast("packet_handler", (ChannelHandler)connection);
                }
            }).group(SERVER_EVENT_GROUP.get()).localAddress(LocalAddress.ANY)).bind().syncUninterruptibly();
            this.channels.add(channelFuture);
        }
        return channelFuture.channel().localAddress();
    }

    public void stop() {
        this.running = false;
        for (ChannelFuture channelFuture : this.channels) {
            try {
                channelFuture.channel().close().sync();
            } catch (InterruptedException interruptedException) {
                LOGGER.error("Interrupted whilst closing channel");
            }
        }
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
                if (connection.isConnecting()) continue;
                if (connection.isConnected()) {
                    try {
                        connection.tick();
                    } catch (Exception exception) {
                        if (connection.isMemoryConnection()) {
                            throw new ReportedException(CrashReport.forThrowable(exception, "Ticking memory connection"));
                        }
                        LOGGER.warn("Failed to handle packet for {}", (Object)connection.getRemoteAddress(), (Object)exception);
                        TextComponent component = new TextComponent("Internal server error");
                        connection.send(new ClientboundDisconnectPacket(component), future -> connection.disconnect(component));
                        connection.setReadOnly();
                    }
                    continue;
                }
                iterator.remove();
                connection.handleDisconnection();
            }
        }
    }

    public MinecraftServer getServer() {
        return this.server;
    }
}

