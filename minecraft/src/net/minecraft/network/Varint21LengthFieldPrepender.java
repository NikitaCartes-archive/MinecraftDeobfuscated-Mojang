package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToByteEncoder;

@Sharable
public class Varint21LengthFieldPrepender extends MessageToByteEncoder<ByteBuf> {
	protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf byteBuf2) {
		int i = byteBuf.readableBytes();
		int j = FriendlyByteBuf.getVarIntSize(i);
		if (j > 3) {
			throw new IllegalArgumentException("unable to fit " + i + " into " + 3);
		} else {
			FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBuf2);
			friendlyByteBuf.ensureWritable(j + i);
			friendlyByteBuf.writeVarInt(i);
			friendlyByteBuf.writeBytes(byteBuf, byteBuf.readerIndex(), i);
		}
	}
}
