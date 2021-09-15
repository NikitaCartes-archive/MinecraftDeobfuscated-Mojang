package net.minecraft.client.renderer.culling;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.AABB;

@Environment(EnvType.CLIENT)
public class Frustum {
	public static final int OFFSET_STEP = 4;
	private final Vector4f[] frustumData = new Vector4f[6];
	private Vector4f viewVector;
	private double camX;
	private double camY;
	private double camZ;

	public Frustum(Matrix4f matrix4f, Matrix4f matrix4f2) {
		this.calculateFrustum(matrix4f, matrix4f2);
	}

	public Frustum(Frustum frustum) {
		System.arraycopy(frustum.frustumData, 0, this.frustumData, 0, frustum.frustumData.length);
		this.camX = frustum.camX;
		this.camY = frustum.camY;
		this.camZ = frustum.camZ;
		this.viewVector = frustum.viewVector;
	}

	public Frustum offsetToFullyIncludeCameraCube(int i) {
		double d = Math.floor(this.camX / (double)i) * (double)i;
		double e = Math.floor(this.camY / (double)i) * (double)i;
		double f = Math.floor(this.camZ / (double)i) * (double)i;
		double g = Math.ceil(this.camX / (double)i) * (double)i;
		double h = Math.ceil(this.camY / (double)i) * (double)i;

		for (double j = Math.ceil(this.camZ / (double)i) * (double)i;
			!this.cubeCompletelyInFrustum(
				(float)(d - this.camX), (float)(e - this.camY), (float)(f - this.camZ), (float)(g - this.camX), (float)(h - this.camY), (float)(j - this.camZ)
			);
			this.camZ = this.camZ - (double)(this.viewVector.z() * 4.0F)
		) {
			this.camX = this.camX - (double)(this.viewVector.x() * 4.0F);
			this.camY = this.camY - (double)(this.viewVector.y() * 4.0F);
		}

		return this;
	}

	public void prepare(double d, double e, double f) {
		this.camX = d;
		this.camY = e;
		this.camZ = f;
	}

	private void calculateFrustum(Matrix4f matrix4f, Matrix4f matrix4f2) {
		Matrix4f matrix4f3 = matrix4f2.copy();
		matrix4f3.multiply(matrix4f);
		matrix4f3.transpose();
		this.viewVector = new Vector4f(0.0F, 0.0F, 1.0F, 0.0F);
		this.viewVector.transform(matrix4f3);
		this.getPlane(matrix4f3, -1, 0, 0, 0);
		this.getPlane(matrix4f3, 1, 0, 0, 1);
		this.getPlane(matrix4f3, 0, -1, 0, 2);
		this.getPlane(matrix4f3, 0, 1, 0, 3);
		this.getPlane(matrix4f3, 0, 0, -1, 4);
		this.getPlane(matrix4f3, 0, 0, 1, 5);
	}

	private void getPlane(Matrix4f matrix4f, int i, int j, int k, int l) {
		Vector4f vector4f = new Vector4f((float)i, (float)j, (float)k, 1.0F);
		vector4f.transform(matrix4f);
		vector4f.normalize();
		this.frustumData[l] = vector4f;
	}

	public boolean isVisible(AABB aABB) {
		return this.cubeInFrustum(aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ);
	}

	private boolean cubeInFrustum(double d, double e, double f, double g, double h, double i) {
		float j = (float)(d - this.camX);
		float k = (float)(e - this.camY);
		float l = (float)(f - this.camZ);
		float m = (float)(g - this.camX);
		float n = (float)(h - this.camY);
		float o = (float)(i - this.camZ);
		return this.cubeInFrustum(j, k, l, m, n, o);
	}

	private boolean cubeInFrustum(float f, float g, float h, float i, float j, float k) {
		for (int l = 0; l < 6; l++) {
			Vector4f vector4f = this.frustumData[l];
			if (!(vector4f.dot(new Vector4f(f, g, h, 1.0F)) > 0.0F)
				&& !(vector4f.dot(new Vector4f(i, g, h, 1.0F)) > 0.0F)
				&& !(vector4f.dot(new Vector4f(f, j, h, 1.0F)) > 0.0F)
				&& !(vector4f.dot(new Vector4f(i, j, h, 1.0F)) > 0.0F)
				&& !(vector4f.dot(new Vector4f(f, g, k, 1.0F)) > 0.0F)
				&& !(vector4f.dot(new Vector4f(i, g, k, 1.0F)) > 0.0F)
				&& !(vector4f.dot(new Vector4f(f, j, k, 1.0F)) > 0.0F)
				&& !(vector4f.dot(new Vector4f(i, j, k, 1.0F)) > 0.0F)) {
				return false;
			}
		}

		return true;
	}

	private boolean cubeCompletelyInFrustum(float f, float g, float h, float i, float j, float k) {
		for (int l = 0; l < 6; l++) {
			Vector4f vector4f = this.frustumData[l];
			if (vector4f.dot(new Vector4f(f, g, h, 1.0F)) <= 0.0F) {
				return false;
			}

			if (vector4f.dot(new Vector4f(i, g, h, 1.0F)) <= 0.0F) {
				return false;
			}

			if (vector4f.dot(new Vector4f(f, j, h, 1.0F)) <= 0.0F) {
				return false;
			}

			if (vector4f.dot(new Vector4f(i, j, h, 1.0F)) <= 0.0F) {
				return false;
			}

			if (vector4f.dot(new Vector4f(f, g, k, 1.0F)) <= 0.0F) {
				return false;
			}

			if (vector4f.dot(new Vector4f(i, g, k, 1.0F)) <= 0.0F) {
				return false;
			}

			if (vector4f.dot(new Vector4f(f, j, k, 1.0F)) <= 0.0F) {
				return false;
			}

			if (vector4f.dot(new Vector4f(i, j, k, 1.0F)) <= 0.0F) {
				return false;
			}
		}

		return true;
	}
}
