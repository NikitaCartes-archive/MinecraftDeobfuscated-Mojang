package net.minecraft.util;

import java.io.IOException;
import java.io.InputStream;

public class FastBufferedInputStream extends InputStream {
	private static final int DEFAULT_BUFFER_SIZE = 8192;
	private final InputStream in;
	private final byte[] buffer;
	private int limit;
	private int position;

	public FastBufferedInputStream(InputStream inputStream) {
		this(inputStream, 8192);
	}

	public FastBufferedInputStream(InputStream inputStream, int i) {
		this.in = inputStream;
		this.buffer = new byte[i];
	}

	public int read() throws IOException {
		if (this.position >= this.limit) {
			this.fill();
			if (this.position >= this.limit) {
				return -1;
			}
		}

		return Byte.toUnsignedInt(this.buffer[this.position++]);
	}

	public int read(byte[] bs, int i, int j) throws IOException {
		int k = this.bytesInBuffer();
		if (k <= 0) {
			if (j >= this.buffer.length) {
				return this.in.read(bs, i, j);
			}

			this.fill();
			k = this.bytesInBuffer();
			if (k <= 0) {
				return -1;
			}
		}

		if (j > k) {
			j = k;
		}

		System.arraycopy(this.buffer, this.position, bs, i, j);
		this.position += j;
		return j;
	}

	public long skip(long l) throws IOException {
		if (l <= 0L) {
			return 0L;
		} else {
			long m = (long)this.bytesInBuffer();
			if (m <= 0L) {
				return this.in.skip(l);
			} else {
				if (l > m) {
					l = m;
				}

				this.position = (int)((long)this.position + l);
				return l;
			}
		}
	}

	public int available() throws IOException {
		return this.bytesInBuffer() + this.in.available();
	}

	public void close() throws IOException {
		this.in.close();
	}

	private int bytesInBuffer() {
		return this.limit - this.position;
	}

	private void fill() throws IOException {
		this.limit = 0;
		this.position = 0;
		int i = this.in.read(this.buffer, 0, this.buffer.length);
		if (i > 0) {
			this.limit = i;
		}
	}
}
