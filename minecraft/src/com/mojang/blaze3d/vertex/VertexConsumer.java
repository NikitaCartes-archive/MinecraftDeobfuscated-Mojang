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
import net.minecraft.util.Mth;
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

	void defaultOverlayCoords(int i, int j);

	void unsetDefaultOverlayCoords();

	default VertexConsumer color(float f, float g, float h, float i) {
		return this.color((int)(f * 255.0F), (int)(g * 255.0F), (int)(h * 255.0F), (int)(i * 255.0F));
	}

	default VertexConsumer uv2(int i) {
		return this.uv2(i & 65535, i >> 16 & 65535);
	}

	default void putBulkData(Matrix4f matrix4f, BakedQuad bakedQuad, float f, float g, float h, int i) {
		this.putBulkData(matrix4f, bakedQuad, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, f, g, h, new int[]{i, i, i, i}, false);
	}

	default void putBulkData(Matrix4f matrix4f, BakedQuad bakedQuad, float[] fs, float f, float g, float h, int[] is, boolean bl) {
		int[] js = bakedQuad.getVertices();
		Vec3i vec3i = bakedQuad.getDirection().getNormal();
		Vector3f vector3f = new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
		Matrix3f matrix3f = new Matrix3f(matrix4f);
		matrix3f.transpose();
		float i = matrix3f.adjugateAndDet();
		if (i < 1.0E-5F) {
			LOGGER.warn("Could not invert matrix while baking vertex: " + matrix4f);
		} else {
			float j = matrix3f.determinant();
			matrix3f.mul(Mth.fastInvCubeRoot(j));
		}

		vector3f.transform(matrix3f);
		int k = 8;
		int l = js.length / 8;

		try (MemoryStack memoryStack = MemoryStack.stackPush()) {
			ByteBuffer byteBuffer = memoryStack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
			IntBuffer intBuffer = byteBuffer.asIntBuffer();

			for (int m = 0; m < l; m++) {
				intBuffer.clear();
				intBuffer.put(js, m * 8, 8);
				float n = byteBuffer.getFloat(0);
				float o = byteBuffer.getFloat(4);
				float p = byteBuffer.getFloat(8);
				byte b;
				byte c;
				byte d;
				if (bl) {
					int q = byteBuffer.get(12) & 255;
					int r = byteBuffer.get(13) & 255;
					int s = byteBuffer.get(14) & 255;
					b = (byte)((int)((float)q * fs[m] * f));
					c = (byte)((int)((float)r * fs[m] * g));
					d = (byte)((int)((float)s * fs[m] * h));
				} else {
					b = (byte)((int)(255.0F * fs[m] * f));
					c = (byte)((int)(255.0F * fs[m] * g));
					d = (byte)((int)(255.0F * fs[m] * h));
				}

				int q = is[m];
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

	default VertexConsumer vertex(Matrix4f matrix4f, float f, float g, float h) {
		Vector4f vector4f = new Vector4f(f, g, h, 1.0F);
		vector4f.transform(matrix4f);
		return this.vertex((double)vector4f.x(), (double)vector4f.y(), (double)vector4f.z());
	}
}
