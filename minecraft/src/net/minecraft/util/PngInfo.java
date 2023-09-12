package net.minecraft.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public record PngInfo(int width, int height) {
	private static final long PNG_HEADER = -8552249625308161526L;
	private static final int IHDR_TYPE = 1229472850;
	private static final int IHDR_SIZE = 13;

	public static PngInfo fromStream(InputStream inputStream) throws IOException {
		DataInputStream dataInputStream = new DataInputStream(inputStream);
		if (dataInputStream.readLong() != -8552249625308161526L) {
			throw new IOException("Bad PNG Signature");
		} else if (dataInputStream.readInt() != 13) {
			throw new IOException("Bad length for IHDR chunk!");
		} else if (dataInputStream.readInt() != 1229472850) {
			throw new IOException("Bad type for IHDR chunk!");
		} else {
			int i = dataInputStream.readInt();
			int j = dataInputStream.readInt();
			return new PngInfo(i, j);
		}
	}

	public static PngInfo fromBytes(byte[] bs) throws IOException {
		return fromStream(new ByteArrayInputStream(bs));
	}
}
