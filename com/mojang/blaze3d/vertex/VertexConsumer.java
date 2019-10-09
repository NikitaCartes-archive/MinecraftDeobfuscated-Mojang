/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.MemoryStack;

@Environment(value=EnvType.CLIENT)
public interface VertexConsumer {
    public static final Logger LOGGER = LogManager.getLogger();

    public VertexConsumer vertex(double var1, double var3, double var5);

    public VertexConsumer color(int var1, int var2, int var3, int var4);

    public VertexConsumer uv(float var1, float var2);

    public VertexConsumer overlayCoords(int var1, int var2);

    public VertexConsumer uv2(int var1, int var2);

    public VertexConsumer normal(float var1, float var2, float var3);

    public void endVertex();

    default public VertexConsumer color(float f, float g, float h, float i) {
        return this.color((int)(f * 255.0f), (int)(g * 255.0f), (int)(h * 255.0f), (int)(i * 255.0f));
    }

    default public VertexConsumer uv2(int i) {
        return this.uv2(i & 0xFFFF, i >> 16 & 0xFFFF);
    }

    default public VertexConsumer overlayCoords(int i) {
        return this.overlayCoords(i & 0xFFFF, i >> 16 & 0xFFFF);
    }

    default public void putBulkData(Matrix4f matrix4f, Matrix3f matrix3f, BakedQuad bakedQuad, float f, float g, float h, int i, int j) {
        this.putBulkData(matrix4f, matrix3f, bakedQuad, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, f, g, h, new int[]{i, i, i, i}, j, false);
    }

    default public void putBulkData(Matrix4f matrix4f, Matrix3f matrix3f, BakedQuad bakedQuad, float[] fs, float f, float g, float h, int[] is, int i, boolean bl) {
        int[] js = bakedQuad.getVertices();
        Vec3i vec3i = bakedQuad.getDirection().getNormal();
        Vector3f vector3f = new Vector3f(vec3i.getX(), vec3i.getY(), vec3i.getZ());
        vector3f.transform(matrix3f);
        int j = 8;
        int k = js.length / 8;
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            ByteBuffer byteBuffer = memoryStack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            for (int l = 0; l < k; ++l) {
                byte d;
                byte c;
                byte b;
                int p;
                intBuffer.clear();
                intBuffer.put(js, l * 8, 8);
                float m = byteBuffer.getFloat(0);
                float n = byteBuffer.getFloat(4);
                float o = byteBuffer.getFloat(8);
                if (bl) {
                    p = byteBuffer.get(12) & 0xFF;
                    int q = byteBuffer.get(13) & 0xFF;
                    int r = byteBuffer.get(14) & 0xFF;
                    b = (byte)((float)p * fs[l] * f);
                    c = (byte)((float)q * fs[l] * g);
                    d = (byte)((float)r * fs[l] * h);
                } else {
                    b = (byte)(255.0f * fs[l] * f);
                    c = (byte)(255.0f * fs[l] * g);
                    d = (byte)(255.0f * fs[l] * h);
                }
                p = is[l];
                float s = byteBuffer.getFloat(16);
                float t = byteBuffer.getFloat(20);
                this.vertex(matrix4f, m, n, o);
                this.color(b, c, d, 255);
                this.uv(s, t);
                this.overlayCoords(i);
                this.uv2(p);
                this.normal(vector3f.x(), vector3f.y(), vector3f.z());
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

