package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class BufferBuilder extends DefaultedVertexConsumer implements BufferVertexConsumer {
	private static final int MAX_GROWTH_SIZE = 2097152;
	private static final Logger LOGGER = LogUtils.getLogger();
	private ByteBuffer buffer;
	private boolean closed;
	private int renderedBufferCount;
	private int renderedBufferPointer;
	private int nextElementByte;
	private int vertices;
	@Nullable
	private VertexFormatElement currentElement;
	private int elementIndex;
	private VertexFormat format;
	private VertexFormat.Mode mode;
	private boolean fastFormat;
	private boolean fullFormat;
	private boolean building;
	@Nullable
	private Vector3f[] sortingPoints;
	@Nullable
	private VertexSorting sorting;
	private boolean indexOnly;

	public BufferBuilder(int i) {
		this.buffer = MemoryTracker.create(i);
	}

	private void ensureVertexCapacity() {
		this.ensureCapacity(this.format.getVertexSize());
	}

	private void ensureCapacity(int i) {
		if (this.nextElementByte + i > this.buffer.capacity()) {
			int j = this.buffer.capacity();
			int k = Math.min(j, 2097152);
			int l = j + i;
			int m = Math.max(j + k, l);
			LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", j, m);
			ByteBuffer byteBuffer = MemoryTracker.resize(this.buffer, m);
			byteBuffer.rewind();
			this.buffer = byteBuffer;
		}
	}

	public void setQuadSorting(VertexSorting vertexSorting) {
		if (this.mode == VertexFormat.Mode.QUADS) {
			this.sorting = vertexSorting;
			if (this.sortingPoints == null) {
				this.sortingPoints = this.makeQuadSortingPoints();
			}
		}
	}

	public BufferBuilder.SortState getSortState() {
		return new BufferBuilder.SortState(this.mode, this.vertices, this.sortingPoints, this.sorting);
	}

	private void checkOpen() {
		if (this.closed) {
			throw new IllegalStateException("This BufferBuilder has been closed");
		}
	}

	public void restoreSortState(BufferBuilder.SortState sortState) {
		this.checkOpen();
		this.buffer.rewind();
		this.mode = sortState.mode;
		this.vertices = sortState.vertices;
		this.nextElementByte = this.renderedBufferPointer;
		this.sortingPoints = sortState.sortingPoints;
		this.sorting = sortState.sorting;
		this.indexOnly = true;
	}

	public void begin(VertexFormat.Mode mode, VertexFormat vertexFormat) {
		if (this.building) {
			throw new IllegalStateException("Already building!");
		} else {
			this.checkOpen();
			this.building = true;
			this.mode = mode;
			this.switchFormat(vertexFormat);
			this.currentElement = (VertexFormatElement)vertexFormat.getElements().get(0);
			this.elementIndex = 0;
			this.buffer.rewind();
		}
	}

	private void switchFormat(VertexFormat vertexFormat) {
		if (this.format != vertexFormat) {
			this.format = vertexFormat;
			boolean bl = vertexFormat == DefaultVertexFormat.NEW_ENTITY;
			boolean bl2 = vertexFormat == DefaultVertexFormat.BLOCK;
			this.fastFormat = bl || bl2;
			this.fullFormat = bl;
		}
	}

	private IntConsumer intConsumer(int i, VertexFormat.IndexType indexType) {
		MutableInt mutableInt = new MutableInt(i);

		return switch (indexType) {
			case SHORT -> ix -> this.buffer.putShort(mutableInt.getAndAdd(2), (short)ix);
			case INT -> ix -> this.buffer.putInt(mutableInt.getAndAdd(4), ix);
		};
	}

	private Vector3f[] makeQuadSortingPoints() {
		FloatBuffer floatBuffer = this.buffer.asFloatBuffer();
		int i = this.renderedBufferPointer / 4;
		int j = this.format.getIntegerSize();
		int k = j * this.mode.primitiveStride;
		int l = this.vertices / this.mode.primitiveStride;
		Vector3f[] vector3fs = new Vector3f[l];

		for (int m = 0; m < l; m++) {
			float f = floatBuffer.get(i + m * k + 0);
			float g = floatBuffer.get(i + m * k + 1);
			float h = floatBuffer.get(i + m * k + 2);
			float n = floatBuffer.get(i + m * k + j * 2 + 0);
			float o = floatBuffer.get(i + m * k + j * 2 + 1);
			float p = floatBuffer.get(i + m * k + j * 2 + 2);
			float q = (f + n) / 2.0F;
			float r = (g + o) / 2.0F;
			float s = (h + p) / 2.0F;
			vector3fs[m] = new Vector3f(q, r, s);
		}

		return vector3fs;
	}

	private void putSortedQuadIndices(VertexFormat.IndexType indexType) {
		if (this.sortingPoints != null && this.sorting != null) {
			int[] is = this.sorting.sort(this.sortingPoints);
			IntConsumer intConsumer = this.intConsumer(this.nextElementByte, indexType);

			for (int i : is) {
				intConsumer.accept(i * this.mode.primitiveStride + 0);
				intConsumer.accept(i * this.mode.primitiveStride + 1);
				intConsumer.accept(i * this.mode.primitiveStride + 2);
				intConsumer.accept(i * this.mode.primitiveStride + 2);
				intConsumer.accept(i * this.mode.primitiveStride + 3);
				intConsumer.accept(i * this.mode.primitiveStride + 0);
			}
		} else {
			throw new IllegalStateException("Sorting state uninitialized");
		}
	}

	public boolean isCurrentBatchEmpty() {
		return this.vertices == 0;
	}

	@Nullable
	public BufferBuilder.RenderedBuffer endOrDiscardIfEmpty() {
		this.ensureDrawing();
		if (this.isCurrentBatchEmpty()) {
			this.reset();
			return null;
		} else {
			BufferBuilder.RenderedBuffer renderedBuffer = this.storeRenderedBuffer();
			this.reset();
			return renderedBuffer;
		}
	}

	public BufferBuilder.RenderedBuffer end() {
		this.ensureDrawing();
		BufferBuilder.RenderedBuffer renderedBuffer = this.storeRenderedBuffer();
		this.reset();
		return renderedBuffer;
	}

	private void ensureDrawing() {
		if (!this.building) {
			throw new IllegalStateException("Not building!");
		}
	}

	private BufferBuilder.RenderedBuffer storeRenderedBuffer() {
		int i = this.mode.indexCount(this.vertices);
		int j = !this.indexOnly ? this.vertices * this.format.getVertexSize() : 0;
		VertexFormat.IndexType indexType = VertexFormat.IndexType.least(this.vertices);
		boolean bl;
		int l;
		if (this.sortingPoints != null) {
			int k = Mth.roundToward(i * indexType.bytes, 4);
			this.ensureCapacity(k);
			this.putSortedQuadIndices(indexType);
			bl = false;
			this.nextElementByte += k;
			l = j + k;
		} else {
			bl = true;
			l = j;
		}

		int k = this.renderedBufferPointer;
		this.renderedBufferPointer += l;
		this.renderedBufferCount++;
		BufferBuilder.DrawState drawState = new BufferBuilder.DrawState(this.format, this.vertices, i, this.mode, indexType, this.indexOnly, bl);
		return new BufferBuilder.RenderedBuffer(k, drawState);
	}

	private void reset() {
		this.building = false;
		this.vertices = 0;
		this.currentElement = null;
		this.elementIndex = 0;
		this.sortingPoints = null;
		this.sorting = null;
		this.indexOnly = false;
	}

	@Override
	public void putByte(int i, byte b) {
		this.buffer.put(this.nextElementByte + i, b);
	}

	@Override
	public void putShort(int i, short s) {
		this.buffer.putShort(this.nextElementByte + i, s);
	}

	@Override
	public void putFloat(int i, float f) {
		this.buffer.putFloat(this.nextElementByte + i, f);
	}

	@Override
	public void endVertex() {
		if (this.elementIndex != 0) {
			throw new IllegalStateException("Not filled all elements of the vertex");
		} else {
			this.vertices++;
			this.ensureVertexCapacity();
			if (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP) {
				int i = this.format.getVertexSize();
				this.buffer.put(this.nextElementByte, this.buffer, this.nextElementByte - i, i);
				this.nextElementByte += i;
				this.vertices++;
				this.ensureVertexCapacity();
			}
		}
	}

	@Override
	public void nextElement() {
		ImmutableList<VertexFormatElement> immutableList = this.format.getElements();
		this.elementIndex = (this.elementIndex + 1) % immutableList.size();
		this.nextElementByte = this.nextElementByte + this.currentElement.getByteSize();
		VertexFormatElement vertexFormatElement = (VertexFormatElement)immutableList.get(this.elementIndex);
		this.currentElement = vertexFormatElement;
		if (vertexFormatElement.getUsage() == VertexFormatElement.Usage.PADDING) {
			this.nextElement();
		}

		if (this.defaultColorSet && this.currentElement.getUsage() == VertexFormatElement.Usage.COLOR) {
			BufferVertexConsumer.super.color(this.defaultR, this.defaultG, this.defaultB, this.defaultA);
		}
	}

	@Override
	public VertexConsumer color(int i, int j, int k, int l) {
		if (this.defaultColorSet) {
			throw new IllegalStateException();
		} else {
			return BufferVertexConsumer.super.color(i, j, k, l);
		}
	}

	@Override
	public void vertex(float f, float g, float h, float i, float j, float k, float l, float m, float n, int o, int p, float q, float r, float s) {
		if (this.defaultColorSet) {
			throw new IllegalStateException();
		} else if (this.fastFormat) {
			this.putFloat(0, f);
			this.putFloat(4, g);
			this.putFloat(8, h);
			this.putByte(12, (byte)((int)(i * 255.0F)));
			this.putByte(13, (byte)((int)(j * 255.0F)));
			this.putByte(14, (byte)((int)(k * 255.0F)));
			this.putByte(15, (byte)((int)(l * 255.0F)));
			this.putFloat(16, m);
			this.putFloat(20, n);
			int t;
			if (this.fullFormat) {
				this.putShort(24, (short)(o & 65535));
				this.putShort(26, (short)(o >> 16 & 65535));
				t = 28;
			} else {
				t = 24;
			}

			this.putShort(t + 0, (short)(p & 65535));
			this.putShort(t + 2, (short)(p >> 16 & 65535));
			this.putByte(t + 4, BufferVertexConsumer.normalIntValue(q));
			this.putByte(t + 5, BufferVertexConsumer.normalIntValue(r));
			this.putByte(t + 6, BufferVertexConsumer.normalIntValue(s));
			this.nextElementByte += t + 8;
			this.endVertex();
		} else {
			super.vertex(f, g, h, i, j, k, l, m, n, o, p, q, r, s);
		}
	}

	void releaseRenderedBuffer() {
		if (this.renderedBufferCount > 0 && --this.renderedBufferCount == 0) {
			this.clear();
		}
	}

	public void clear() {
		if (this.renderedBufferCount > 0) {
			LOGGER.warn("Clearing BufferBuilder with unused batches");
		}

		this.discard();
	}

	public void discard() {
		this.renderedBufferCount = 0;
		this.renderedBufferPointer = 0;
		this.nextElementByte = 0;
	}

	public void release() {
		if (this.renderedBufferCount > 0) {
			throw new IllegalStateException("BufferBuilder closed with unused batches");
		} else if (this.building) {
			throw new IllegalStateException("Cannot close BufferBuilder while it is building");
		} else if (!this.closed) {
			this.closed = true;
			MemoryTracker.free(this.buffer);
		}
	}

	@Override
	public VertexFormatElement currentElement() {
		if (this.currentElement == null) {
			throw new IllegalStateException("BufferBuilder not started");
		} else {
			return this.currentElement;
		}
	}

	public boolean building() {
		return this.building;
	}

	ByteBuffer bufferSlice(int i, int j) {
		return MemoryUtil.memSlice(this.buffer, i, j - i);
	}

	@Environment(EnvType.CLIENT)
	public static record DrawState(
		VertexFormat format, int vertexCount, int indexCount, VertexFormat.Mode mode, VertexFormat.IndexType indexType, boolean indexOnly, boolean sequentialIndex
	) {

		public int vertexBufferSize() {
			return this.vertexCount * this.format.getVertexSize();
		}

		public int vertexBufferStart() {
			return 0;
		}

		public int vertexBufferEnd() {
			return this.vertexBufferSize();
		}

		public int indexBufferStart() {
			return this.indexOnly ? 0 : this.vertexBufferEnd();
		}

		public int indexBufferEnd() {
			return this.indexBufferStart() + this.indexBufferSize();
		}

		private int indexBufferSize() {
			return this.sequentialIndex ? 0 : this.indexCount * this.indexType.bytes;
		}

		public int bufferSize() {
			return this.indexBufferEnd();
		}
	}

	@Environment(EnvType.CLIENT)
	public class RenderedBuffer {
		private final int pointer;
		private final BufferBuilder.DrawState drawState;
		private boolean released;

		RenderedBuffer(final int i, final BufferBuilder.DrawState drawState) {
			this.pointer = i;
			this.drawState = drawState;
		}

		@Nullable
		public ByteBuffer vertexBuffer() {
			if (this.drawState.indexOnly()) {
				return null;
			} else {
				int i = this.pointer + this.drawState.vertexBufferStart();
				int j = this.pointer + this.drawState.vertexBufferEnd();
				return BufferBuilder.this.bufferSlice(i, j);
			}
		}

		@Nullable
		public ByteBuffer indexBuffer() {
			if (this.drawState.sequentialIndex()) {
				return null;
			} else {
				int i = this.pointer + this.drawState.indexBufferStart();
				int j = this.pointer + this.drawState.indexBufferEnd();
				return BufferBuilder.this.bufferSlice(i, j);
			}
		}

		public BufferBuilder.DrawState drawState() {
			return this.drawState;
		}

		public boolean isEmpty() {
			return this.drawState.vertexCount == 0;
		}

		public void release() {
			if (this.released) {
				throw new IllegalStateException("Buffer has already been released!");
			} else {
				BufferBuilder.this.releaseRenderedBuffer();
				this.released = true;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static class SortState {
		final VertexFormat.Mode mode;
		final int vertices;
		@Nullable
		final Vector3f[] sortingPoints;
		@Nullable
		final VertexSorting sorting;

		SortState(VertexFormat.Mode mode, int i, @Nullable Vector3f[] vector3fs, @Nullable VertexSorting vertexSorting) {
			this.mode = mode;
			this.vertices = i;
			this.sortingPoints = vector3fs;
			this.sorting = vertexSorting;
		}
	}
}
