package com.mojang.blaze3d.buffers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.jtracy.MemoryPool;
import com.mojang.jtracy.TracyClient;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class GpuBuffer implements AutoCloseable {
	private static final MemoryPool MEMORY_POOl = TracyClient.createMemoryPool("GPU Buffers");
	private final BufferType type;
	private final BufferUsage usage;
	private boolean closed;
	private boolean initialized = false;
	public final int handle;
	public int size;

	public GpuBuffer(BufferType bufferType, BufferUsage bufferUsage, int i) {
		this.type = bufferType;
		this.size = i;
		this.usage = bufferUsage;
		this.handle = GlStateManager._glGenBuffers();
	}

	public GpuBuffer(BufferType bufferType, BufferUsage bufferUsage, ByteBuffer byteBuffer) {
		this(bufferType, bufferUsage, byteBuffer.remaining());
		this.write(byteBuffer, 0);
	}

	public void resize(int i) {
		if (this.closed) {
			throw new IllegalStateException("Buffer already closed");
		} else {
			if (this.initialized) {
				MEMORY_POOl.free((long)this.handle);
			}

			this.size = i;
			if (this.usage.writable) {
				this.initialized = false;
			} else {
				this.bind();
				GlStateManager._glBufferData(this.type.id, (long)i, this.usage.id);
				MEMORY_POOl.malloc((long)this.handle, i);
				this.initialized = true;
			}
		}
	}

	public void write(ByteBuffer byteBuffer, int i) {
		if (this.closed) {
			throw new IllegalStateException("Buffer already closed");
		} else if (!this.usage.writable) {
			throw new IllegalStateException("Buffer is not writable");
		} else {
			int j = byteBuffer.remaining();
			if (j + i > this.size) {
				throw new IllegalArgumentException(
					"Cannot write more data than this buffer can hold (attempting to write " + j + " bytes at offset " + i + " to " + this.size + " size buffer)"
				);
			} else {
				this.bind();
				if (this.initialized) {
					GlStateManager._glBufferSubData(this.type.id, i, byteBuffer);
				} else if (i == 0 && j == this.size) {
					GlStateManager._glBufferData(this.type.id, byteBuffer, this.usage.id);
					MEMORY_POOl.malloc((long)this.handle, this.size);
					this.initialized = true;
				} else {
					GlStateManager._glBufferData(this.type.id, (long)this.size, this.usage.id);
					GlStateManager._glBufferSubData(this.type.id, i, byteBuffer);
					MEMORY_POOl.malloc((long)this.handle, this.size);
					this.initialized = true;
				}
			}
		}
	}

	@Nullable
	public GpuBuffer.ReadView read() {
		return this.read(0, this.size);
	}

	@Nullable
	public GpuBuffer.ReadView read(int i, int j) {
		if (this.closed) {
			throw new IllegalStateException("Buffer already closed");
		} else if (!this.usage.readable) {
			throw new IllegalStateException("Buffer is not readable");
		} else if (i + j > this.size) {
			throw new IllegalArgumentException(
				"Cannot read more data than this buffer can hold (attempting to read " + j + " bytes at offset " + i + " from " + this.size + " size buffer)"
			);
		} else {
			this.bind();
			ByteBuffer byteBuffer = GlStateManager._glMapBufferRange(this.type.id, i, j, 1);
			return byteBuffer == null ? null : new GpuBuffer.ReadView(this.type.id, byteBuffer);
		}
	}

	public void close() {
		if (!this.closed) {
			this.closed = true;
			GlStateManager._glDeleteBuffers(this.handle);
			if (this.initialized) {
				MEMORY_POOl.free((long)this.handle);
			}
		}
	}

	public void bind() {
		GlStateManager._glBindBuffer(this.type.id, this.handle);
	}

	@Environment(EnvType.CLIENT)
	public static class ReadView implements AutoCloseable {
		private final int target;
		private final ByteBuffer data;

		protected ReadView(int i, ByteBuffer byteBuffer) {
			this.target = i;
			this.data = byteBuffer;
		}

		public ByteBuffer data() {
			return this.data;
		}

		public void close() {
			GlStateManager._glUnmapBuffer(this.target);
		}
	}
}
