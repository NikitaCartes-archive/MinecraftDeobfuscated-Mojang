/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import java.util.function.IntConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class VertexFormatElement {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Type type;
    private final Usage usage;
    private final int index;
    private final int count;

    public VertexFormatElement(int i, Type type, Usage usage, int j) {
        if (this.supportsUsage(i, usage)) {
            this.usage = usage;
        } else {
            LOGGER.warn("Multiple vertex elements of the same type other than UVs are not supported. Forcing type to UV.");
            this.usage = Usage.UV;
        }
        this.type = type;
        this.index = i;
        this.count = j;
    }

    private boolean supportsUsage(int i, Usage usage) {
        return i == 0 || usage == Usage.UV;
    }

    public final Type getType() {
        return this.type;
    }

    public final Usage getUsage() {
        return this.usage;
    }

    public final int getCount() {
        return this.count;
    }

    public final int getIndex() {
        return this.index;
    }

    public String toString() {
        return this.count + "," + this.usage.getName() + "," + this.type.getName();
    }

    public final int getByteSize() {
        return this.type.getSize() * this.count;
    }

    public final boolean isPosition() {
        return this.usage == Usage.POSITION;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        VertexFormatElement vertexFormatElement = (VertexFormatElement)object;
        if (this.count != vertexFormatElement.count) {
            return false;
        }
        if (this.index != vertexFormatElement.index) {
            return false;
        }
        if (this.type != vertexFormatElement.type) {
            return false;
        }
        return this.usage == vertexFormatElement.usage;
    }

    public int hashCode() {
        int i = this.type.hashCode();
        i = 31 * i + this.usage.hashCode();
        i = 31 * i + this.index;
        i = 31 * i + this.count;
        return i;
    }

    public void setupBufferState(long l, int i) {
        this.usage.setupBufferState(this.count, this.type.getGlType(), i, l, this.index);
    }

    public void clearBufferState() {
        this.usage.clearBufferState(this.index);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Type {
        FLOAT(4, "Float", 5126),
        UBYTE(1, "Unsigned Byte", 5121),
        BYTE(1, "Byte", 5120),
        USHORT(2, "Unsigned Short", 5123),
        SHORT(2, "Short", 5122),
        UINT(4, "Unsigned Int", 5125),
        INT(4, "Int", 5124);

        private final int size;
        private final String name;
        private final int glType;

        private Type(int j, String string2, int k) {
            this.size = j;
            this.name = string2;
            this.glType = k;
        }

        public int getSize() {
            return this.size;
        }

        public String getName() {
            return this.name;
        }

        public int getGlType() {
            return this.glType;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Usage {
        POSITION("Position", (i, j, k, l, m) -> {
            GlStateManager._vertexPointer(i, j, k, l);
            GlStateManager._enableClientState(32884);
        }, i -> GlStateManager._disableClientState(32884)),
        NORMAL("Normal", (i, j, k, l, m) -> {
            GlStateManager._normalPointer(j, k, l);
            GlStateManager._enableClientState(32885);
        }, i -> GlStateManager._disableClientState(32885)),
        COLOR("Vertex Color", (i, j, k, l, m) -> {
            GlStateManager._colorPointer(i, j, k, l);
            GlStateManager._enableClientState(32886);
        }, i -> {
            GlStateManager._disableClientState(32886);
            GlStateManager._clearCurrentColor();
        }),
        UV("UV", (i, j, k, l, m) -> {
            GlStateManager._glClientActiveTexture(33984 + m);
            GlStateManager._texCoordPointer(i, j, k, l);
            GlStateManager._enableClientState(32888);
            GlStateManager._glClientActiveTexture(33984);
        }, i -> {
            GlStateManager._glClientActiveTexture(33984 + i);
            GlStateManager._disableClientState(32888);
            GlStateManager._glClientActiveTexture(33984);
        }),
        PADDING("Padding", (i, j, k, l, m) -> {}, i -> {}),
        GENERIC("Generic", (i, j, k, l, m) -> {
            GlStateManager._enableVertexAttribArray(m);
            GlStateManager._vertexAttribPointer(m, i, j, false, k, l);
        }, GlStateManager::_disableVertexAttribArray);

        private final String name;
        private final SetupState setupState;
        private final IntConsumer clearState;

        private Usage(String string2, SetupState setupState, IntConsumer intConsumer) {
            this.name = string2;
            this.setupState = setupState;
            this.clearState = intConsumer;
        }

        private void setupBufferState(int i, int j, int k, long l, int m) {
            this.setupState.setupBufferState(i, j, k, l, m);
        }

        public void clearBufferState(int i) {
            this.clearState.accept(i);
        }

        public String getName() {
            return this.name;
        }

        @Environment(value=EnvType.CLIENT)
        static interface SetupState {
            public void setupBufferState(int var1, int var2, int var3, long var4, int var6);
        }
    }
}

