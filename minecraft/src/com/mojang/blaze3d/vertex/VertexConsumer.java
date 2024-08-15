package com.mojang.blaze3d.vertex;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ARGB;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

@Environment(EnvType.CLIENT)
public interface VertexConsumer {
	VertexConsumer addVertex(float f, float g, float h);

	VertexConsumer setColor(int i, int j, int k, int l);

	VertexConsumer setUv(float f, float g);

	VertexConsumer setUv1(int i, int j);

	VertexConsumer setUv2(int i, int j);

	VertexConsumer setNormal(float f, float g, float h);

	default void addVertex(float f, float g, float h, int i, float j, float k, int l, int m, float n, float o, float p) {
		this.addVertex(f, g, h);
		this.setColor(i);
		this.setUv(j, k);
		this.setOverlay(l);
		this.setLight(m);
		this.setNormal(n, o, p);
	}

	default VertexConsumer setColor(float f, float g, float h, float i) {
		return this.setColor((int)(f * 255.0F), (int)(g * 255.0F), (int)(h * 255.0F), (int)(i * 255.0F));
	}

	default VertexConsumer setColor(int i) {
		return this.setColor(ARGB.red(i), ARGB.green(i), ARGB.blue(i), ARGB.alpha(i));
	}

	default VertexConsumer setWhiteAlpha(int i) {
		return this.setColor(ARGB.color(i, -1));
	}

	default VertexConsumer setLight(int i) {
		return this.setUv2(i & 65535, i >> 16 & 65535);
	}

	default VertexConsumer setOverlay(int i) {
		return this.setUv1(i & 65535, i >> 16 & 65535);
	}

	default void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float f, float g, float h, float i, int j, int k) {
		this.putBulkData(pose, bakedQuad, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, f, g, h, i, new int[]{j, j, j, j}, k, false);
	}

	default void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float[] fs, float f, float g, float h, float i, int[] is, int j, boolean bl) {
		int[] js = bakedQuad.getVertices();
		Vec3i vec3i = bakedQuad.getDirection().getUnitVec3i();
		Matrix4f matrix4f = pose.pose();
		Vector3f vector3f = pose.transformNormal((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ(), new Vector3f());
		int k = 8;
		int l = js.length / 8;
		int m = (int)(i * 255.0F);

		try (MemoryStack memoryStack = MemoryStack.stackPush()) {
			ByteBuffer byteBuffer = memoryStack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
			IntBuffer intBuffer = byteBuffer.asIntBuffer();

			for (int n = 0; n < l; n++) {
				intBuffer.clear();
				intBuffer.put(js, n * 8, 8);
				float o = byteBuffer.getFloat(0);
				float p = byteBuffer.getFloat(4);
				float q = byteBuffer.getFloat(8);
				float u;
				float v;
				float w;
				if (bl) {
					float r = (float)(byteBuffer.get(12) & 255);
					float s = (float)(byteBuffer.get(13) & 255);
					float t = (float)(byteBuffer.get(14) & 255);
					u = r * fs[n] * f;
					v = s * fs[n] * g;
					w = t * fs[n] * h;
				} else {
					u = fs[n] * f * 255.0F;
					v = fs[n] * g * 255.0F;
					w = fs[n] * h * 255.0F;
				}

				int x = ARGB.color(m, (int)u, (int)v, (int)w);
				int y = is[n];
				float t = byteBuffer.getFloat(16);
				float z = byteBuffer.getFloat(20);
				Vector3f vector3f2 = matrix4f.transformPosition(o, p, q, new Vector3f());
				this.addVertex(vector3f2.x(), vector3f2.y(), vector3f2.z(), x, t, z, j, y, vector3f.x(), vector3f.y(), vector3f.z());
			}
		}
	}

	default VertexConsumer addVertex(Vector3f vector3f) {
		return this.addVertex(vector3f.x(), vector3f.y(), vector3f.z());
	}

	default VertexConsumer addVertex(PoseStack.Pose pose, Vector3f vector3f) {
		return this.addVertex(pose, vector3f.x(), vector3f.y(), vector3f.z());
	}

	default VertexConsumer addVertex(PoseStack.Pose pose, float f, float g, float h) {
		return this.addVertex(pose.pose(), f, g, h);
	}

	default VertexConsumer addVertex(Matrix4f matrix4f, float f, float g, float h) {
		Vector3f vector3f = matrix4f.transformPosition(f, g, h, new Vector3f());
		return this.addVertex(vector3f.x(), vector3f.y(), vector3f.z());
	}

	default VertexConsumer setNormal(PoseStack.Pose pose, float f, float g, float h) {
		Vector3f vector3f = pose.transformNormal(f, g, h, new Vector3f());
		return this.setNormal(vector3f.x(), vector3f.y(), vector3f.z());
	}

	default VertexConsumer setNormal(PoseStack.Pose pose, Vector3f vector3f) {
		return this.setNormal(pose, vector3f.x(), vector3f.y(), vector3f.z());
	}
}
