/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

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

    private final boolean supportsUsage(int i, Usage usage) {
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
        POSITION("Position"),
        NORMAL("Normal"),
        COLOR("Vertex Color"),
        UV("UV"),
        MATRIX("Bone Matrix"),
        BLEND_WEIGHT("Blend Weight"),
        PADDING("Padding");

        private final String name;

        private Usage(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }
    }
}

