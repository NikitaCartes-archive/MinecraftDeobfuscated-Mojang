/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class VertexFormat {
    private final ImmutableList<VertexFormatElement> elements;
    private final ImmutableMap<String, VertexFormatElement> elementMapping;
    private final IntList offsets = new IntArrayList();
    private final int vertexSize;
    @Nullable
    private VertexBuffer immediateDrawVertexBuffer;

    public VertexFormat(ImmutableMap<String, VertexFormatElement> immutableMap) {
        this.elementMapping = immutableMap;
        this.elements = ((ImmutableCollection)immutableMap.values()).asList();
        int i = 0;
        for (VertexFormatElement vertexFormatElement : immutableMap.values()) {
            this.offsets.add(i);
            i += vertexFormatElement.getByteSize();
        }
        this.vertexSize = i;
    }

    public String toString() {
        return "format: " + this.elementMapping.size() + " elements: " + this.elementMapping.entrySet().stream().map(Object::toString).collect(Collectors.joining(" "));
    }

    public int getIntegerSize() {
        return this.getVertexSize() / 4;
    }

    public int getVertexSize() {
        return this.vertexSize;
    }

    public ImmutableList<VertexFormatElement> getElements() {
        return this.elements;
    }

    public ImmutableList<String> getElementAttributeNames() {
        return ((ImmutableCollection)((Object)this.elementMapping.keySet())).asList();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        VertexFormat vertexFormat = (VertexFormat)object;
        if (this.vertexSize != vertexFormat.vertexSize) {
            return false;
        }
        return this.elementMapping.equals(vertexFormat.elementMapping);
    }

    public int hashCode() {
        return this.elementMapping.hashCode();
    }

    public void setupBufferState() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::_setupBufferState);
            return;
        }
        this._setupBufferState();
    }

    private void _setupBufferState() {
        int i = this.getVertexSize();
        ImmutableList<VertexFormatElement> list = this.getElements();
        for (int j = 0; j < list.size(); ++j) {
            ((VertexFormatElement)list.get(j)).setupBufferState(j, this.offsets.getInt(j), i);
        }
    }

    public void clearBufferState() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::_clearBufferState);
            return;
        }
        this._clearBufferState();
    }

    private void _clearBufferState() {
        ImmutableList<VertexFormatElement> immutableList = this.getElements();
        for (int i = 0; i < immutableList.size(); ++i) {
            VertexFormatElement vertexFormatElement = (VertexFormatElement)immutableList.get(i);
            vertexFormatElement.clearBufferState(i);
        }
    }

    public VertexBuffer getImmediateDrawVertexBuffer() {
        VertexBuffer vertexBuffer = this.immediateDrawVertexBuffer;
        if (vertexBuffer == null) {
            this.immediateDrawVertexBuffer = vertexBuffer = new VertexBuffer();
        }
        return vertexBuffer;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Mode {
        LINES(4, 2, 2, false),
        LINE_STRIP(5, 2, 1, true),
        DEBUG_LINES(1, 2, 2, false),
        DEBUG_LINE_STRIP(3, 2, 1, true),
        TRIANGLES(4, 3, 3, false),
        TRIANGLE_STRIP(5, 3, 1, true),
        TRIANGLE_FAN(6, 3, 1, true),
        QUADS(4, 4, 4, false);

        public final int asGLMode;
        public final int primitiveLength;
        public final int primitiveStride;
        public final boolean connectedPrimitives;

        private Mode(int j, int k, int l, boolean bl) {
            this.asGLMode = j;
            this.primitiveLength = k;
            this.primitiveStride = l;
            this.connectedPrimitives = bl;
        }

        public int indexCount(int i) {
            return switch (this) {
                case LINE_STRIP, DEBUG_LINES, DEBUG_LINE_STRIP, TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN -> i;
                case LINES, QUADS -> i / 4 * 6;
                default -> 0;
            };
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum IndexType {
        BYTE(5121, 1),
        SHORT(5123, 2),
        INT(5125, 4);

        public final int asGLType;
        public final int bytes;

        private IndexType(int j, int k) {
            this.asGLType = j;
            this.bytes = k;
        }

        public static IndexType least(int i) {
            if ((i & 0xFFFF0000) != 0) {
                return INT;
            }
            if ((i & 0xFF00) != 0) {
                return SHORT;
            }
            return BYTE;
        }
    }
}

