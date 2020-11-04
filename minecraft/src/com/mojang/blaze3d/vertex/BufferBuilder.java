package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class BufferBuilder extends DefaultedVertexConsumer implements BufferVertexConsumer {
	private static final Logger LOGGER = LogManager.getLogger();
	private ByteBuffer buffer;
	private final List<BufferBuilder.DrawState> drawStates = Lists.<BufferBuilder.DrawState>newArrayList();
	private int lastPoppedStateIndex;
	private int totalRenderedBytes;
	private int nextElementByte;
	private int totalUploadedBytes;
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
	private float sortX = Float.NaN;
	private float sortY = Float.NaN;
	private float sortZ = Float.NaN;
	private boolean indexOnly;

	public BufferBuilder(int i) {
		this.buffer = MemoryTracker.createByteBuffer(i * 6);
	}

	private void ensureVertexCapacity() {
		this.ensureCapacity(this.format.getVertexSize());
	}

	private void ensureCapacity(int i) {
		if (this.nextElementByte + i > this.buffer.capacity()) {
			int j = this.buffer.capacity();
			int k = j + roundUp(i);
			LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", j, k);
			ByteBuffer byteBuffer = MemoryTracker.createByteBuffer(k);
			this.buffer.position(0);
			byteBuffer.put(this.buffer);
			byteBuffer.rewind();
			this.buffer = byteBuffer;
		}
	}

	private static int roundUp(int i) {
		int j = 2097152;
		if (i == 0) {
			return j;
		} else {
			if (i < 0) {
				j *= -1;
			}

			int k = i % j;
			return k == 0 ? i : i + j - k;
		}
	}

	public void setQuadSortOrigin(float f, float g, float h) {
		if (this.mode == VertexFormat.Mode.QUADS) {
			if (this.sortX != f || this.sortY != g || this.sortZ != h) {
				this.sortX = f;
				this.sortY = g;
				this.sortZ = h;
				if (this.sortingPoints == null) {
					this.sortingPoints = this.makeQuadSortingPoints();
				}
			}
		}
	}

	public BufferBuilder.SortState getSortState() {
		return new BufferBuilder.SortState(this.mode, this.vertices, this.sortingPoints, this.sortX, this.sortY, this.sortZ);
	}

	public void restoreSortState(BufferBuilder.SortState sortState) {
		this.buffer.clear();
		this.mode = sortState.mode;
		this.vertices = sortState.vertices;
		this.nextElementByte = this.totalRenderedBytes;
		this.sortingPoints = sortState.sortingPoints;
		this.sortX = sortState.sortX;
		this.sortY = sortState.sortY;
		this.sortZ = sortState.sortZ;
		this.indexOnly = true;
	}

	public void begin(VertexFormat.Mode mode, VertexFormat vertexFormat) {
		if (this.building) {
			throw new IllegalStateException("Already building!");
		} else {
			this.building = true;
			this.mode = mode;
			this.switchFormat(vertexFormat);
			this.currentElement = (VertexFormatElement)vertexFormat.getElements().get(0);
			this.elementIndex = 0;
			this.buffer.clear();
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

	private IntConsumer intConsumer(VertexFormat.IndexType indexType) {
		switch (indexType) {
			case BYTE:
				return i -> this.buffer.put((byte)i);
			case SHORT:
				return i -> this.buffer.putShort((short)i);
			case INT:
			default:
				return i -> this.buffer.putInt(i);
		}
	}

	private Vector3f[] makeQuadSortingPoints() {
		FloatBuffer floatBuffer = this.buffer.asFloatBuffer();
		int i = this.totalRenderedBytes / 4;
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
		float[] fs = new float[this.sortingPoints.length];
		int[] is = new int[this.sortingPoints.length];

		for (int i = 0; i < this.sortingPoints.length; is[i] = i++) {
			float f = this.sortingPoints[i].x() - this.sortX;
			float g = this.sortingPoints[i].y() - this.sortY;
			float h = this.sortingPoints[i].z() - this.sortZ;
			fs[i] = f * f + g * g + h * h;
		}

		IntArrays.mergeSort(is, (i, jx) -> Floats.compare(fs[jx], fs[i]));
		IntConsumer intConsumer = this.intConsumer(indexType);
		this.buffer.position(this.nextElementByte);

		for (int j : is) {
			intConsumer.accept(j * this.mode.primitiveStride + 0);
			intConsumer.accept(j * this.mode.primitiveStride + 1);
			intConsumer.accept(j * this.mode.primitiveStride + 2);
			intConsumer.accept(j * this.mode.primitiveStride + 2);
			intConsumer.accept(j * this.mode.primitiveStride + 3);
			intConsumer.accept(j * this.mode.primitiveStride + 0);
		}
	}

	public void end() {
		if (!this.building) {
			throw new IllegalStateException("Not building!");
		} else {
			int i = this.mode.indexCount(this.vertices);
			VertexFormat.IndexType indexType = VertexFormat.IndexType.least(i);
			boolean bl;
			if (this.sortingPoints != null) {
				int j = Mth.roundToward(i * indexType.bytes, 4);
				this.ensureCapacity(j);
				this.putSortedQuadIndices(indexType);
				bl = false;
				this.nextElementByte += j;
				this.totalRenderedBytes = this.totalRenderedBytes + this.vertices * this.format.getVertexSize() + j;
			} else {
				bl = true;
				this.totalRenderedBytes = this.totalRenderedBytes + this.vertices * this.format.getVertexSize();
			}

			this.building = false;
			this.drawStates.add(new BufferBuilder.DrawState(this.format, this.vertices, i, this.mode, indexType, this.indexOnly, bl));
			this.vertices = 0;
			this.currentElement = null;
			this.elementIndex = 0;
			this.sortingPoints = null;
			this.sortX = Float.NaN;
			this.sortY = Float.NaN;
			this.sortZ = Float.NaN;
			this.indexOnly = false;
		}
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

	public Pair<BufferBuilder.DrawState, ByteBuffer> popNextBuffer() {
		BufferBuilder.DrawState drawState = (BufferBuilder.DrawState)this.drawStates.get(this.lastPoppedStateIndex++);
		this.buffer.position(this.totalUploadedBytes);
		this.totalUploadedBytes = this.totalUploadedBytes + Mth.roundToward(drawState.bufferSize(), 4);
		this.buffer.limit(this.totalUploadedBytes);
		if (this.lastPoppedStateIndex == this.drawStates.size() && this.vertices == 0) {
			this.clear();
		}

		ByteBuffer byteBuffer = this.buffer.slice();
		this.buffer.clear();
		return Pair.of(drawState, byteBuffer);
	}

	public void clear() {
		if (this.totalRenderedBytes != this.totalUploadedBytes) {
			LOGGER.warn("Bytes mismatch {} {}", this.totalRenderedBytes, this.totalUploadedBytes);
		}

		this.discard();
	}

	public void discard() {
		this.totalRenderedBytes = 0;
		this.totalUploadedBytes = 0;
		this.nextElementByte = 0;
		this.drawStates.clear();
		this.lastPoppedStateIndex = 0;
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

	@Environment(EnvType.CLIENT)
	public static final class DrawState {
		private final VertexFormat format;
		private final int vertexCount;
		private final int indexCount;
		private final VertexFormat.Mode mode;
		private final VertexFormat.IndexType indexType;
		private final boolean indexOnly;
		private final boolean sequentialIndex;

		private DrawState(VertexFormat vertexFormat, int i, int j, VertexFormat.Mode mode, VertexFormat.IndexType indexType, boolean bl, boolean bl2) {
			this.format = vertexFormat;
			this.vertexCount = i;
			this.indexCount = j;
			this.mode = mode;
			this.indexType = indexType;
			this.indexOnly = bl;
			this.sequentialIndex = bl2;
		}

		public VertexFormat format() {
			return this.format;
		}

		public int vertexCount() {
			return this.vertexCount;
		}

		public int indexCount() {
			return this.indexCount;
		}

		public VertexFormat.Mode mode() {
			return this.mode;
		}

		public VertexFormat.IndexType indexType() {
			return this.indexType;
		}

		public int vertexBufferSize() {
			return this.vertexCount * this.format.getVertexSize();
		}

		private int indexBufferSize() {
			return this.sequentialIndex ? 0 : this.indexCount * this.indexType.bytes;
		}

		public int bufferSize() {
			return this.vertexBufferSize() + this.indexBufferSize();
		}

		public boolean indexOnly() {
			return this.indexOnly;
		}

		public boolean sequentialIndex() {
			return this.sequentialIndex;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class SortState {
		private final VertexFormat.Mode mode;
		private final int vertices;
		@Nullable
		private final Vector3f[] sortingPoints;
		private final float sortX;
		private final float sortY;
		private final float sortZ;

		private SortState(VertexFormat.Mode mode, int i, @Nullable Vector3f[] vector3fs, float f, float g, float h) {
			this.mode = mode;
			this.vertices = i;
			this.sortingPoints = vector3fs;
			this.sortX = f;
			this.sortY = g;
			this.sortZ = h;
		}
	}
}
