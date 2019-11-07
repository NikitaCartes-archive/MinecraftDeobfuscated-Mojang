package com.mojang.blaze3d.vertex;

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

@Environment(EnvType.CLIENT)
public interface VertexConsumer {
	Logger LOGGER = LogManager.getLogger();

	VertexConsumer vertex(double d, double e, double f);

	VertexConsumer color(int i, int j, int k, int l);

	VertexConsumer uv(float f, float g);

	VertexConsumer overlayCoords(int i, int j);

	VertexConsumer uv2(int i, int j);

	VertexConsumer normal(float f, float g, float h);

	void endVertex();

	default VertexConsumer color(float f, float g, float h, float i) {
		return this.color((int)(f * 255.0F), (int)(g * 255.0F), (int)(h * 255.0F), (int)(i * 255.0F));
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
		int[] js = bakedQuad.getVertices();
		Vec3i vec3i = bakedQuad.getDirection().getNormal();
		Vector3f vector3f = new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
		Matrix4f matrix4f = pose.pose();
		vector3f.transform(pose.normal());
		int j = 8;
		int k = js.length / 8;

		try (MemoryStack memoryStack = MemoryStack.stackPush()) {
			ByteBuffer byteBuffer = memoryStack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
			IntBuffer intBuffer = byteBuffer.asIntBuffer();

			for (int l = 0; l < k; l++) {
				intBuffer.clear();
				intBuffer.put(js, l * 8, 8);
				float m = byteBuffer.getFloat(0);
				float n = byteBuffer.getFloat(4);
				float o = byteBuffer.getFloat(8);
				byte b;
				byte c;
				byte d;
				if (bl) {
					int p = byteBuffer.get(12) & 255;
					int q = byteBuffer.get(13) & 255;
					int r = byteBuffer.get(14) & 255;
					b = (byte)((int)((float)p * fs[l] * f));
					c = (byte)((int)((float)q * fs[l] * g));
					d = (byte)((int)((float)r * fs[l] * h));
				} else {
					b = (byte)((int)(255.0F * fs[l] * f));
					c = (byte)((int)(255.0F * fs[l] * g));
					d = (byte)((int)(255.0F * fs[l] * h));
				}

				int p = is[l];
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

	default VertexConsumer vertex(Matrix4f matrix4f, float f, float g, float h) {
		Vector4f vector4f = new Vector4f(f, g, h, 1.0F);
		vector4f.transform(matrix4f);
		return this.vertex((double)vector4f.x(), (double)vector4f.y(), (double)vector4f.z());
	}

	default VertexConsumer normal(Matrix3f matrix3f, float f, float g, float h) {
		Vector3f vector3f = new Vector3f(f, g, h);
		vector3f.transform(matrix3f);
		return this.normal(vector3f.x(), vector3f.y(), vector3f.z());
	}
}
