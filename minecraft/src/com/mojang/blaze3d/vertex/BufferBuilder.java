package com.mojang.blaze3d.vertex;

import com.google.common.primitives.Floats;
import com.mojang.blaze3d.platform.MemoryTracker;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.BitSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class BufferBuilder {
	private static final Logger LOGGER = LogManager.getLogger();
	private ByteBuffer buffer;
	private IntBuffer intBuffer;
	private ShortBuffer shortBuffer;
	private FloatBuffer floatBuffer;
	private int vertices;
	private VertexFormatElement currentElement;
	private int elementIndex;
	private boolean noColor;
	private int mode;
	private double xo;
	private double yo;
	private double zo;
	private VertexFormat format;
	private boolean building;

	public BufferBuilder(int i) {
		this.buffer = MemoryTracker.createByteBuffer(i * 4);
		this.intBuffer = this.buffer.asIntBuffer();
		this.shortBuffer = this.buffer.asShortBuffer();
		this.floatBuffer = this.buffer.asFloatBuffer();
	}

	private void ensureCapacity(int i) {
		if (this.vertices * this.format.getVertexSize() + i > this.buffer.capacity()) {
			int j = this.buffer.capacity();
			int k = j + roundUp(i);
			LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", j, k);
			int l = this.intBuffer.position();
			ByteBuffer byteBuffer = MemoryTracker.createByteBuffer(k);
			this.buffer.position(0);
			byteBuffer.put(this.buffer);
			byteBuffer.rewind();
			this.buffer = byteBuffer;
			this.floatBuffer = this.buffer.asFloatBuffer().asReadOnlyBuffer();
			this.intBuffer = this.buffer.asIntBuffer();
			this.intBuffer.position(l);
			this.shortBuffer = this.buffer.asShortBuffer();
			this.shortBuffer.position(l << 1);
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
		int i = this.vertices / 4;
		float[] fs = new float[i];

		for (int j = 0; j < i; j++) {
			fs[j] = getQuadDistanceFromPlayer(
				this.floatBuffer,
				(float)((double)f + this.xo),
				(float)((double)g + this.yo),
				(float)((double)h + this.zo),
				this.format.getIntegerSize(),
				j * this.format.getVertexSize()
			);
		}

		Integer[] integers = new Integer[i];

		for (int k = 0; k < integers.length; k++) {
			integers[k] = k;
		}

		Arrays.sort(integers, (integer, integer2) -> Floats.compare(fs[integer2], fs[integer]));
		BitSet bitSet = new BitSet();
		int l = this.format.getVertexSize();
		int[] is = new int[l];

		for (int m = bitSet.nextClearBit(0); m < integers.length; m = bitSet.nextClearBit(m + 1)) {
			int n = integers[m];
			if (n != m) {
				this.intBuffer.limit(n * l + l);
				this.intBuffer.position(n * l);
				this.intBuffer.get(is);
				int o = n;

				for (int p = integers[n]; o != m; p = integers[p]) {
					this.intBuffer.limit(p * l + l);
					this.intBuffer.position(p * l);
					IntBuffer intBuffer = this.intBuffer.slice();
					this.intBuffer.limit(o * l + l);
					this.intBuffer.position(o * l);
					this.intBuffer.put(intBuffer);
					bitSet.set(o);
					o = p;
				}

				this.intBuffer.limit(m * l + l);
				this.intBuffer.position(m * l);
				this.intBuffer.put(is);
			}

			bitSet.set(m);
		}
	}

	public BufferBuilder.State getState() {
		this.intBuffer.rewind();
		int i = this.getBufferIndex();
		this.intBuffer.limit(i);
		int[] is = new int[i];
		this.intBuffer.get(is);
		this.intBuffer.limit(this.intBuffer.capacity());
		this.intBuffer.position(i);
		return new BufferBuilder.State(is, new VertexFormat(this.format));
	}

	private int getBufferIndex() {
		return this.vertices * this.format.getIntegerSize();
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
		this.intBuffer.clear();
		this.ensureCapacity(state.array().length * 4);
		this.intBuffer.put(state.array());
		this.vertices = state.vertices();
		this.format = new VertexFormat(state.getFormat());
	}

	public void clear() {
		this.vertices = 0;
		this.currentElement = null;
		this.elementIndex = 0;
	}

	public void begin(int i, VertexFormat vertexFormat) {
		if (this.building) {
			throw new IllegalStateException("Already building!");
		} else {
			this.building = true;
			this.clear();
			this.mode = i;
			this.format = vertexFormat;
			this.currentElement = vertexFormat.getElement(this.elementIndex);
			this.noColor = false;
			this.buffer.limit(this.buffer.capacity());
		}
	}

	public BufferBuilder uv(double d, double e) {
		int i = this.vertices * this.format.getVertexSize() + this.format.getOffset(this.elementIndex);
		switch (this.currentElement.getType()) {
			case FLOAT:
				this.buffer.putFloat(i, (float)d);
				this.buffer.putFloat(i + 4, (float)e);
				break;
			case UINT:
			case INT:
				this.buffer.putInt(i, (int)d);
				this.buffer.putInt(i + 4, (int)e);
				break;
			case USHORT:
			case SHORT:
				this.buffer.putShort(i, (short)((int)e));
				this.buffer.putShort(i + 2, (short)((int)d));
				break;
			case UBYTE:
			case BYTE:
				this.buffer.put(i, (byte)((int)e));
				this.buffer.put(i + 1, (byte)((int)d));
		}

		this.nextElement();
		return this;
	}

	public BufferBuilder uv2(int i, int j) {
		int k = this.vertices * this.format.getVertexSize() + this.format.getOffset(this.elementIndex);
		switch (this.currentElement.getType()) {
			case FLOAT:
				this.buffer.putFloat(k, (float)i);
				this.buffer.putFloat(k + 4, (float)j);
				break;
			case UINT:
			case INT:
				this.buffer.putInt(k, i);
				this.buffer.putInt(k + 4, j);
				break;
			case USHORT:
			case SHORT:
				this.buffer.putShort(k, (short)j);
				this.buffer.putShort(k + 2, (short)i);
				break;
			case UBYTE:
			case BYTE:
				this.buffer.put(k, (byte)j);
				this.buffer.put(k + 1, (byte)i);
		}

		this.nextElement();
		return this;
	}

	public void faceTex2(int i, int j, int k, int l) {
		int m = (this.vertices - 4) * this.format.getIntegerSize() + this.format.getUvOffset(1) / 4;
		int n = this.format.getVertexSize() >> 2;
		this.intBuffer.put(m, i);
		this.intBuffer.put(m + n, j);
		this.intBuffer.put(m + n * 2, k);
		this.intBuffer.put(m + n * 3, l);
	}

	public void postProcessFacePosition(double d, double e, double f) {
		int i = this.format.getIntegerSize();
		int j = (this.vertices - 4) * i;

		for (int k = 0; k < 4; k++) {
			int l = j + k * i;
			int m = l + 1;
			int n = m + 1;
			this.intBuffer.put(l, Float.floatToRawIntBits((float)(d + this.xo) + Float.intBitsToFloat(this.intBuffer.get(l))));
			this.intBuffer.put(m, Float.floatToRawIntBits((float)(e + this.yo) + Float.intBitsToFloat(this.intBuffer.get(m))));
			this.intBuffer.put(n, Float.floatToRawIntBits((float)(f + this.zo) + Float.intBitsToFloat(this.intBuffer.get(n))));
		}
	}

	private int getStartingColorIndex(int i) {
		return ((this.vertices - i) * this.format.getVertexSize() + this.format.getColorOffset()) / 4;
	}

	public void faceTint(float f, float g, float h, int i) {
		int j = this.getStartingColorIndex(i);
		int k = -1;
		if (!this.noColor) {
			k = this.intBuffer.get(j);
			if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
				int l = (int)((float)(k & 0xFF) * f);
				int m = (int)((float)(k >> 8 & 0xFF) * g);
				int n = (int)((float)(k >> 16 & 0xFF) * h);
				k &= -16777216;
				k |= n << 16 | m << 8 | l;
			} else {
				int l = (int)((float)(k >> 24 & 0xFF) * f);
				int m = (int)((float)(k >> 16 & 0xFF) * g);
				int n = (int)((float)(k >> 8 & 0xFF) * h);
				k &= 255;
				k |= l << 24 | m << 16 | n << 8;
			}
		}

		this.intBuffer.put(j, k);
	}

	private void fixupVertexColor(int i, int j) {
		int k = this.getStartingColorIndex(j);
		int l = i >> 16 & 0xFF;
		int m = i >> 8 & 0xFF;
		int n = i & 0xFF;
		this.putColor(k, l, m, n);
	}

	public void fixupVertexColor(float f, float g, float h, int i) {
		int j = this.getStartingColorIndex(i);
		int k = clamp((int)(f * 255.0F), 0, 255);
		int l = clamp((int)(g * 255.0F), 0, 255);
		int m = clamp((int)(h * 255.0F), 0, 255);
		this.putColor(j, k, l, m);
	}

	private static int clamp(int i, int j, int k) {
		if (i < j) {
			return j;
		} else {
			return i > k ? k : i;
		}
	}

	private void putColor(int i, int j, int k, int l) {
		if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
			this.intBuffer.put(i, 0xFF000000 | l << 16 | k << 8 | j);
		} else {
			this.intBuffer.put(i, j << 24 | k << 16 | l << 8 | 0xFF);
		}
	}

	public void noColor() {
		this.noColor = true;
	}

	public BufferBuilder color(float f, float g, float h, float i) {
		return this.color((int)(f * 255.0F), (int)(g * 255.0F), (int)(h * 255.0F), (int)(i * 255.0F));
	}

	public BufferBuilder color(int i, int j, int k, int l) {
		if (this.noColor) {
			return this;
		} else {
			int m = this.vertices * this.format.getVertexSize() + this.format.getOffset(this.elementIndex);
			switch (this.currentElement.getType()) {
				case FLOAT:
					this.buffer.putFloat(m, (float)i / 255.0F);
					this.buffer.putFloat(m + 4, (float)j / 255.0F);
					this.buffer.putFloat(m + 8, (float)k / 255.0F);
					this.buffer.putFloat(m + 12, (float)l / 255.0F);
					break;
				case UINT:
				case INT:
					this.buffer.putFloat(m, (float)i);
					this.buffer.putFloat(m + 4, (float)j);
					this.buffer.putFloat(m + 8, (float)k);
					this.buffer.putFloat(m + 12, (float)l);
					break;
				case USHORT:
				case SHORT:
					this.buffer.putShort(m, (short)i);
					this.buffer.putShort(m + 2, (short)j);
					this.buffer.putShort(m + 4, (short)k);
					this.buffer.putShort(m + 6, (short)l);
					break;
				case UBYTE:
				case BYTE:
					if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
						this.buffer.put(m, (byte)i);
						this.buffer.put(m + 1, (byte)j);
						this.buffer.put(m + 2, (byte)k);
						this.buffer.put(m + 3, (byte)l);
					} else {
						this.buffer.put(m, (byte)l);
						this.buffer.put(m + 1, (byte)k);
						this.buffer.put(m + 2, (byte)j);
						this.buffer.put(m + 3, (byte)i);
					}
			}

			this.nextElement();
			return this;
		}
	}

	public void putBulkData(int[] is) {
		this.ensureCapacity(is.length * 4 + this.format.getVertexSize());
		this.intBuffer.position(this.getBufferIndex());
		this.intBuffer.put(is);
		this.vertices = this.vertices + is.length / this.format.getIntegerSize();
	}

	public void endVertex() {
		this.vertices++;
		this.ensureCapacity(this.format.getVertexSize());
	}

	public BufferBuilder vertex(double d, double e, double f) {
		int i = this.vertices * this.format.getVertexSize() + this.format.getOffset(this.elementIndex);
		switch (this.currentElement.getType()) {
			case FLOAT:
				this.buffer.putFloat(i, (float)(d + this.xo));
				this.buffer.putFloat(i + 4, (float)(e + this.yo));
				this.buffer.putFloat(i + 8, (float)(f + this.zo));
				break;
			case UINT:
			case INT:
				this.buffer.putInt(i, Float.floatToRawIntBits((float)(d + this.xo)));
				this.buffer.putInt(i + 4, Float.floatToRawIntBits((float)(e + this.yo)));
				this.buffer.putInt(i + 8, Float.floatToRawIntBits((float)(f + this.zo)));
				break;
			case USHORT:
			case SHORT:
				this.buffer.putShort(i, (short)((int)(d + this.xo)));
				this.buffer.putShort(i + 2, (short)((int)(e + this.yo)));
				this.buffer.putShort(i + 4, (short)((int)(f + this.zo)));
				break;
			case UBYTE:
			case BYTE:
				this.buffer.put(i, (byte)((int)(d + this.xo)));
				this.buffer.put(i + 1, (byte)((int)(e + this.yo)));
				this.buffer.put(i + 2, (byte)((int)(f + this.zo)));
		}

		this.nextElement();
		return this;
	}

	public void postNormal(float f, float g, float h) {
		int i = (byte)((int)(f * 127.0F)) & 255;
		int j = (byte)((int)(g * 127.0F)) & 255;
		int k = (byte)((int)(h * 127.0F)) & 255;
		int l = i | j << 8 | k << 16;
		int m = this.format.getVertexSize() >> 2;
		int n = (this.vertices - 4) * m + this.format.getNormalOffset() / 4;
		this.intBuffer.put(n, l);
		this.intBuffer.put(n + m, l);
		this.intBuffer.put(n + m * 2, l);
		this.intBuffer.put(n + m * 3, l);
	}

	private void nextElement() {
		this.elementIndex++;
		this.elementIndex = this.elementIndex % this.format.getElementCount();
		this.currentElement = this.format.getElement(this.elementIndex);
		if (this.currentElement.getUsage() == VertexFormatElement.Usage.PADDING) {
			this.nextElement();
		}
	}

	public BufferBuilder normal(float f, float g, float h) {
		int i = this.vertices * this.format.getVertexSize() + this.format.getOffset(this.elementIndex);
		switch (this.currentElement.getType()) {
			case FLOAT:
				this.buffer.putFloat(i, f);
				this.buffer.putFloat(i + 4, g);
				this.buffer.putFloat(i + 8, h);
				break;
			case UINT:
			case INT:
				this.buffer.putInt(i, (int)f);
				this.buffer.putInt(i + 4, (int)g);
				this.buffer.putInt(i + 8, (int)h);
				break;
			case USHORT:
			case SHORT:
				this.buffer.putShort(i, (short)((int)f * 32767 & 65535));
				this.buffer.putShort(i + 2, (short)((int)g * 32767 & 65535));
				this.buffer.putShort(i + 4, (short)((int)h * 32767 & 65535));
				break;
			case UBYTE:
			case BYTE:
				this.buffer.put(i, (byte)((int)f * 127 & 0xFF));
				this.buffer.put(i + 1, (byte)((int)g * 127 & 0xFF));
				this.buffer.put(i + 2, (byte)((int)h * 127 & 0xFF));
		}

		this.nextElement();
		return this;
	}

	public void offset(double d, double e, double f) {
		this.xo = d;
		this.yo = e;
		this.zo = f;
	}

	public void end() {
		if (!this.building) {
			throw new IllegalStateException("Not building!");
		} else {
			this.building = false;
			this.buffer.position(0);
			this.buffer.limit(this.getBufferIndex() * 4);
		}
	}

	public ByteBuffer getBuffer() {
		return this.buffer;
	}

	public VertexFormat getVertexFormat() {
		return this.format;
	}

	public int getVertexCount() {
		return this.vertices;
	}

	public int getDrawMode() {
		return this.mode;
	}

	public void fixupQuadColor(int i) {
		for (int j = 0; j < 4; j++) {
			this.fixupVertexColor(i, j + 1);
		}
	}

	public void fixupQuadColor(float f, float g, float h) {
		for (int i = 0; i < 4; i++) {
			this.fixupVertexColor(f, g, h, i + 1);
		}
	}

	@Environment(EnvType.CLIENT)
	public class State {
		private final int[] array;
		private final VertexFormat format;

		public State(int[] is, VertexFormat vertexFormat) {
			this.array = is;
			this.format = vertexFormat;
		}

		public int[] array() {
			return this.array;
		}

		public int vertices() {
			return this.array.length / this.format.getIntegerSize();
		}

		public VertexFormat getFormat() {
			return this.format;
		}
	}
}
