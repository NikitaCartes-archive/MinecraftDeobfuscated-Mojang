package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.nio.charset.StandardCharsets;

public class Utf8String {
	public static String read(ByteBuf byteBuf, int i) {
		int j = ByteBufUtil.utf8MaxBytes(i);
		int k = VarInt.read(byteBuf);
		if (k > j) {
			throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + k + " > " + j + ")");
		} else if (k < 0) {
			throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
		} else {
			int l = byteBuf.readableBytes();
			if (k > l) {
				throw new DecoderException("Not enough bytes in buffer, expected " + k + ", but got " + l);
			} else {
				String string = byteBuf.toString(byteBuf.readerIndex(), k, StandardCharsets.UTF_8);
				byteBuf.readerIndex(byteBuf.readerIndex() + k);
				if (string.length() > i) {
					throw new DecoderException("The received string length is longer than maximum allowed (" + string.length() + " > " + i + ")");
				} else {
					return string;
				}
			}
		}
	}

	public static void write(ByteBuf byteBuf, CharSequence charSequence, int i) {
		if (charSequence.length() > i) {
			throw new EncoderException("String too big (was " + charSequence.length() + " characters, max " + i + ")");
		} else {
			int j = ByteBufUtil.utf8MaxBytes(charSequence);
			ByteBuf byteBuf2 = byteBuf.alloc().buffer(j);

			try {
				int k = ByteBufUtil.writeUtf8(byteBuf2, charSequence);
				int l = ByteBufUtil.utf8MaxBytes(i);
				if (k > l) {
					throw new EncoderException("String too big (was " + k + " bytes encoded, max " + l + ")");
				}

				VarInt.write(byteBuf, k);
				byteBuf.writeBytes(byteBuf2);
			} finally {
				byteBuf2.release();
			}
		}
	}
}
