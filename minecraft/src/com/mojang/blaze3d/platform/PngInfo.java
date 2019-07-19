package com.mojang.blaze3d.platform;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.stb.STBIEOFCallback;
import org.lwjgl.stb.STBIIOCallbacks;
import org.lwjgl.stb.STBIReadCallback;
import org.lwjgl.stb.STBISkipCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class PngInfo {
	public final int width;
	public final int height;

	public PngInfo(String string, InputStream inputStream) throws IOException {
		try (
			MemoryStack memoryStack = MemoryStack.stackPush();
			PngInfo.StbReader stbReader = createCallbacks(inputStream);
			STBIReadCallback sTBIReadCallback = STBIReadCallback.create(stbReader::read);
			STBISkipCallback sTBISkipCallback = STBISkipCallback.create(stbReader::skip);
			STBIEOFCallback sTBIEOFCallback = STBIEOFCallback.create(stbReader::eof);
		) {
			STBIIOCallbacks sTBIIOCallbacks = STBIIOCallbacks.mallocStack(memoryStack);
			sTBIIOCallbacks.read(sTBIReadCallback);
			sTBIIOCallbacks.skip(sTBISkipCallback);
			sTBIIOCallbacks.eof(sTBIEOFCallback);
			IntBuffer intBuffer = memoryStack.mallocInt(1);
			IntBuffer intBuffer2 = memoryStack.mallocInt(1);
			IntBuffer intBuffer3 = memoryStack.mallocInt(1);
			if (!STBImage.stbi_info_from_callbacks(sTBIIOCallbacks, 0L, intBuffer, intBuffer2, intBuffer3)) {
				throw new IOException("Could not read info from the PNG file " + string + " " + STBImage.stbi_failure_reason());
			}

			this.width = intBuffer.get(0);
			this.height = intBuffer2.get(0);
		}
	}

	private static PngInfo.StbReader createCallbacks(InputStream inputStream) {
		return (PngInfo.StbReader)(inputStream instanceof FileInputStream
			? new PngInfo.StbReaderSeekableByteChannel(((FileInputStream)inputStream).getChannel())
			: new PngInfo.StbReaderBufferedChannel(Channels.newChannel(inputStream)));
	}

	@Environment(EnvType.CLIENT)
	abstract static class StbReader implements AutoCloseable {
		protected boolean closed;

		private StbReader() {
		}

		int read(long l, long m, int i) {
			try {
				return this.read(m, i);
			} catch (IOException var7) {
				this.closed = true;
				return 0;
			}
		}

		void skip(long l, int i) {
			try {
				this.skip(i);
			} catch (IOException var5) {
				this.closed = true;
			}
		}

		int eof(long l) {
			return this.closed ? 1 : 0;
		}

		protected abstract int read(long l, int i) throws IOException;

		protected abstract void skip(int i) throws IOException;

		public abstract void close() throws IOException;
	}

	@Environment(EnvType.CLIENT)
	static class StbReaderBufferedChannel extends PngInfo.StbReader {
		private final ReadableByteChannel channel;
		private long readBufferAddress = MemoryUtil.nmemAlloc(128L);
		private int bufferSize = 128;
		private int read;
		private int consumed;

		private StbReaderBufferedChannel(ReadableByteChannel readableByteChannel) {
			this.channel = readableByteChannel;
		}

		private void fillReadBuffer(int i) throws IOException {
			ByteBuffer byteBuffer = MemoryUtil.memByteBuffer(this.readBufferAddress, this.bufferSize);
			if (i + this.consumed > this.bufferSize) {
				this.bufferSize = i + this.consumed;
				byteBuffer = MemoryUtil.memRealloc(byteBuffer, this.bufferSize);
				this.readBufferAddress = MemoryUtil.memAddress(byteBuffer);
			}

			byteBuffer.position(this.read);

			while (i + this.consumed > this.read) {
				try {
					int j = this.channel.read(byteBuffer);
					if (j == -1) {
						break;
					}
				} finally {
					this.read = byteBuffer.position();
				}
			}
		}

		@Override
		public int read(long l, int i) throws IOException {
			this.fillReadBuffer(i);
			if (i + this.consumed > this.read) {
				i = this.read - this.consumed;
			}

			MemoryUtil.memCopy(this.readBufferAddress + (long)this.consumed, l, (long)i);
			this.consumed += i;
			return i;
		}

		@Override
		public void skip(int i) throws IOException {
			if (i > 0) {
				this.fillReadBuffer(i);
				if (i + this.consumed > this.read) {
					throw new EOFException("Can't skip past the EOF.");
				}
			}

			if (this.consumed + i < 0) {
				throw new IOException("Can't seek before the beginning: " + (this.consumed + i));
			} else {
				this.consumed += i;
			}
		}

		@Override
		public void close() throws IOException {
			MemoryUtil.nmemFree(this.readBufferAddress);
			this.channel.close();
		}
	}

	@Environment(EnvType.CLIENT)
	static class StbReaderSeekableByteChannel extends PngInfo.StbReader {
		private final SeekableByteChannel channel;

		private StbReaderSeekableByteChannel(SeekableByteChannel seekableByteChannel) {
			this.channel = seekableByteChannel;
		}

		@Override
		public int read(long l, int i) throws IOException {
			ByteBuffer byteBuffer = MemoryUtil.memByteBuffer(l, i);
			return this.channel.read(byteBuffer);
		}

		@Override
		public void skip(int i) throws IOException {
			this.channel.position(this.channel.position() + (long)i);
		}

		@Override
		public int eof(long l) {
			return super.eof(l) != 0 && this.channel.isOpen() ? 1 : 0;
		}

		@Override
		public void close() throws IOException {
			this.channel.close();
		}
	}
}
