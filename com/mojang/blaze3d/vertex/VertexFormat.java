/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class VertexFormat {
    private final ImmutableList<VertexFormatElement> elements;
    private final ImmutableMap<String, VertexFormatElement> elementMapping;
    private final IntList offsets = new IntArrayList();
    private final int vertexSize;
    private int vertexArrayObject;
    private int vertexBufferObject;
    private int indexBufferObject;

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
        return ((ImmutableSet)this.elementMapping.keySet()).asList();
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

    public int getOrCreateVertexArrayObject() {
        if (this.vertexArrayObject == 0) {
            this.vertexArrayObject = GlStateManager._glGenVertexArrays();
        }
        return this.vertexArrayObject;
    }

    public int getOrCreateVertexBufferObject() {
        if (this.vertexBufferObject == 0) {
            this.vertexBufferObject = GlStateManager._glGenBuffers();
        }
        return this.vertexBufferObject;
    }

    public int getOrCreateIndexBufferObject() {
        if (this.indexBufferObject == 0) {
            this.indexBufferObject = GlStateManager._glGenBuffers();
        }
        return this.indexBufferObject;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Mode {
        LINES(4, 2, 2),
        LINE_STRIP(5, 2, 1),
        DEBUG_LINES(1, 2, 2),
        DEBUG_LINE_STRIP(3, 2, 1),
        TRIANGLES(4, 3, 3),
        TRIANGLE_STRIP(5, 3, 1),
        TRIANGLE_FAN(6, 3, 1),
        QUADS(4, 4, 4);

        public final int asGLMode;
        public final int primitiveLength;
        public final int primitiveStride;

        private Mode(int j, int k, int l) {
            this.asGLMode = j;
            this.primitiveLength = k;
            this.primitiveStride = l;
        }

        public int indexCount(int i) {
            int j;
            switch (this) {
                case LINE_STRIP: 
                case DEBUG_LINES: 
                case DEBUG_LINE_STRIP: 
                case TRIANGLES: 
                case TRIANGLE_STRIP: 
                case TRIANGLE_FAN: {
                    j = i;
                    break;
                }
                case LINES: 
                case QUADS: {
                    j = i / 4 * 6;
                    break;
                }
                default: {
                    j = 0;
                }
            }
            return j;
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

