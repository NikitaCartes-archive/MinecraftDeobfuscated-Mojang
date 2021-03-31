package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.zip.Deflater;

public class CompressionEncoder extends MessageToByteEncoder<ByteBuf> {
	private final byte[] encodeBuf = new byte[8192];
	private final Deflater deflater;
	private int threshold;

	public CompressionEncoder(int i) {
		this.threshold = i;
		this.deflater = new Deflater();
	}

	protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf byteBuf2) {
		int i = byteBuf.readableBytes();
		FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBuf2);
		if (i < this.threshold) {
			friendlyByteBuf.writeVarInt(0);
			friendlyByteBuf.writeBytes(byteBuf);
		} else {
			byte[] bs = new byte[i];
			byteBuf.readBytes(bs);
			friendlyByteBuf.writeVarInt(bs.length);
			this.deflater.setInput(bs, 0, i);
			this.deflater.finish();

			while (!this.deflater.finished()) {
				int j = this.deflater.deflate(this.encodeBuf);
				friendlyByteBuf.writeBytes(this.encodeBuf, 0, j);
			}

			this.deflater.reset();
		}
	}

	public int getThreshold() {
		return this.threshold;
	}

	public void setThreshold(int i) {
		this.threshold = i;
	}
}
