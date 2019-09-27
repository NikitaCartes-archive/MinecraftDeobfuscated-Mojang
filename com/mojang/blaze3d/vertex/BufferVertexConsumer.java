/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

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
        if (this.currentElement().getType() != VertexFormatElement.Type.FLOAT) {
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
        if (this.currentElement().getUsage() != VertexFormatElement.Usage.COLOR) {
            return this;
        }
        if (this.currentElement().getType() != VertexFormatElement.Type.UBYTE) {
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
        if (this.currentElement().getUsage() != VertexFormatElement.Usage.UV || this.currentElement().getIndex() != 0) {
            return this;
        }
        if (this.currentElement().getType() != VertexFormatElement.Type.FLOAT) {
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
        if (this.currentElement().getUsage() != VertexFormatElement.Usage.UV || this.currentElement().getIndex() != i) {
            return this;
        }
        if (this.currentElement().getType() != VertexFormatElement.Type.SHORT) {
            throw new IllegalStateException();
        }
        this.putShort(0, s);
        this.putShort(2, t);
        this.nextElement();
        return this;
    }

    @Override
    default public VertexConsumer normal(float f, float g, float h) {
        if (this.currentElement().getUsage() != VertexFormatElement.Usage.NORMAL) {
            return this;
        }
        if (this.currentElement().getType() != VertexFormatElement.Type.BYTE) {
            throw new IllegalStateException();
        }
        this.putByte(0, (byte)((int)(f * 127.0f) & 0xFF));
        this.putByte(1, (byte)((int)(g * 127.0f) & 0xFF));
        this.putByte(2, (byte)((int)(h * 127.0f) & 0xFF));
        this.nextElement();
        return this;
    }
}

