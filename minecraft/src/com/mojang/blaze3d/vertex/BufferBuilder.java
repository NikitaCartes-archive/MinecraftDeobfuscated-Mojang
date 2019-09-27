package com.mojang.blaze3d.vertex;

import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrays;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class BufferBuilder extends DefaultedVertexConsumer implements BufferVertexConsumer {
	private static final Logger LOGGER = LogManager.getLogger();
	private ByteBuffer buffer;
	private final List<BufferBuilder.DrawState> vertexCounts = Lists.<BufferBuilder.DrawState>newArrayList();
	private int lastRenderedCountIndex = 0;
	private int totalRenderedBytes = 0;
	private int nextElementByte = 0;
	private int totalUploadedBytes = 0;
	private int vertices;
	@Nullable
	private VertexFormatElement currentElement;
	private int elementIndex;
	private int mode;
	private VertexFormat format;
	private boolean building;

	public BufferBuilder(int i) {
		this.buffer = MemoryTracker.createByteBuffer(i * 4);
	}

	protected void ensureVertexCapacity() {
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

	public void sortQuads(float f, float g, float h) {
		this.buffer.clear();
		FloatBuffer floatBuffer = this.buffer.asFloatBuffer();
		int i = this.vertices / 4;
		float[] fs = new float[i];

		for (int j = 0; j < i; j++) {
			fs[j] = getQuadDistanceFromPlayer(floatBuffer, f, g, h, this.format.getIntegerSize(), this.totalRenderedBytes / 4 + j * this.format.getVertexSize());
		}

		int[] is = new int[i];
		int k = 0;

		while (k < is.length) {
			is[k] = k++;
		}

		IntArrays.quickSort(is, (ix, j) -> Floats.compare(fs[j], fs[ix]));
		BitSet bitSet = new BitSet();
		FloatBuffer floatBuffer2 = MemoryTracker.createFloatBuffer(this.format.getIntegerSize() * 4);

		for (int l = bitSet.nextClearBit(0); l < is.length; l = bitSet.nextClearBit(l + 1)) {
			int m = is[l];
			if (m != l) {
				this.limitToVertex(floatBuffer, m);
				floatBuffer2.clear();
				floatBuffer2.put(floatBuffer);
				int n = m;

				for (int o = is[m]; n != l; o = is[o]) {
					this.limitToVertex(floatBuffer, o);
					FloatBuffer floatBuffer3 = floatBuffer.slice();
					this.limitToVertex(floatBuffer, n);
					floatBuffer.put(floatBuffer3);
					bitSet.set(n);
					n = o;
				}

				this.limitToVertex(floatBuffer, l);
				floatBuffer2.flip();
				floatBuffer.put(floatBuffer2);
			}

			bitSet.set(l);
		}
	}

	private void limitToVertex(FloatBuffer floatBuffer, int i) {
		int j = this.format.getIntegerSize() * 4;
		floatBuffer.limit(this.totalRenderedBytes / 4 + (i + 1) * j);
		floatBuffer.position(this.totalRenderedBytes / 4 + i * j);
	}

	public BufferBuilder.State getState() {
		this.buffer.limit(this.nextElementByte);
		this.buffer.position(this.totalRenderedBytes);
		ByteBuffer byteBuffer = ByteBuffer.allocate(this.vertices * this.format.getVertexSize());
		byteBuffer.put(this.buffer);
		this.buffer.clear();
		return new BufferBuilder.State(byteBuffer, new VertexFormat(this.format));
	}

	private static float getQuadDistanceFromPlayer(FloatBuffer floatBuffer, float f, float g, float h, int i, int j) {
		float k = floatBuffer.get(j + i * 0 + 0);
		float l = floatBuffer.get(j + i * 0 + 1);
		float m = floatBuffer.get(j + i * 0 + 2);
		float n = floatBuffer.get(j + i * 1 + 0);
		float o = floatBuffer.get(j + i * 1 + 1);
		float p = floatBuffer.get(j + i * 1 + 2);
		float q = floatBuffer.get(j + i * 2 + 0);
		float r = floatBuffer.get(j + i * 2 + 1);
		float s = floatBuffer.get(j + i * 2 + 2);
		float t = floatBuffer.get(j + i * 3 + 0);
		float u = floatBuffer.get(j + i * 3 + 1);
		float v = floatBuffer.get(j + i * 3 + 2);
		float w = (k + n + q + t) * 0.25F - f;
		float x = (l + o + r + u) * 0.25F - g;
		float y = (m + p + s + v) * 0.25F - h;
		return w * w + x * x + y * y;
	}

	public void restoreState(BufferBuilder.State state) {
		state.data.clear();
		int i = state.data.capacity();
		this.ensureCapacity(i);
		this.buffer.limit(this.buffer.capacity());
		this.buffer.position(this.totalRenderedBytes);
		this.buffer.put(state.data);
		this.buffer.clear();
		this.format = new VertexFormat(state.format);
		this.vertices = i / this.format.getVertexSize();
		this.nextElementByte = this.totalRenderedBytes + this.vertices * this.format.getVertexSize();
	}

	public void begin(int i, VertexFormat vertexFormat) {
		if (this.building) {
			throw new IllegalStateException("Already building!");
		} else {
			this.building = true;
			this.mode = i;
			this.format = vertexFormat;
			this.currentElement = vertexFormat.getElement(0);
			this.elementIndex = 0;
			this.buffer.clear();
		}
	}

	public void end() {
		if (!this.building) {
			throw new IllegalStateException("Not building!");
		} else {
			this.building = false;
			this.vertexCounts.add(new BufferBuilder.DrawState(this.format, this.vertices, this.mode));
			this.totalRenderedBytes = this.totalRenderedBytes + this.vertices * this.format.getVertexSize();
			this.vertices = 0;
			this.currentElement = null;
			this.elementIndex = 0;
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
		this.elementIndex++;
		this.nextElementByte = this.nextElementByte + this.currentElement().getByteSize();
		this.elementIndex = this.elementIndex % this.format.getElementCount();
		this.currentElement = this.format.getElement(this.elementIndex);
		if (this.currentElement().getUsage() == VertexFormatElement.Usage.PADDING) {
			this.nextElement();
		}

		if (this.defaultColorSet && this.currentElement.getUsage() == VertexFormatElement.Usage.COLOR) {
			BufferVertexConsumer.super.color(this.defaultR, this.defaultG, this.defaultB, this.defaultA);
		}

		if (this.defaultOverlayCoordsSet && this.currentElement.getUsage() == VertexFormatElement.Usage.UV && this.currentElement.getIndex() == 1) {
			BufferVertexConsumer.super.overlayCoords(this.defaultOverlayU, this.defaultOverlayV);
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
	public VertexConsumer overlayCoords(int i, int j) {
		if (this.defaultOverlayCoordsSet) {
			throw new IllegalStateException();
		} else {
			return BufferVertexConsumer.super.overlayCoords(i, j);
		}
	}

	public Pair<BufferBuilder.DrawState, ByteBuffer> popNextBuffer() {
		BufferBuilder.DrawState drawState = (BufferBuilder.DrawState)this.vertexCounts.get(this.lastRenderedCountIndex++);
		this.buffer.position(this.totalUploadedBytes);
		this.totalUploadedBytes = this.totalUploadedBytes + drawState.vertexCount() * drawState.format().getVertexSize();
		this.buffer.limit(this.totalUploadedBytes);
		if (this.lastRenderedCountIndex == this.vertexCounts.size() && this.vertices == 0) {
			this.clear();
		}

		ByteBuffer byteBuffer = this.buffer.slice();
		this.buffer.clear();
		return Pair.of(drawState, byteBuffer);
	}

	public void clear() {
		if (this.totalRenderedBytes != this.totalUploadedBytes) {
			LOGGER.warn("Bytes mismatch " + this.totalRenderedBytes + " " + this.totalUploadedBytes);
		}

		this.totalRenderedBytes = 0;
		this.totalUploadedBytes = 0;
		this.nextElementByte = 0;
		this.vertexCounts.clear();
		this.lastRenderedCountIndex = 0;
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
		private final int mode;

		private DrawState(VertexFormat vertexFormat, int i, int j) {
			this.format = vertexFormat;
			this.vertexCount = i;
			this.mode = j;
		}

		public VertexFormat format() {
			return this.format;
		}

		public int vertexCount() {
			return this.vertexCount;
		}

		public int mode() {
			return this.mode;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class State {
		private final ByteBuffer data;
		private final VertexFormat format;

		private State(ByteBuffer byteBuffer, VertexFormat vertexFormat) {
			this.data = byteBuffer;
			this.format = vertexFormat;
		}
	}
}
