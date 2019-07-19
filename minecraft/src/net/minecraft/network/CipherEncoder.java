package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import javax.crypto.Cipher;

public class CipherEncoder extends MessageToByteEncoder<ByteBuf> {
	private final CipherBase cipher;

	public CipherEncoder(Cipher cipher) {
		this.cipher = new CipherBase(cipher);
	}

	protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf byteBuf2) throws Exception {
		this.cipher.encipher(byteBuf, byteBuf2);
	}
}
