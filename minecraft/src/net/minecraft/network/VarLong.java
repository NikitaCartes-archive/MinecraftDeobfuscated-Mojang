package net.minecraft.network;

import io.netty.buffer.ByteBuf;

public class VarLong {
	private static final int MAX_VARLONG_SIZE = 10;
	private static final int DATA_BITS_MASK = 127;
	private static final int CONTINUATION_BIT_MASK = 128;
	private static final int DATA_BITS_PER_BYTE = 7;

	public static int getByteSize(long l) {
		for (int i = 1; i < 10; i++) {
			if ((l & -1L << i * 7) == 0L) {
				return i;
			}
		}

		return 10;
	}

	public static boolean hasContinuationBit(byte b) {
		return (b & 128) == 128;
	}

	public static long read(ByteBuf byteBuf) {
		long l = 0L;
		int i = 0;

		byte b;
		do {
			b = byteBuf.readByte();
			l |= (long)(b & 127) << i++ * 7;
			if (i > 10) {
				throw new RuntimeException("VarLong too big");
			}
		} while (hasContinuationBit(b));

		return l;
	}

	public static ByteBuf write(ByteBuf byteBuf, long l) {
		while ((l & -128L) != 0L) {
			byteBuf.writeByte((int)(l & 127L) | 128);
			l >>>= 7;
		}

		byteBuf.writeByte((int)l);
		return byteBuf;
	}
}
