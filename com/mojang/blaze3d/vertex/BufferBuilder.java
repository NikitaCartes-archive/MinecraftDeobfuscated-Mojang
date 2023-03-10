/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Floats;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.BufferVertexConsumer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.DefaultedVertexConsumer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class BufferBuilder
extends DefaultedVertexConsumer
implements BufferVertexConsumer {
    private static final int GROWTH_SIZE = 0x200000;
    private static final Logger LOGGER = LogUtils.getLogger();
    private ByteBuffer buffer;
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
    private float sortX = Float.NaN;
    private float sortY = Float.NaN;
    private float sortZ = Float.NaN;
    private boolean indexOnly;

    public BufferBuilder(int i) {
        this.buffer = MemoryTracker.create(i * 6);
    }

    private void ensureVertexCapacity() {
        this.ensureCapacity(this.format.getVertexSize());
    }

    private void ensureCapacity(int i) {
        if (this.nextElementByte + i <= this.buffer.capacity()) {
            return;
        }
        int j = this.buffer.capacity();
        int k = j + BufferBuilder.roundUp(i);
        LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", (Object)j, (Object)k);
        ByteBuffer byteBuffer = MemoryTracker.resize(this.buffer, k);
        byteBuffer.rewind();
        this.buffer = byteBuffer;
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

    public void setQuadSortOrigin(float f, float g, float h) {
        if (this.mode != VertexFormat.Mode.QUADS) {
            return;
        }
        if (this.sortX != f || this.sortY != g || this.sortZ != h) {
            this.sortX = f;
            this.sortY = g;
            this.sortZ = h;
            if (this.sortingPoints == null) {
                this.sortingPoints = this.makeQuadSortingPoints();
            }
        }
    }

    public SortState getSortState() {
        return new SortState(this.mode, this.vertices, this.sortingPoints, this.sortX, this.sortY, this.sortZ);
    }

    public void restoreSortState(SortState sortState) {
        this.buffer.rewind();
        this.mode = sortState.mode;
        this.vertices = sortState.vertices;
        this.nextElementByte = this.renderedBufferPointer;
        this.sortingPoints = sortState.sortingPoints;
        this.sortX = sortState.sortX;
        this.sortY = sortState.sortY;
        this.sortZ = sortState.sortZ;
        this.indexOnly = true;
    }

    public void begin(VertexFormat.Mode mode, VertexFormat vertexFormat) {
        if (this.building) {
            throw new IllegalStateException("Already building!");
        }
        this.building = true;
        this.mode = mode;
        this.switchFormat(vertexFormat);
        this.currentElement = (VertexFormatElement)vertexFormat.getElements().get(0);
        this.elementIndex = 0;
        this.buffer.rewind();
    }

    private void switchFormat(VertexFormat vertexFormat) {
        if (this.format == vertexFormat) {
            return;
        }
        this.format = vertexFormat;
        boolean bl = vertexFormat == DefaultVertexFormat.NEW_ENTITY;
        boolean bl2 = vertexFormat == DefaultVertexFormat.BLOCK;
        this.fastFormat = bl || bl2;
        this.fullFormat = bl;
    }

    private IntConsumer intConsumer(int i2, VertexFormat.IndexType indexType) {
        MutableInt mutableInt = new MutableInt(i2);
        return switch (indexType) {
            default -> throw new IncompatibleClassChangeError();
            case VertexFormat.IndexType.BYTE -> i -> this.buffer.put(mutableInt.getAndIncrement(), (byte)i);
            case VertexFormat.IndexType.SHORT -> i -> this.buffer.putShort(mutableInt.getAndAdd(2), (short)i);
            case VertexFormat.IndexType.INT -> i -> this.buffer.putInt(mutableInt.getAndAdd(4), i);
        };
    }

    private Vector3f[] makeQuadSortingPoints() {
        FloatBuffer floatBuffer = this.buffer.asFloatBuffer();
        int i = this.renderedBufferPointer / 4;
        int j = this.format.getIntegerSize();
        int k = j * this.mode.primitiveStride;
        int l = this.vertices / this.mode.primitiveStride;
        Vector3f[] vector3fs = new Vector3f[l];
        for (int m = 0; m < l; ++m) {
            float f = floatBuffer.get(i + m * k + 0);
            float g = floatBuffer.get(i + m * k + 1);
            float h = floatBuffer.get(i + m * k + 2);
            float n = floatBuffer.get(i + m * k + j * 2 + 0);
            float o = floatBuffer.get(i + m * k + j * 2 + 1);
            float p = floatBuffer.get(i + m * k + j * 2 + 2);
            float q = (f + n) / 2.0f;
            float r = (g + o) / 2.0f;
            float s = (h + p) / 2.0f;
            vector3fs[m] = new Vector3f(q, r, s);
        }
        return vector3fs;
    }

    private void putSortedQuadIndices(VertexFormat.IndexType indexType) {
        float[] fs = new float[this.sortingPoints.length];
        int[] is = new int[this.sortingPoints.length];
        for (int i2 = 0; i2 < this.sortingPoints.length; ++i2) {
            float f = this.sortingPoints[i2].x() - this.sortX;
            float g = this.sortingPoints[i2].y() - this.sortY;
            float h = this.sortingPoints[i2].z() - this.sortZ;
            fs[i2] = f * f + g * g + h * h;
            is[i2] = i2;
        }
        IntArrays.mergeSort(is, (i, j) -> Floats.compare(fs[j], fs[i]));
        IntConsumer intConsumer = this.intConsumer(this.nextElementByte, indexType);
        for (int j2 : is) {
            intConsumer.accept(j2 * this.mode.primitiveStride + 0);
            intConsumer.accept(j2 * this.mode.primitiveStride + 1);
            intConsumer.accept(j2 * this.mode.primitiveStride + 2);
            intConsumer.accept(j2 * this.mode.primitiveStride + 2);
            intConsumer.accept(j2 * this.mode.primitiveStride + 3);
            intConsumer.accept(j2 * this.mode.primitiveStride + 0);
        }
    }

    public boolean isCurrentBatchEmpty() {
        return this.vertices == 0;
    }

    @Nullable
    public RenderedBuffer endOrDiscardIfEmpty() {
        this.ensureDrawing();
        if (this.isCurrentBatchEmpty()) {
            this.reset();
            return null;
        }
        RenderedBuffer renderedBuffer = this.storeRenderedBuffer();
        this.reset();
        return renderedBuffer;
    }

    public RenderedBuffer end() {
        this.ensureDrawing();
        RenderedBuffer renderedBuffer = this.storeRenderedBuffer();
        this.reset();
        return renderedBuffer;
    }

    private void ensureDrawing() {
        if (!this.building) {
            throw new IllegalStateException("Not building!");
        }
    }

    private RenderedBuffer storeRenderedBuffer() {
        int l;
        boolean bl;
        int k;
        int i = this.mode.indexCount(this.vertices);
        int j = !this.indexOnly ? this.vertices * this.format.getVertexSize() : 0;
        VertexFormat.IndexType indexType = VertexFormat.IndexType.least(i);
        if (this.sortingPoints != null) {
            k = Mth.roundToward(i * indexType.bytes, 4);
            this.ensureCapacity(k);
            this.putSortedQuadIndices(indexType);
            bl = false;
            this.nextElementByte += k;
            l = j + k;
        } else {
            bl = true;
            l = j;
        }
        k = this.renderedBufferPointer;
        this.renderedBufferPointer += l;
        ++this.renderedBufferCount;
        DrawState drawState = new DrawState(this.format, this.vertices, i, this.mode, indexType, this.indexOnly, bl);
        return new RenderedBuffer(k, drawState);
    }

    private void reset() {
        this.building = false;
        this.vertices = 0;
        this.currentElement = null;
        this.elementIndex = 0;
        this.sortingPoints = null;
        this.sortX = Float.NaN;
        this.sortY = Float.NaN;
        this.sortZ = Float.NaN;
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
        }
        ++this.vertices;
        this.ensureVertexCapacity();
        if (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP) {
            int i = this.format.getVertexSize();
            this.buffer.put(this.nextElementByte, this.buffer, this.nextElementByte - i, i);
            this.nextElementByte += i;
            ++this.vertices;
            this.ensureVertexCapacity();
        }
    }

    @Override
    public void nextElement() {
        VertexFormatElement vertexFormatElement;
        ImmutableList<VertexFormatElement> immutableList = this.format.getElements();
        this.elementIndex = (this.elementIndex + 1) % immutableList.size();
        this.nextElementByte += this.currentElement.getByteSize();
        this.currentElement = vertexFormatElement = (VertexFormatElement)immutableList.get(this.elementIndex);
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
        }
        return BufferVertexConsumer.super.color(i, j, k, l);
    }

    @Override
    public void vertex(float f, float g, float h, float i, float j, float k, float l, float m, float n, int o, int p, float q, float r, float s) {
        if (this.defaultColorSet) {
            throw new IllegalStateException();
        }
        if (this.fastFormat) {
            int t;
            this.putFloat(0, f);
            this.putFloat(4, g);
            this.putFloat(8, h);
            this.putByte(12, (byte)(i * 255.0f));
            this.putByte(13, (byte)(j * 255.0f));
            this.putByte(14, (byte)(k * 255.0f));
            this.putByte(15, (byte)(l * 255.0f));
            this.putFloat(16, m);
            this.putFloat(20, n);
            if (this.fullFormat) {
                this.putShort(24, (short)(o & 0xFFFF));
                this.putShort(26, (short)(o >> 16 & 0xFFFF));
                t = 28;
            } else {
                t = 24;
            }
            this.putShort(t + 0, (short)(p & 0xFFFF));
            this.putShort(t + 2, (short)(p >> 16 & 0xFFFF));
            this.putByte(t + 4, BufferVertexConsumer.normalIntValue(q));
            this.putByte(t + 5, BufferVertexConsumer.normalIntValue(r));
            this.putByte(t + 6, BufferVertexConsumer.normalIntValue(s));
            this.nextElementByte += t + 8;
            this.endVertex();
            return;
        }
        super.vertex(f, g, h, i, j, k, l, m, n, o, p, q, r, s);
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

    @Override
    public VertexFormatElement currentElement() {
        if (this.currentElement == null) {
            throw new IllegalStateException("BufferBuilder not started");
        }
        return this.currentElement;
    }

    public boolean building() {
        return this.building;
    }

    ByteBuffer bufferSlice(int i, int j) {
        return MemoryUtil.memSlice(this.buffer, i, j - i);
    }

    @Environment(value=EnvType.CLIENT)
    public static class SortState {
        final VertexFormat.Mode mode;
        final int vertices;
        @Nullable
        final Vector3f[] sortingPoints;
        final float sortX;
        final float sortY;
        final float sortZ;

        SortState(VertexFormat.Mode mode, int i, @Nullable Vector3f[] vector3fs, float f, float g, float h) {
            this.mode = mode;
            this.vertices = i;
            this.sortingPoints = vector3fs;
            this.sortX = f;
            this.sortY = g;
            this.sortZ = h;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class RenderedBuffer {
        private final int pointer;
        private final DrawState drawState;
        private boolean released;

        RenderedBuffer(int i, DrawState drawState) {
            this.pointer = i;
            this.drawState = drawState;
        }

        public ByteBuffer vertexBuffer() {
            int i = this.pointer + this.drawState.vertexBufferStart();
            int j = this.pointer + this.drawState.vertexBufferEnd();
            return BufferBuilder.this.bufferSlice(i, j);
        }

        public ByteBuffer indexBuffer() {
            int i = this.pointer + this.drawState.indexBufferStart();
            int j = this.pointer + this.drawState.indexBufferEnd();
            return BufferBuilder.this.bufferSlice(i, j);
        }

        public DrawState drawState() {
            return this.drawState;
        }

        public boolean isEmpty() {
            return this.drawState.vertexCount == 0;
        }

        public void release() {
            if (this.released) {
                throw new IllegalStateException("Buffer has already been released!");
            }
            BufferBuilder.this.releaseRenderedBuffer();
            this.released = true;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record DrawState(VertexFormat format, int vertexCount, int indexCount, VertexFormat.Mode mode, VertexFormat.IndexType indexType, boolean indexOnly, boolean sequentialIndex) {
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
}

