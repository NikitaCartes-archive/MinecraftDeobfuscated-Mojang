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
		if (i > 8388608) {
			throw new IllegalArgumentException("Packet too big (is " + i + ", should be less than 8388608)");
		} else {
			if (i < this.threshold) {
				VarInt.write(byteBuf2, 0);
				byteBuf2.writeBytes(byteBuf);
			} else {
				byte[] bs = new byte[i];
				byteBuf.readBytes(bs);
				VarInt.write(byteBuf2, bs.length);
				this.deflater.setInput(bs, 0, i);
				this.deflater.finish();

				while (!this.deflater.finished()) {
					int j = this.deflater.deflate(this.encodeBuf);
					byteBuf2.writeBytes(this.encodeBuf, 0, j);
				}

				this.deflater.reset();
			}
		}
	}

	public int getThreshold() {
		return this.threshold;
	}

	public void setThreshold(int i) {
		this.threshold = i;
	}
}
