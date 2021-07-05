package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import java.util.List;
import java.util.zip.Inflater;

public class CompressionDecoder extends ByteToMessageDecoder {
	public static final int MAXIMUM_COMPRESSED_LENGTH = 2097152;
	public static final int MAXIMUM_UNCOMPRESSED_LENGTH = 8388608;
	private final Inflater inflater;
	private int threshold;
	private boolean validateDecompressed;

	public CompressionDecoder(int i, boolean bl) {
		this.threshold = i;
		this.validateDecompressed = bl;
		this.inflater = new Inflater();
	}

	@Override
	protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
		if (byteBuf.readableBytes() != 0) {
			FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBuf);
			int i = friendlyByteBuf.readVarInt();
			if (i == 0) {
				list.add(friendlyByteBuf.readBytes(friendlyByteBuf.readableBytes()));
			} else {
				if (this.validateDecompressed) {
					if (i < this.threshold) {
						throw new DecoderException("Badly compressed packet - size of " + i + " is below server threshold of " + this.threshold);
					}

					if (i > 8388608) {
						throw new DecoderException("Badly compressed packet - size of " + i + " is larger than protocol maximum of 8388608");
					}
				}

				byte[] bs = new byte[friendlyByteBuf.readableBytes()];
				friendlyByteBuf.readBytes(bs);
				this.inflater.setInput(bs);
				byte[] cs = new byte[i];
				this.inflater.inflate(cs);
				list.add(Unpooled.wrappedBuffer(cs));
				this.inflater.reset();
			}
		}
	}

	public void setThreshold(int i, boolean bl) {
		this.threshold = i;
		this.validateDecompressed = bl;
	}
}
