/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.IOException;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.SkipPacketException;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketEncoder
extends MessageToByteEncoder<Packet<?>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PacketFlow flow;

    public PacketEncoder(PacketFlow packetFlow) {
        this.flow = packetFlow;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, ByteBuf byteBuf) throws Exception {
        ConnectionProtocol connectionProtocol = channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get();
        if (connectionProtocol == null) {
            throw new RuntimeException("ConnectionProtocol unknown: " + packet);
        }
        Integer integer = connectionProtocol.getPacketId(this.flow, packet);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(Connection.PACKET_SENT_MARKER, "OUT: [{}:{}] {}", new Object[]{channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get(), integer, packet.getClass().getName()});
        }
        if (integer == null) {
            throw new IOException("Can't serialize unregistered packet");
        }
        FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBuf);
        friendlyByteBuf.writeVarInt(integer);
        try {
            int i = friendlyByteBuf.writerIndex();
            packet.write(friendlyByteBuf);
            int j = friendlyByteBuf.writerIndex() - i;
            if (j > 0x800000) {
                throw new IllegalArgumentException("Packet too big (is " + j + ", should be less than 8388608): " + packet);
            }
            int k = channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().getId();
            JvmProfiler.INSTANCE.onPacketSent(k, integer, channelHandlerContext.channel().remoteAddress(), j);
        } catch (Throwable throwable) {
            LOGGER.error("Error receiving packet {}", (Object)integer, (Object)throwable);
            if (packet.isSkippable()) {
                throw new SkipPacketException(throwable);
            }
            throw throwable;
        }
    }

    @Override
    protected /* synthetic */ void encode(ChannelHandlerContext channelHandlerContext, Object object, ByteBuf byteBuf) throws Exception {
        this.encode(channelHandlerContext, (Packet)object, byteBuf);
    }
}

