/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.primitives.Floats;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.IntArrays;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class BufferBuilder {
    private static final Logger LOGGER = LogManager.getLogger();
    private ByteBuffer buffer;
    private IntBuffer intBuffer;
    private FloatBuffer floatBuffer;
    private final List<DrawState> vertexCounts = Lists.newArrayList();
    private int lastRenderedCountIndex = 0;
    private int totalRenderedBytes = 0;
    private int totalUploadedBytes = 0;
    private int vertices;
    private VertexFormatElement currentElement;
    private int elementIndex;
    private boolean noColor;
    private int mode;
    private double xo;
    private double yo;
    private double zo;
    private final Deque<Matrix4f> poseStack = Util.make(Queues.newArrayDeque(), arrayDeque -> {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        arrayDeque.add(matrix4f);
    });
    private VertexFormat format;
    private boolean building;

    public BufferBuilder(int i) {
        this.buffer = MemoryTracker.createByteBuffer(i * 4);
        this.intBuffer = this.buffer.asIntBuffer();
        this.floatBuffer = this.buffer.asFloatBuffer().asReadOnlyBuffer();
    }

    private void ensureCapacity(int i) {
        if (this.totalRenderedBytes + this.vertices * this.format.getVertexSize() + i <= this.buffer.capacity()) {
            return;
        }
        int j = this.buffer.capacity();
        int k = j + BufferBuilder.roundUp(i);
        LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", (Object)j, (Object)k);
        ByteBuffer byteBuffer = MemoryTracker.createByteBuffer(k);
        this.buffer.position(0);
        byteBuffer.put(this.buffer);
        byteBuffer.rewind();
        this.buffer = byteBuffer;
        this.floatBuffer = this.buffer.asFloatBuffer().asReadOnlyBuffer();
        this.intBuffer = this.buffer.asIntBuffer();
    }

    private static int roundUp(int i) {
        int k;
        int j = 0x200000;
        if (i == 0) {
            return j;
        }
        if (i < 0) {
            j *= -1;
        }
        if ((k = i % j) == 0) {
            return i;
        }
        return i + j - k;
    }

    public void sortQuads(float f, float g, float h) {
        int i2 = this.vertices / 4;
        float[] fs = new float[i2];
        for (int j2 = 0; j2 < i2; ++j2) {
            fs[j2] = BufferBuilder.getQuadDistanceFromPlayer(this.floatBuffer, (float)((double)f + this.xo), (float)((double)g + this.yo), (float)((double)h + this.zo), this.format.getIntegerSize(), this.totalRenderedBytes / 4 + j2 * this.format.getVertexSize());
        }
        int[] is = new int[i2];
        for (int k = 0; k < is.length; ++k) {
            is[k] = k;
        }
        IntArrays.quickSort(is, (i, j) -> Floats.compare(fs[j], fs[i]));
        BitSet bitSet = new BitSet();
        int[] js = new int[this.format.getVertexSize()];
        int l = bitSet.nextClearBit(0);
        while (l < is.length) {
            int m = is[l];
            if (m != l) {
                this.limitToVertex(m);
                this.intBuffer.get(js);
                int n = m;
                int o = is[n];
                while (n != l) {
                    this.limitToVertex(o);
                    IntBuffer intBuffer = this.intBuffer.slice();
                    this.limitToVertex(n);
                    this.intBuffer.put(intBuffer);
                    bitSet.set(n);
                    n = o;
                    o = is[n];
                }
                this.limitToVertex(l);
                this.intBuffer.put(js);
            }
            bitSet.set(l);
            l = bitSet.nextClearBit(l + 1);
        }
    }

    private void limitToVertex(int i) {
        int j = this.format.getIntegerSize() * 4;
        this.intBuffer.limit(this.totalRenderedBytes / 4 + (i + 1) * j);
        this.intBuffer.position(this.totalRenderedBytes / 4 + i * j);
    }

    public State getState() {
        this.intBuffer.position(this.totalRenderedBytes / 4);
        int i = this.nextVertexIntPosition();
        this.intBuffer.limit(i);
        int[] is = new int[this.vertices * this.format.getIntegerSize()];
        this.intBuffer.get(is);
        return new State(is, new VertexFormat(this.format));
    }

    private int nextVertexIntPosition() {
        return this.totalRenderedBytes / 4 + this.vertices * this.format.getIntegerSize();
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
        float w = (k + n + q + t) * 0.25f - f;
        float x = (l + o + r + u) * 0.25f - g;
        float y = (m + p + s + v) * 0.25f - h;
        return w * w + x * x + y * y;
    }

    public void restoreState(State state) {
        this.vertices = 0;
        this.ensureCapacity(state.array().length * 4);
        this.intBuffer.limit(this.intBuffer.capacity());
        this.intBuffer.position(this.totalRenderedBytes / 4);
        this.intBuffer.put(state.array());
        this.vertices = state.vertices();
        this.format = new VertexFormat(state.getFormat());
    }

    public void begin(int i, VertexFormat vertexFormat) {
        if (this.building) {
            throw new IllegalStateException("Already building!");
        }
        this.building = true;
        this.mode = i;
        this.format = vertexFormat;
        this.currentElement = vertexFormat.getElement(this.elementIndex);
        this.noColor = false;
        this.buffer.limit(this.buffer.capacity());
    }

    public void end() {
        if (!this.building) {
            throw new IllegalStateException("Not building!");
        }
        this.building = false;
        this.vertexCounts.add(new DrawState(this.format, this.vertices, this.mode));
        this.totalRenderedBytes += this.vertices * this.format.getVertexSize();
        this.vertices = 0;
        this.currentElement = null;
        this.elementIndex = 0;
    }

    public BufferBuilder uv(double d, double e) {
        int i = this.getIndex();
        switch (this.currentElement.getType()) {
            case FLOAT: {
                this.buffer.putFloat(i, (float)d);
                this.buffer.putFloat(i + 4, (float)e);
                break;
            }
            case UINT: 
            case INT: {
                this.buffer.putInt(i, (int)d);
                this.buffer.putInt(i + 4, (int)e);
                break;
            }
            case USHORT: 
            case SHORT: {
                this.buffer.putShort(i, (short)e);
                this.buffer.putShort(i + 2, (short)d);
                break;
            }
            case UBYTE: 
            case BYTE: {
                this.buffer.put(i, (byte)e);
                this.buffer.put(i + 1, (byte)d);
            }
        }
        this.nextElement();
        return this;
    }

    public BufferBuilder uv2(int i, int j) {
        int k = this.getIndex();
        switch (this.currentElement.getType()) {
            case FLOAT: {
                this.buffer.putFloat(k, i);
                this.buffer.putFloat(k + 4, j);
                break;
            }
            case UINT: 
            case INT: {
                this.buffer.putInt(k, i);
                this.buffer.putInt(k + 4, j);
                break;
            }
            case USHORT: 
            case SHORT: {
                this.buffer.putShort(k, (short)j);
                this.buffer.putShort(k + 2, (short)i);
                break;
            }
            case UBYTE: 
            case BYTE: {
                this.buffer.put(k, (byte)j);
                this.buffer.put(k + 1, (byte)i);
            }
        }
        this.nextElement();
        return this;
    }

    public void faceTex2(int i, int j, int k, int l) {
        int m = this.totalRenderedBytes / 4 + (this.vertices - 4) * this.format.getIntegerSize() + this.format.getUvOffset(1) / 4;
        int n = this.format.getVertexSize() >> 2;
        this.intBuffer.put(m, i);
        this.intBuffer.put(m + n, j);
        this.intBuffer.put(m + n * 2, k);
        this.intBuffer.put(m + n * 3, l);
    }

    public void postProcessFacePosition(double d, double e, double f) {
        int i = this.format.getIntegerSize();
        int j = this.totalRenderedBytes / 4 + (this.vertices - 4) * i;
        for (int k = 0; k < 4; ++k) {
            int l = j + k * i;
            int m = l + 1;
            int n = m + 1;
            this.intBuffer.put(l, Float.floatToRawIntBits((float)(d + this.xo) + Float.intBitsToFloat(this.intBuffer.get(l))));
            this.intBuffer.put(m, Float.floatToRawIntBits((float)(e + this.yo) + Float.intBitsToFloat(this.intBuffer.get(m))));
            this.intBuffer.put(n, Float.floatToRawIntBits((float)(f + this.zo) + Float.intBitsToFloat(this.intBuffer.get(n))));
        }
    }

    private int getStartingColorIndex(int i) {
        return (this.totalRenderedBytes + (this.vertices - i) * this.format.getVertexSize() + this.format.getColorOffset()) / 4;
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
                k &= 0xFF000000;
                k |= n << 16 | m << 8 | l;
            } else {
                int l = (int)((float)(k >> 24 & 0xFF) * f);
                int m = (int)((float)(k >> 16 & 0xFF) * g);
                int n = (int)((float)(k >> 8 & 0xFF) * h);
                k &= 0xFF;
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
        int k = BufferBuilder.clamp((int)(f * 255.0f), 0, 255);
        int l = BufferBuilder.clamp((int)(g * 255.0f), 0, 255);
        int m = BufferBuilder.clamp((int)(h * 255.0f), 0, 255);
        this.putColor(j, k, l, m);
    }

    private static int clamp(int i, int j, int k) {
        if (i < j) {
            return j;
        }
        if (i > k) {
            return k;
        }
        return i;
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
        return this.color((int)(f * 255.0f), (int)(g * 255.0f), (int)(h * 255.0f), (int)(i * 255.0f));
    }

    public BufferBuilder color(int i, int j, int k, int l) {
        if (this.noColor) {
            this.nextElement();
            return this;
        }
        int m = this.getIndex();
        switch (this.currentElement.getType()) {
            case FLOAT: {
                this.buffer.putFloat(m, (float)i / 255.0f);
                this.buffer.putFloat(m + 4, (float)j / 255.0f);
                this.buffer.putFloat(m + 8, (float)k / 255.0f);
                this.buffer.putFloat(m + 12, (float)l / 255.0f);
                break;
            }
            case UINT: 
            case INT: {
                this.buffer.putFloat(m, i);
                this.buffer.putFloat(m + 4, j);
                this.buffer.putFloat(m + 8, k);
                this.buffer.putFloat(m + 12, l);
                break;
            }
            case USHORT: 
            case SHORT: {
                this.buffer.putShort(m, (short)i);
                this.buffer.putShort(m + 2, (short)j);
                this.buffer.putShort(m + 4, (short)k);
                this.buffer.putShort(m + 6, (short)l);
                break;
            }
            case UBYTE: 
            case BYTE: {
                if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                    this.buffer.put(m, (byte)i);
                    this.buffer.put(m + 1, (byte)j);
                    this.buffer.put(m + 2, (byte)k);
                    this.buffer.put(m + 3, (byte)l);
                    break;
                }
                this.buffer.put(m, (byte)l);
                this.buffer.put(m + 1, (byte)k);
                this.buffer.put(m + 2, (byte)j);
                this.buffer.put(m + 3, (byte)i);
            }
        }
        this.nextElement();
        return this;
    }

    private int getIndex() {
        return this.totalRenderedBytes + this.vertices * this.format.getVertexSize() + this.format.getOffset(this.elementIndex);
    }

    public void putBulkData(int[] is) {
        this.ensureCapacity(is.length * 4 + this.format.getVertexSize());
        this.intBuffer.limit(this.intBuffer.capacity());
        this.intBuffer.position(this.nextVertexIntPosition());
        this.intBuffer.put(is);
        this.vertices += is.length / this.format.getIntegerSize();
    }

    public void endVertex() {
        ++this.vertices;
        this.ensureCapacity(this.format.getVertexSize());
    }

    public BufferBuilder vertex(double d, double e, double f) {
        int i = this.getIndex();
        switch (this.currentElement.getType()) {
            case FLOAT: {
                this.buffer.putFloat(i, (float)(d + this.xo));
                this.buffer.putFloat(i + 4, (float)(e + this.yo));
                this.buffer.putFloat(i + 8, (float)(f + this.zo));
                break;
            }
            case UINT: 
            case INT: {
                this.buffer.putInt(i, Float.floatToRawIntBits((float)(d + this.xo)));
                this.buffer.putInt(i + 4, Float.floatToRawIntBits((float)(e + this.yo)));
                this.buffer.putInt(i + 8, Float.floatToRawIntBits((float)(f + this.zo)));
                break;
            }
            case USHORT: 
            case SHORT: {
                this.buffer.putShort(i, (short)(d + this.xo));
                this.buffer.putShort(i + 2, (short)(e + this.yo));
                this.buffer.putShort(i + 4, (short)(f + this.zo));
                break;
            }
            case UBYTE: 
            case BYTE: {
                this.buffer.put(i, (byte)(d + this.xo));
                this.buffer.put(i + 1, (byte)(e + this.yo));
                this.buffer.put(i + 2, (byte)(f + this.zo));
            }
        }
        this.nextElement();
        return this;
    }

    public void postNormal(float f, float g, float h) {
        int i = (byte)(f * 127.0f) & 0xFF;
        int j = (byte)(g * 127.0f) & 0xFF;
        int k = (byte)(h * 127.0f) & 0xFF;
        int l = i | j << 8 | k << 16;
        int m = this.format.getVertexSize() >> 2;
        int n = this.totalRenderedBytes / 4 + (this.vertices - 4) * m + this.format.getNormalOffset() / 4;
        this.intBuffer.put(n, l);
        this.intBuffer.put(n + m, l);
        this.intBuffer.put(n + m * 2, l);
        this.intBuffer.put(n + m * 3, l);
    }

    private void nextElement() {
        ++this.elementIndex;
        this.elementIndex %= this.format.getElementCount();
        this.currentElement = this.format.getElement(this.elementIndex);
        if (this.currentElement.getUsage() == VertexFormatElement.Usage.PADDING) {
            this.nextElement();
        }
    }

    public BufferBuilder normal(float f, float g, float h) {
        int i = this.getIndex();
        switch (this.currentElement.getType()) {
            case FLOAT: {
                this.buffer.putFloat(i, f);
                this.buffer.putFloat(i + 4, g);
                this.buffer.putFloat(i + 8, h);
                break;
            }
            case UINT: 
            case INT: {
                this.buffer.putInt(i, (int)f);
                this.buffer.putInt(i + 4, (int)g);
                this.buffer.putInt(i + 8, (int)h);
                break;
            }
            case USHORT: 
            case SHORT: {
                this.buffer.putShort(i, (short)((int)f * Short.MAX_VALUE & 0xFFFF));
                this.buffer.putShort(i + 2, (short)((int)g * Short.MAX_VALUE & 0xFFFF));
                this.buffer.putShort(i + 4, (short)((int)h * Short.MAX_VALUE & 0xFFFF));
                break;
            }
            case UBYTE: 
            case BYTE: {
                this.buffer.put(i, (byte)((int)f * 127 & 0xFF));
                this.buffer.put(i + 1, (byte)((int)g * 127 & 0xFF));
                this.buffer.put(i + 2, (byte)((int)h * 127 & 0xFF));
            }
        }
        this.nextElement();
        return this;
    }

    public void offset(double d, double e, double f) {
        this.xo = d;
        this.yo = e;
        this.zo = f;
    }

    public void translate(double d, double e, double f) {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        matrix4f.translate(new Vector3f((float)d, (float)e, (float)f));
        this.multiplyPose(matrix4f);
    }

    public void scale(float f, float g, float h) {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        matrix4f.set(0, 0, f);
        matrix4f.set(1, 1, g);
        matrix4f.set(2, 2, h);
        this.multiplyPose(matrix4f);
    }

    public void multiplyPose(Matrix4f matrix4f) {
        Matrix4f matrix4f2 = this.poseStack.getLast();
        matrix4f2.multiply(matrix4f);
    }

    public void multiplyPose(Quaternion quaternion) {
        Matrix4f matrix4f = this.poseStack.getLast();
        matrix4f.multiply(quaternion);
    }

    public void pushPose() {
        this.poseStack.addLast(this.poseStack.getLast().copy());
    }

    public void popPose() {
        this.poseStack.removeLast();
    }

    public Matrix4f getPose() {
        return this.poseStack.getLast();
    }

    public VertexFormat getVertexFormat() {
        return this.format;
    }

    public void fixupQuadColor(int i) {
        for (int j = 0; j < 4; ++j) {
            this.fixupVertexColor(i, j + 1);
        }
    }

    public void fixupQuadColor(float f, float g, float h) {
        for (int i = 0; i < 4; ++i) {
            this.fixupVertexColor(f, g, h, i + 1);
        }
    }

    public Pair<DrawState, ByteBuffer> popNextBuffer() {
        DrawState drawState = this.vertexCounts.get(this.lastRenderedCountIndex++);
        this.buffer.position(this.totalUploadedBytes);
        this.totalUploadedBytes += drawState.vertexCount() * drawState.format().getVertexSize();
        this.buffer.limit(this.totalUploadedBytes);
        if (this.lastRenderedCountIndex == this.vertexCounts.size() && this.vertices == 0) {
            this.clear();
        }
        ByteBuffer byteBuffer = this.buffer.slice();
        this.buffer.position(0);
        this.buffer.limit(this.buffer.capacity());
        return Pair.of(drawState, byteBuffer);
    }

    public void clear() {
        if (this.totalRenderedBytes != this.totalUploadedBytes) {
            LOGGER.warn("Bytes mismatch " + this.totalRenderedBytes + " " + this.totalUploadedBytes);
        }
        this.totalRenderedBytes = 0;
        this.totalUploadedBytes = 0;
        this.vertexCounts.clear();
        this.lastRenderedCountIndex = 0;
    }

    @Environment(value=EnvType.CLIENT)
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

    @Environment(value=EnvType.CLIENT)
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

