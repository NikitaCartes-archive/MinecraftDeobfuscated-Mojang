package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

public class CipherBase {
	private final Cipher cipher;
	private byte[] heapIn = new byte[0];
	private byte[] heapOut = new byte[0];

	protected CipherBase(Cipher cipher) {
		this.cipher = cipher;
	}

	private byte[] bufToByte(ByteBuf byteBuf) {
		int i = byteBuf.readableBytes();
		if (this.heapIn.length < i) {
			this.heapIn = new byte[i];
		}

		byteBuf.readBytes(this.heapIn, 0, i);
		return this.heapIn;
	}

	protected ByteBuf decipher(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws ShortBufferException {
		int i = byteBuf.readableBytes();
		byte[] bs = this.bufToByte(byteBuf);
		ByteBuf byteBuf2 = channelHandlerContext.alloc().heapBuffer(this.cipher.getOutputSize(i));
		byteBuf2.writerIndex(this.cipher.update(bs, 0, i, byteBuf2.array(), byteBuf2.arrayOffset()));
		return byteBuf2;
	}

	protected void encipher(ByteBuf byteBuf, ByteBuf byteBuf2) throws ShortBufferException {
		int i = byteBuf.readableBytes();
		byte[] bs = this.bufToByte(byteBuf);
		int j = this.cipher.getOutputSize(i);
		if (this.heapOut.length < j) {
			this.heapOut = new byte[j];
		}

		byteBuf2.writeBytes(this.heapOut, 0, this.cipher.update(bs, 0, i, this.heapOut));
	}
}
