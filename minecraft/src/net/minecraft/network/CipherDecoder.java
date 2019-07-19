package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import javax.crypto.Cipher;

public class CipherDecoder extends MessageToMessageDecoder<ByteBuf> {
	private final CipherBase cipher;

	public CipherDecoder(Cipher cipher) {
		this.cipher = new CipherBase(cipher);
	}

	protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
		list.add(this.cipher.decipher(channelHandlerContext, byteBuf));
	}
}
