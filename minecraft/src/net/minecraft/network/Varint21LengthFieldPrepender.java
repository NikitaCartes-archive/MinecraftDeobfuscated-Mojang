package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;

@Sharable
public class Varint21LengthFieldPrepender extends MessageToByteEncoder<ByteBuf> {
	public static final int MAX_VARINT21_BYTES = 3;

	protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf byteBuf2) {
		int i = byteBuf.readableBytes();
		int j = VarInt.getByteSize(i);
		if (j > 3) {
			throw new EncoderException("Packet too large: size " + i + " is over 8");
		} else {
			byteBuf2.ensureWritable(j + i);
			VarInt.write(byteBuf2, i);
			byteBuf2.writeBytes(byteBuf, byteBuf.readerIndex(), i);
		}
	}
}
