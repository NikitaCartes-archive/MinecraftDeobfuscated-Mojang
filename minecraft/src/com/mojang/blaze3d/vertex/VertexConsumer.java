package com.mojang.blaze3d.vertex;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
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
		int i = 8;
		int j = js.length / 8;

		try (MemoryStack memoryStack = MemoryStack.stackPush()) {
			ByteBuffer byteBuffer = memoryStack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
			IntBuffer intBuffer = byteBuffer.asIntBuffer();

			for (int k = 0; k < j; k++) {
				intBuffer.clear();
				intBuffer.put(js, k * 8, 8);
				float l = byteBuffer.getFloat(0);
				float m = byteBuffer.getFloat(4);
				float n = byteBuffer.getFloat(8);
				byte b;
				byte c;
				byte d;
				if (bl) {
					int o = byteBuffer.get(12) & 255;
					int p = byteBuffer.get(13) & 255;
					int q = byteBuffer.get(14) & 255;
					b = (byte)((int)((float)o * fs[k] * f));
					c = (byte)((int)((float)p * fs[k] * g));
					d = (byte)((int)((float)q * fs[k] * h));
				} else {
					b = (byte)((int)(255.0F * fs[k] * f));
					c = (byte)((int)(255.0F * fs[k] * g));
					d = (byte)((int)(255.0F * fs[k] * h));
				}

				int o = is[k];
				float r = byteBuffer.getFloat(16);
				float s = byteBuffer.getFloat(20);
				this.vertex(matrix4f, l, m, n);
				this.color(b, c, d, 255);
				this.uv(r, s);
				this.uv2(o);
				this.normal((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
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
