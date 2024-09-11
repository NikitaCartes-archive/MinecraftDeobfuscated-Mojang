package com.mojang.blaze3d.vertex;

import com.mojang.jtracy.MemoryPool;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.MemoryUtil.MemoryAllocator;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ByteBufferBuilder implements AutoCloseable {
	private static final MemoryPool MEMORY_POOL = TracyClient.createMemoryPool("ByteBufferBuilder");
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);
	private static final int MAX_GROWTH_SIZE = 2097152;
	private static final int BUFFER_FREED_GENERATION = -1;
	long pointer;
	private int capacity;
	private int writeOffset;
	private int nextResultOffset;
	private int resultCount;
	private int generation;

	public ByteBufferBuilder(int i) {
		this.capacity = i;
		this.pointer = ALLOCATOR.malloc((long)i);
		MEMORY_POOL.malloc(this.pointer, i);
		if (this.pointer == 0L) {
			throw new OutOfMemoryError("Failed to allocate " + i + " bytes");
		}
	}

	public long reserve(int i) {
		int j = this.writeOffset;
		int k = j + i;
		this.ensureCapacity(k);
		this.writeOffset = k;
		return this.pointer + (long)j;
	}

	private void ensureCapacity(int i) {
		if (i > this.capacity) {
			int j = Math.min(this.capacity, 2097152);
			int k = Math.max(this.capacity + j, i);
			this.resize(k);
		}
	}

	private void resize(int i) {
		MEMORY_POOL.free(this.pointer);
		this.pointer = ALLOCATOR.realloc(this.pointer, (long)i);
		MEMORY_POOL.malloc(this.pointer, i);
		LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", this.capacity, i);
		if (this.pointer == 0L) {
			throw new OutOfMemoryError("Failed to resize buffer from " + this.capacity + " bytes to " + i + " bytes");
		} else {
			this.capacity = i;
		}
	}

	@Nullable
	public ByteBufferBuilder.Result build() {
		this.checkOpen();
		int i = this.nextResultOffset;
		int j = this.writeOffset - i;
		if (j == 0) {
			return null;
		} else {
			this.nextResultOffset = this.writeOffset;
			this.resultCount++;
			return new ByteBufferBuilder.Result(i, j, this.generation);
		}
	}

	public void clear() {
		if (this.resultCount > 0) {
			LOGGER.warn("Clearing BufferBuilder with unused batches");
		}

		this.discard();
	}

	public void discard() {
		this.checkOpen();
		if (this.resultCount > 0) {
			this.discardResults();
			this.resultCount = 0;
		}
	}

	boolean isValid(int i) {
		return i == this.generation;
	}

	void freeResult() {
		if (--this.resultCount <= 0) {
			this.discardResults();
		}
	}

	private void discardResults() {
		int i = this.writeOffset - this.nextResultOffset;
		if (i > 0) {
			MemoryUtil.memCopy(this.pointer + (long)this.nextResultOffset, this.pointer, (long)i);
		}

		this.writeOffset = i;
		this.nextResultOffset = 0;
		this.generation++;
	}

	public void close() {
		if (this.pointer != 0L) {
			MEMORY_POOL.free(this.pointer);
			ALLOCATOR.free(this.pointer);
			this.pointer = 0L;
			this.generation = -1;
		}
	}

	private void checkOpen() {
		if (this.pointer == 0L) {
			throw new IllegalStateException("Buffer has been freed");
		}
	}

	@Environment(EnvType.CLIENT)
	public class Result implements AutoCloseable {
		private final int offset;
		private final int capacity;
		private final int generation;
		private boolean closed;

		Result(final int i, final int j, final int k) {
			this.offset = i;
			this.capacity = j;
			this.generation = k;
		}

		public ByteBuffer byteBuffer() {
			if (!ByteBufferBuilder.this.isValid(this.generation)) {
				throw new IllegalStateException("Buffer is no longer valid");
			} else {
				return MemoryUtil.memByteBuffer(ByteBufferBuilder.this.pointer + (long)this.offset, this.capacity);
			}
		}

		public void close() {
			if (!this.closed) {
				this.closed = true;
				if (ByteBufferBuilder.this.isValid(this.generation)) {
					ByteBufferBuilder.this.freeResult();
				}
			}
		}
	}
}
