package net.minecraft.network;

import io.netty.buffer.ByteBuf;

public class VarInt {
	private static final int MAX_VARINT_SIZE = 5;
	private static final int DATA_BITS_MASK = 127;
	private static final int CONTINUATION_BIT_MASK = 128;
	private static final int DATA_BITS_PER_BYTE = 7;

	public static int getByteSize(int i) {
		for (int j = 1; j < 5; j++) {
			if ((i & -1 << j * 7) == 0) {
				return j;
			}
		}

		return 5;
	}

	public static boolean hasContinuationBit(byte b) {
		return (b & 128) == 128;
	}

	public static int read(ByteBuf byteBuf) {
		int i = 0;
		int j = 0;

		byte b;
		do {
			b = byteBuf.readByte();
			i |= (b & 127) << j++ * 7;
			if (j > 5) {
				throw new RuntimeException("VarInt too big");
			}
		} while (hasContinuationBit(b));

		return i;
	}

	public static ByteBuf write(ByteBuf byteBuf, int i) {
		while ((i & -128) != 0) {
			byteBuf.writeByte(i & 127 | 128);
			i >>>= 7;
		}

		byteBuf.writeByte(i);
		return byteBuf;
	}
}
