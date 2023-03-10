/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public interface BufferVertexConsumer
extends VertexConsumer {
    public VertexFormatElement currentElement();

    public void nextElement();

    public void putByte(int var1, byte var2);

    public void putShort(int var1, short var2);

    public void putFloat(int var1, float var2);

    @Override
    default public VertexConsumer vertex(double d, double e, double f) {
        if (this.currentElement().getUsage() != VertexFormatElement.Usage.POSITION) {
            return this;
        }
        if (this.currentElement().getType() != VertexFormatElement.Type.FLOAT || this.currentElement().getCount() != 3) {
            throw new IllegalStateException();
        }
        this.putFloat(0, (float)d);
        this.putFloat(4, (float)e);
        this.putFloat(8, (float)f);
        this.nextElement();
        return this;
    }

    @Override
    default public VertexConsumer color(int i, int j, int k, int l) {
        VertexFormatElement vertexFormatElement = this.currentElement();
        if (vertexFormatElement.getUsage() != VertexFormatElement.Usage.COLOR) {
            return this;
        }
        if (vertexFormatElement.getType() != VertexFormatElement.Type.UBYTE || vertexFormatElement.getCount() != 4) {
            throw new IllegalStateException();
        }
        this.putByte(0, (byte)i);
        this.putByte(1, (byte)j);
        this.putByte(2, (byte)k);
        this.putByte(3, (byte)l);
        this.nextElement();
        return this;
    }

    @Override
    default public VertexConsumer uv(float f, float g) {
        VertexFormatElement vertexFormatElement = this.currentElement();
        if (vertexFormatElement.getUsage() != VertexFormatElement.Usage.UV || vertexFormatElement.getIndex() != 0) {
            return this;
        }
        if (vertexFormatElement.getType() != VertexFormatElement.Type.FLOAT || vertexFormatElement.getCount() != 2) {
            throw new IllegalStateException();
        }
        this.putFloat(0, f);
        this.putFloat(4, g);
        this.nextElement();
        return this;
    }

    @Override
    default public VertexConsumer overlayCoords(int i, int j) {
        return this.uvShort((short)i, (short)j, 1);
    }

    @Override
    default public VertexConsumer uv2(int i, int j) {
        return this.uvShort((short)i, (short)j, 2);
    }

    default public VertexConsumer uvShort(short s, short t, int i) {
        VertexFormatElement vertexFormatElement = this.currentElement();
        if (vertexFormatElement.getUsage() != VertexFormatElement.Usage.UV || vertexFormatElement.getIndex() != i) {
            return this;
        }
        if (vertexFormatElement.getType() != VertexFormatElement.Type.SHORT || vertexFormatElement.getCount() != 2) {
            throw new IllegalStateException();
        }
        this.putShort(0, s);
        this.putShort(2, t);
        this.nextElement();
        return this;
    }

    @Override
    default public VertexConsumer normal(float f, float g, float h) {
        VertexFormatElement vertexFormatElement = this.currentElement();
        if (vertexFormatElement.getUsage() != VertexFormatElement.Usage.NORMAL) {
            return this;
        }
        if (vertexFormatElement.getType() != VertexFormatElement.Type.BYTE || vertexFormatElement.getCount() != 3) {
            throw new IllegalStateException();
        }
        this.putByte(0, BufferVertexConsumer.normalIntValue(f));
        this.putByte(1, BufferVertexConsumer.normalIntValue(g));
        this.putByte(2, BufferVertexConsumer.normalIntValue(h));
        this.nextElement();
        return this;
    }

    public static byte normalIntValue(float f) {
        return (byte)((int)(Mth.clamp(f, -1.0f, 1.0f) * 127.0f) & 0xFF);
    }
}

