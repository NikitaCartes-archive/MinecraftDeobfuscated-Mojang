package net.minecraft.world.level.levelgen.synth;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.RandomSource;

public final class ImprovedNoise {
	private static final float SHIFT_UP_EPSILON = 1.0E-7F;
	private final byte[] p;
	public final double xo;
	public final double yo;
	public final double zo;

	public ImprovedNoise(RandomSource randomSource) {
		this.xo = randomSource.nextDouble() * 256.0;
		this.yo = randomSource.nextDouble() * 256.0;
		this.zo = randomSource.nextDouble() * 256.0;
		this.p = new byte[256];

		for (int i = 0; i < 256; i++) {
			this.p[i] = (byte)i;
		}

		for (int i = 0; i < 256; i++) {
			int j = randomSource.nextInt(256 - i);
			byte b = this.p[i];
			this.p[i] = this.p[i + j];
			this.p[i + j] = b;
		}
	}

	public double noise(double d, double e, double f) {
		return this.noise(d, e, f, 0.0, 0.0);
	}

	@Deprecated
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
		double s;
		if (g != 0.0) {
			double r;
			if (h >= 0.0 && h < p) {
				r = h;
			} else {
				r = p;
			}

			s = (double)Mth.floor(r / g + 1.0E-7F) * g;
		} else {
			s = 0.0;
		}

		return this.sampleAndLerp(l, m, n, o, p - s, q, p);
	}

	public double noiseWithDerivative(double d, double e, double f, double[] ds) {
		double g = d + this.xo;
		double h = e + this.yo;
		double i = f + this.zo;
		int j = Mth.floor(g);
		int k = Mth.floor(h);
		int l = Mth.floor(i);
		double m = g - (double)j;
		double n = h - (double)k;
		double o = i - (double)l;
		return this.sampleWithDerivative(j, k, l, m, n, o, ds);
	}

	private static double gradDot(int i, double d, double e, double f) {
		return SimplexNoise.dot(SimplexNoise.GRADIENT[i & 15], d, e, f);
	}

	private int p(int i) {
		return this.p[i & 0xFF] & 0xFF;
	}

	private double sampleAndLerp(int i, int j, int k, double d, double e, double f, double g) {
		int l = this.p(i);
		int m = this.p(i + 1);
		int n = this.p(l + j);
		int o = this.p(l + j + 1);
		int p = this.p(m + j);
		int q = this.p(m + j + 1);
		double h = gradDot(this.p(n + k), d, e, f);
		double r = gradDot(this.p(p + k), d - 1.0, e, f);
		double s = gradDot(this.p(o + k), d, e - 1.0, f);
		double t = gradDot(this.p(q + k), d - 1.0, e - 1.0, f);
		double u = gradDot(this.p(n + k + 1), d, e, f - 1.0);
		double v = gradDot(this.p(p + k + 1), d - 1.0, e, f - 1.0);
		double w = gradDot(this.p(o + k + 1), d, e - 1.0, f - 1.0);
		double x = gradDot(this.p(q + k + 1), d - 1.0, e - 1.0, f - 1.0);
		double y = Mth.smoothstep(d);
		double z = Mth.smoothstep(g);
		double aa = Mth.smoothstep(f);
		return Mth.lerp3(y, z, aa, h, r, s, t, u, v, w, x);
	}

	private double sampleWithDerivative(int i, int j, int k, double d, double e, double f, double[] ds) {
		int l = this.p(i);
		int m = this.p(i + 1);
		int n = this.p(l + j);
		int o = this.p(l + j + 1);
		int p = this.p(m + j);
		int q = this.p(m + j + 1);
		int r = this.p(n + k);
		int s = this.p(p + k);
		int t = this.p(o + k);
		int u = this.p(q + k);
		int v = this.p(n + k + 1);
		int w = this.p(p + k + 1);
		int x = this.p(o + k + 1);
		int y = this.p(q + k + 1);
		int[] is = SimplexNoise.GRADIENT[r & 15];
		int[] js = SimplexNoise.GRADIENT[s & 15];
		int[] ks = SimplexNoise.GRADIENT[t & 15];
		int[] ls = SimplexNoise.GRADIENT[u & 15];
		int[] ms = SimplexNoise.GRADIENT[v & 15];
		int[] ns = SimplexNoise.GRADIENT[w & 15];
		int[] os = SimplexNoise.GRADIENT[x & 15];
		int[] ps = SimplexNoise.GRADIENT[y & 15];
		double g = SimplexNoise.dot(is, d, e, f);
		double h = SimplexNoise.dot(js, d - 1.0, e, f);
		double z = SimplexNoise.dot(ks, d, e - 1.0, f);
		double aa = SimplexNoise.dot(ls, d - 1.0, e - 1.0, f);
		double ab = SimplexNoise.dot(ms, d, e, f - 1.0);
		double ac = SimplexNoise.dot(ns, d - 1.0, e, f - 1.0);
		double ad = SimplexNoise.dot(os, d, e - 1.0, f - 1.0);
		double ae = SimplexNoise.dot(ps, d - 1.0, e - 1.0, f - 1.0);
		double af = Mth.smoothstep(d);
		double ag = Mth.smoothstep(e);
		double ah = Mth.smoothstep(f);
		double ai = Mth.lerp3(af, ag, ah, (double)is[0], (double)js[0], (double)ks[0], (double)ls[0], (double)ms[0], (double)ns[0], (double)os[0], (double)ps[0]);
		double aj = Mth.lerp3(af, ag, ah, (double)is[1], (double)js[1], (double)ks[1], (double)ls[1], (double)ms[1], (double)ns[1], (double)os[1], (double)ps[1]);
		double ak = Mth.lerp3(af, ag, ah, (double)is[2], (double)js[2], (double)ks[2], (double)ls[2], (double)ms[2], (double)ns[2], (double)os[2], (double)ps[2]);
		double al = Mth.lerp2(ag, ah, h - g, aa - z, ac - ab, ae - ad);
		double am = Mth.lerp2(ah, af, z - g, ad - ab, aa - h, ae - ac);
		double an = Mth.lerp2(af, ag, ab - g, ac - h, ad - z, ae - aa);
		double ao = Mth.smoothstepDerivative(d);
		double ap = Mth.smoothstepDerivative(e);
		double aq = Mth.smoothstepDerivative(f);
		double ar = ai + ao * al;
		double as = aj + ap * am;
		double at = ak + aq * an;
		ds[0] += ar;
		ds[1] += as;
		ds[2] += at;
		return Mth.lerp3(af, ag, ah, g, h, z, aa, ab, ac, ad, ae);
	}
}
