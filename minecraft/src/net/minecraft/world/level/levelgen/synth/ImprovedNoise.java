package net.minecraft.world.level.levelgen.synth;

import java.util.Random;
import net.minecraft.util.Mth;

public final class ImprovedNoise {
	private final byte[] p;
	public final double xo;
	public final double yo;
	public final double zo;

	public ImprovedNoise(Random random) {
		this.xo = random.nextDouble() * 256.0;
		this.yo = random.nextDouble() * 256.0;
		this.zo = random.nextDouble() * 256.0;
		this.p = new byte[256];

		for (int i = 0; i < 256; i++) {
			this.p[i] = (byte)i;
		}

		for (int i = 0; i < 256; i++) {
			int j = random.nextInt(256 - i);
			byte b = this.p[i];
			this.p[i] = this.p[i + j];
			this.p[i + j] = b;
		}
	}

	public double noise(double d, double e, double f, double g, double h) {
		double i = d + this.xo;
		double j = e + this.yo;
		double k = f + this.zo;
		int l = Mth.floor(i);
		int m = Mth.floor(j);
		int n = Mth.floor(k);
		double o = i - (double)l;
		double p = j - (double)m;
		double q = k - (double)n;
		double r = Mth.smoothstep(o);
		double s = Mth.smoothstep(p);
		double t = Mth.smoothstep(q);
		double v;
		if (g != 0.0) {
			double u = Math.min(h, p);
			v = (double)Mth.floor(u / g) * g;
		} else {
			v = 0.0;
		}

		return this.sampleAndLerp(l, m, n, o, p - v, q, r, s, t);
	}

	private static double gradDot(int i, double d, double e, double f) {
		int j = i & 15;
		return SimplexNoise.dot(SimplexNoise.GRADIENT[j], d, e, f);
	}

	private int p(int i) {
		return this.p[i & 0xFF] & 0xFF;
	}

	public double sampleAndLerp(int i, int j, int k, double d, double e, double f, double g, double h, double l) {
		int m = this.p(i) + j;
		int n = this.p(m) + k;
		int o = this.p(m + 1) + k;
		int p = this.p(i + 1) + j;
		int q = this.p(p) + k;
		int r = this.p(p + 1) + k;
		double s = gradDot(this.p(n), d, e, f);
		double t = gradDot(this.p(q), d - 1.0, e, f);
		double u = gradDot(this.p(o), d, e - 1.0, f);
		double v = gradDot(this.p(r), d - 1.0, e - 1.0, f);
		double w = gradDot(this.p(n + 1), d, e, f - 1.0);
		double x = gradDot(this.p(q + 1), d - 1.0, e, f - 1.0);
		double y = gradDot(this.p(o + 1), d, e - 1.0, f - 1.0);
		double z = gradDot(this.p(r + 1), d - 1.0, e - 1.0, f - 1.0);
		return Mth.lerp3(g, h, l, s, t, u, v, w, x, y, z);
	}
}
