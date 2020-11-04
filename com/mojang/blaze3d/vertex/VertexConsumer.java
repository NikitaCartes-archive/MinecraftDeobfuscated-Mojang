/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
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

    default public void vertex(float f, float g, float h, float i, float j, float k, float l, float m, float n, int o, int p, float q, float r, float s) {
        this.vertex(f, g, h);
        this.color(i, j, k, l);
        this.uv(m, n);
        this.overlayCoords(o);
        this.uv2(p);
        this.normal(q, r, s);
        this.endVertex();
    }

    default public VertexConsumer color(float f, float g, float h, float i) {
        return this.color((int)(f * 255.0f), (int)(g * 255.0f), (int)(h * 255.0f), (int)(i * 255.0f));
    }

    default public VertexConsumer uv2(int i) {
        return this.uv2(i & 0xFFFF, i >> 16 & 0xFFFF);
    }

    default public VertexConsumer overlayCoords(int i) {
        return this.overlayCoords(i & 0xFFFF, i >> 16 & 0xFFFF);
    }

    default public void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float f, float g, float h, int i, int j) {
        this.putBulkData(pose, bakedQuad, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, f, g, h, new int[]{i, i, i, i}, j, false);
    }

    default public void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float[] fs, float f, float g, float h, int[] is, int i, boolean bl) {
        float[] gs = new float[]{fs[0], fs[1], fs[2], fs[3]};
        int[] js = new int[]{is[0], is[1], is[2], is[3]};
        int[] ks = bakedQuad.getVertices();
        Vec3i vec3i = bakedQuad.getDirection().getNormal();
        Vector3f vector3f = new Vector3f(vec3i.getX(), vec3i.getY(), vec3i.getZ());
        Matrix4f matrix4f = pose.pose();
        vector3f.transform(pose.normal());
        int j = 8;
        int k = ks.length / 8;
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            ByteBuffer byteBuffer = memoryStack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            for (int l = 0; l < k; ++l) {
                float u;
                float t;
                float s;
                float r;
                float q;
                intBuffer.clear();
                intBuffer.put(ks, l * 8, 8);
                float m = byteBuffer.getFloat(0);
                float n = byteBuffer.getFloat(4);
                float o = byteBuffer.getFloat(8);
                if (bl) {
                    float p = (float)(byteBuffer.get(12) & 0xFF) / 255.0f;
                    q = (float)(byteBuffer.get(13) & 0xFF) / 255.0f;
                    r = (float)(byteBuffer.get(14) & 0xFF) / 255.0f;
                    s = p * gs[l] * f;
                    t = q * gs[l] * g;
                    u = r * gs[l] * h;
                } else {
                    s = gs[l] * f;
                    t = gs[l] * g;
                    u = gs[l] * h;
                }
                int v = js[l];
                q = byteBuffer.getFloat(16);
                r = byteBuffer.getFloat(20);
                Vector4f vector4f = new Vector4f(m, n, o, 1.0f);
                vector4f.transform(matrix4f);
                this.vertex(vector4f.x(), vector4f.y(), vector4f.z(), s, t, u, 1.0f, q, r, i, v, vector3f.x(), vector3f.y(), vector3f.z());
            }
        }
    }

    default public VertexConsumer vertex(Matrix4f matrix4f, float f, float g, float h) {
        Vector4f vector4f = new Vector4f(f, g, h, 1.0f);
        vector4f.transform(matrix4f);
        return this.vertex(vector4f.x(), vector4f.y(), vector4f.z());
    }

    default public VertexConsumer normal(Matrix3f matrix3f, float f, float g, float h) {
        Vector3f vector3f = new Vector3f(f, g, h);
        vector3f.transform(matrix3f);
        return this.normal(vector3f.x(), vector3f.y(), vector3f.z());
    }
}

