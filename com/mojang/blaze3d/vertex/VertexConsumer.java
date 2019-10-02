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
import net.minecraft.util.Mth;
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
        Vector3f vector3f = new Vector3f(vec3i.getX(), vec3i.getY(), vec3i.getZ());
        Matrix3f matrix3f = new Matrix3f(matrix4f);
        matrix3f.transpose();
        float i = matrix3f.adjugateAndDet();
        if (i < 1.0E-5f) {
            LOGGER.warn("Could not invert matrix while baking vertex: " + matrix4f);
        } else {
            float j = matrix3f.determinant();
            matrix3f.mul(Mth.fastInvCubeRoot(j));
        }
        vector3f.transform(matrix3f);
        int k = 8;
        int l = js.length / 8;
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            ByteBuffer byteBuffer = memoryStack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            for (int m = 0; m < l; ++m) {
                byte d;
                byte c;
                byte b;
                int q;
                intBuffer.clear();
                intBuffer.put(js, m * 8, 8);
                float n = byteBuffer.getFloat(0);
                float o = byteBuffer.getFloat(4);
                float p = byteBuffer.getFloat(8);
                if (bl) {
                    q = byteBuffer.get(12) & 0xFF;
                    int r = byteBuffer.get(13) & 0xFF;
                    int s = byteBuffer.get(14) & 0xFF;
                    b = (byte)((float)q * fs[m] * f);
                    c = (byte)((float)r * fs[m] * g);
                    d = (byte)((float)s * fs[m] * h);
                } else {
                    b = (byte)(255.0f * fs[m] * f);
                    c = (byte)(255.0f * fs[m] * g);
                    d = (byte)(255.0f * fs[m] * h);
                }
                q = is[m];
                float t = byteBuffer.getFloat(16);
                float u = byteBuffer.getFloat(20);
                this.vertex(matrix4f, n, o, p);
                this.color(b, c, d, 255);
                this.uv(t, u);
                this.uv2(q);
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

