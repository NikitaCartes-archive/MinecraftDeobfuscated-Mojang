package com.mojang.blaze3d.vertex;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import net.minecraft.util.FastColor;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

@Environment(EnvType.CLIENT)
public interface VertexConsumer {
	VertexConsumer vertex(double d, double e, double f);

	VertexConsumer color(int i, int j, int k, int l);

	VertexConsumer uv(float f, float g);

	VertexConsumer overlayCoords(int i, int j);

	VertexConsumer uv2(int i, int j);

	VertexConsumer normal(float f, float g, float h);

	void endVertex();

	default void vertex(float f, float g, float h, float i, float j, float k, float l, float m, float n, int o, int p, float q, float r, float s) {
		this.vertex((double)f, (double)g, (double)h);
		this.color(i, j, k, l);
		this.uv(m, n);
		this.overlayCoords(o);
		this.uv2(p);
		this.normal(q, r, s);
		this.endVertex();
	}

	void defaultColor(int i, int j, int k, int l);

	void unsetDefaultColor();

	default VertexConsumer color(float f, float g, float h, float i) {
		return this.color((int)(f * 255.0F), (int)(g * 255.0F), (int)(h * 255.0F), (int)(i * 255.0F));
	}

	default VertexConsumer color(int i) {
		return this.color(FastColor.ARGB32.red(i), FastColor.ARGB32.green(i), FastColor.ARGB32.blue(i), FastColor.ARGB32.alpha(i));
	}

	default VertexConsumer uv2(int i) {
		return this.uv2(i & 65535, i >> 16 & 65535);
	}

	default VertexConsumer overlayCoords(int i) {
		return this.overlayCoords(i & 65535, i >> 16 & 65535);
	}

	default void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float f, float g, float h, int i, int j) {
		this.putBulkData(pose, bakedQuad, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, f, g, h, new int[]{i, i, i, i}, j, false);
	}

	default void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float[] fs, float f, float g, float h, int[] is, int i, boolean bl) {
		float[] gs = new float[]{fs[0], fs[1], fs[2], fs[3]};
		int[] js = new int[]{is[0], is[1], is[2], is[3]};
		int[] ks = bakedQuad.getVertices();
		Vec3i vec3i = bakedQuad.getDirection().getNormal();
		Matrix4f matrix4f = pose.pose();
		Vector3f vector3f = pose.transformNormal((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ(), new Vector3f());
		int j = 8;
		int k = ks.length / 8;

		try (MemoryStack memoryStack = MemoryStack.stackPush()) {
			ByteBuffer byteBuffer = memoryStack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
			IntBuffer intBuffer = byteBuffer.asIntBuffer();

			for (int l = 0; l < k; l++) {
				intBuffer.clear();
				intBuffer.put(ks, l * 8, 8);
				float m = byteBuffer.getFloat(0);
				float n = byteBuffer.getFloat(4);
				float o = byteBuffer.getFloat(8);
				float s;
				float t;
				float u;
				if (bl) {
					float p = (float)(byteBuffer.get(12) & 255) / 255.0F;
					float q = (float)(byteBuffer.get(13) & 255) / 255.0F;
					float r = (float)(byteBuffer.get(14) & 255) / 255.0F;
					s = p * gs[l] * f;
					t = q * gs[l] * g;
					u = r * gs[l] * h;
				} else {
					s = gs[l] * f;
					t = gs[l] * g;
					u = gs[l] * h;
				}

				int v = js[l];
				float q = byteBuffer.getFloat(16);
				float r = byteBuffer.getFloat(20);
				Vector4f vector4f = matrix4f.transform(new Vector4f(m, n, o, 1.0F));
				this.vertex(vector4f.x(), vector4f.y(), vector4f.z(), s, t, u, 1.0F, q, r, i, v, vector3f.x(), vector3f.y(), vector3f.z());
			}
		}
	}

	default VertexConsumer vertex(PoseStack.Pose pose, float f, float g, float h) {
		return this.vertex(pose.pose(), f, g, h);
	}

	default VertexConsumer vertex(Matrix4f matrix4f, float f, float g, float h) {
		Vector3f vector3f = matrix4f.transformPosition(f, g, h, new Vector3f());
		return this.vertex((double)vector3f.x(), (double)vector3f.y(), (double)vector3f.z());
	}

	default VertexConsumer normal(PoseStack.Pose pose, float f, float g, float h) {
		Vector3f vector3f = pose.transformNormal(f, g, h, new Vector3f());
		return this.normal(vector3f.x(), vector3f.y(), vector3f.z());
	}
}
