package net.minecraft.client.renderer.culling;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class FrustumData {
	public final float[][] frustumData = new float[6][4];
	public final float[] projectionMatrix = new float[16];
	public final float[] modelViewMatrix = new float[16];
	public final float[] clip = new float[16];

	private double discriminant(float[] fs, double d, double e, double f) {
		return (double)fs[0] * d + (double)fs[1] * e + (double)fs[2] * f + (double)fs[3];
	}

	public boolean cubeInFrustum(double d, double e, double f, double g, double h, double i) {
		for (int j = 0; j < 6; j++) {
			float[] fs = this.frustumData[j];
			if (!(this.discriminant(fs, d, e, f) > 0.0)
				&& !(this.discriminant(fs, g, e, f) > 0.0)
				&& !(this.discriminant(fs, d, h, f) > 0.0)
				&& !(this.discriminant(fs, g, h, f) > 0.0)
				&& !(this.discriminant(fs, d, e, i) > 0.0)
				&& !(this.discriminant(fs, g, e, i) > 0.0)
				&& !(this.discriminant(fs, d, h, i) > 0.0)
				&& !(this.discriminant(fs, g, h, i) > 0.0)) {
				return false;
			}
		}

		return true;
	}
}
