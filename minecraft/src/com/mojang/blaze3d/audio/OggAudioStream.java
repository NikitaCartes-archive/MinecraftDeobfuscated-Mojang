package com.mojang.blaze3d.audio;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.util.Mth;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class OggAudioStream implements AudioStream {
	private long handle;
	private final AudioFormat audioFormat;
	private final InputStream input;
	private ByteBuffer buffer = MemoryUtil.memAlloc(8192);

	public OggAudioStream(InputStream inputStream) throws IOException {
		this.input = inputStream;
		this.buffer.limit(0);

		try (MemoryStack memoryStack = MemoryStack.stackPush()) {
			IntBuffer intBuffer = memoryStack.mallocInt(1);
			IntBuffer intBuffer2 = memoryStack.mallocInt(1);

			while (this.handle == 0L) {
				if (!this.refillFromStream()) {
					throw new IOException("Failed to find Ogg header");
				}

				int i = this.buffer.position();
				this.buffer.position(0);
				this.handle = STBVorbis.stb_vorbis_open_pushdata(this.buffer, intBuffer, intBuffer2, null);
				this.buffer.position(i);
				int j = intBuffer2.get(0);
				if (j == 1) {
					this.forwardBuffer();
				} else if (j != 0) {
					throw new IOException("Failed to read Ogg file " + j);
				}
			}

			this.buffer.position(this.buffer.position() + intBuffer.get(0));
			STBVorbisInfo sTBVorbisInfo = STBVorbisInfo.mallocStack(memoryStack);
			STBVorbis.stb_vorbis_get_info(this.handle, sTBVorbisInfo);
			this.audioFormat = new AudioFormat((float)sTBVorbisInfo.sample_rate(), 16, sTBVorbisInfo.channels(), true, false);
		}
	}

	private boolean refillFromStream() throws IOException {
		int i = this.buffer.limit();
		int j = this.buffer.capacity() - i;
		if (j == 0) {
			return true;
		} else {
			byte[] bs = new byte[j];
			int k = this.input.read(bs);
			if (k == -1) {
				return false;
			} else {
				int l = this.buffer.position();
				this.buffer.limit(i + k);
				this.buffer.position(i);
				this.buffer.put(bs, 0, k);
				this.buffer.position(l);
				return true;
			}
		}
	}

	private void forwardBuffer() {
		boolean bl = this.buffer.position() == 0;
		boolean bl2 = this.buffer.position() == this.buffer.limit();
		if (bl2 && !bl) {
			this.buffer.position(0);
			this.buffer.limit(0);
		} else {
			ByteBuffer byteBuffer = MemoryUtil.memAlloc(bl ? 2 * this.buffer.capacity() : this.buffer.capacity());
			byteBuffer.put(this.buffer);
			MemoryUtil.memFree(this.buffer);
			byteBuffer.flip();
			this.buffer = byteBuffer;
		}
	}

	private boolean readFrame(OggAudioStream.OutputConcat outputConcat) throws IOException {
		if (this.handle == 0L) {
			return false;
		} else {
			try (MemoryStack memoryStack = MemoryStack.stackPush()) {
				PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
				IntBuffer intBuffer = memoryStack.mallocInt(1);
				IntBuffer intBuffer2 = memoryStack.mallocInt(1);

				while (true) {
					int i = STBVorbis.stb_vorbis_decode_frame_pushdata(this.handle, this.buffer, intBuffer, pointerBuffer, intBuffer2);
					this.buffer.position(this.buffer.position() + i);
					int j = STBVorbis.stb_vorbis_get_error(this.handle);
					if (j == 1) {
						this.forwardBuffer();
						if (!this.refillFromStream()) {
							return false;
						}
					} else {
						if (j != 0) {
							throw new IOException("Failed to read Ogg file " + j);
						}

						int k = intBuffer2.get(0);
						if (k != 0) {
							int l = intBuffer.get(0);
							PointerBuffer pointerBuffer2 = pointerBuffer.getPointerBuffer(l);
							if (l == 1) {
								this.convertMono(pointerBuffer2.getFloatBuffer(0, k), outputConcat);
								return true;
							}

							if (l == 2) {
								this.convertStereo(pointerBuffer2.getFloatBuffer(0, k), pointerBuffer2.getFloatBuffer(1, k), outputConcat);
								return true;
							}

							throw new IllegalStateException("Invalid number of channels: " + l);
						}
					}
				}
			}
		}
	}

	private void convertMono(FloatBuffer floatBuffer, OggAudioStream.OutputConcat outputConcat) {
		while (floatBuffer.hasRemaining()) {
			outputConcat.put(floatBuffer.get());
		}
	}

	private void convertStereo(FloatBuffer floatBuffer, FloatBuffer floatBuffer2, OggAudioStream.OutputConcat outputConcat) {
		while (floatBuffer.hasRemaining() && floatBuffer2.hasRemaining()) {
			outputConcat.put(floatBuffer.get());
			outputConcat.put(floatBuffer2.get());
		}
	}

	public void close() throws IOException {
		if (this.handle != 0L) {
			STBVorbis.stb_vorbis_close(this.handle);
			this.handle = 0L;
		}

		MemoryUtil.memFree(this.buffer);
		this.input.close();
	}

	@Override
	public AudioFormat getFormat() {
		return this.audioFormat;
	}

	@Override
	public ByteBuffer read(int i) throws IOException {
		OggAudioStream.OutputConcat outputConcat = new OggAudioStream.OutputConcat(i + 8192);

		while (this.readFrame(outputConcat) && outputConcat.byteCount < i) {
		}

		return outputConcat.get();
	}

	public ByteBuffer readAll() throws IOException {
		OggAudioStream.OutputConcat outputConcat = new OggAudioStream.OutputConcat(16384);

		while (this.readFrame(outputConcat)) {
		}

		return outputConcat.get();
	}

	@Environment(EnvType.CLIENT)
	static class OutputConcat {
		private final List<ByteBuffer> buffers = Lists.<ByteBuffer>newArrayList();
		private final int bufferSize;
		private int byteCount;
		private ByteBuffer currentBuffer;

		public OutputConcat(int i) {
			this.bufferSize = i + 1 & -2;
			this.createNewBuffer();
		}

		private void createNewBuffer() {
			this.currentBuffer = BufferUtils.createByteBuffer(this.bufferSize);
		}

		public void put(float f) {
			if (this.currentBuffer.remaining() == 0) {
				this.currentBuffer.flip();
				this.buffers.add(this.currentBuffer);
				this.createNewBuffer();
			}

			int i = Mth.clamp((int)(f * 32767.5F - 0.5F), -32768, 32767);
			this.currentBuffer.putShort((short)i);
			this.byteCount += 2;
		}

		public ByteBuffer get() {
			this.currentBuffer.flip();
			if (this.buffers.isEmpty()) {
				return this.currentBuffer;
			} else {
				ByteBuffer byteBuffer = BufferUtils.createByteBuffer(this.byteCount);
				this.buffers.forEach(byteBuffer::put);
				byteBuffer.put(this.currentBuffer);
				byteBuffer.flip();
				return byteBuffer;
			}
		}
	}
}
