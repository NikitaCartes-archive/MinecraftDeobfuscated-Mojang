/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import org.lwjgl.system.MemoryStack;

@Environment(value=EnvType.CLIENT)
public interface VertexConsumer {
    public VertexConsumer vertex(double var1, double var3, double var5);

    public VertexConsumer color(int var1, int var2, int var3, int var4);

    public VertexConsumer uv(float var1, float var2);

    public VertexConsumer overlayCoords(int var1, int var2);

    public VertexConsumer uv2(int var1, int var2);

    public VertexConsumer normal(float var1, float var2, float var3);

    public void endVertex();

    public void defaultOverlayCoords(int var1, int var2);

    public void unsetDefaultOverlayCoords();

    default public VertexConsumer color(float f, float g, float h, float i) {
        return this.color((int)(f * 255.0f), (int)(g * 255.0f), (int)(h * 255.0f), (int)(i * 255.0f));
    }

    default public VertexConsumer uv2(int i) {
        return this.uv2(i & 0xFFFF, i >> 16 & 0xFFFF);
    }

    default public void putBulkData(Matrix4f matrix4f, BakedQuad bakedQuad, float f, float g, float h, int i) {
        this.putBulkData(matrix4f, bakedQuad, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, f, g, h, new int[]{i, i, i, i}, false);
    }

    default public void putBulkData(Matrix4f matrix4f, BakedQuad bakedQuad, float[] fs, float f, float g, float h, int[] is, boolean bl) {
        int[] js = bakedQuad.getVertices();
        Vec3i vec3i = bakedQuad.getDirection().getNormal();
        int i = 8;
        int j = js.length / 8;
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            ByteBuffer byteBuffer = memoryStack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            for (int k = 0; k < j; ++k) {
                byte d;
                byte c;
                byte b;
                int o;
                intBuffer.clear();
                intBuffer.put(js, k * 8, 8);
                float l = byteBuffer.getFloat(0);
                float m = byteBuffer.getFloat(4);
                float n = byteBuffer.getFloat(8);
                if (bl) {
                    o = byteBuffer.get(12) & 0xFF;
                    int p = byteBuffer.get(13) & 0xFF;
                    int q = byteBuffer.get(14) & 0xFF;
                    b = (byte)((float)o * fs[k] * f);
                    c = (byte)((float)p * fs[k] * g);
                    d = (byte)((float)q * fs[k] * h);
                } else {
                    b = (byte)(255.0f * fs[k] * f);
                    c = (byte)(255.0f * fs[k] * g);
                    d = (byte)(255.0f * fs[k] * h);
                }
                o = is[k];
                float r = byteBuffer.getFloat(16);
                float s = byteBuffer.getFloat(20);
                this.vertex(matrix4f, l, m, n);
                this.color(b, c, d, 255);
                this.uv(r, s);
                this.uv2(o);
                this.normal(vec3i.getX(), vec3i.getY(), vec3i.getZ());
                this.endVertex();
            }
        }
    }

    default public VertexConsumer vertex(Matrix4f matrix4f, float f, float g, float h) {
        Vector4f vector4f = new Vector4f(f, g, h, 1.0f);
        vector4f.transform(matrix4f);
        return this.vertex(vector4f.x(), vector4f.y(), vector4f.z());
    }
}

