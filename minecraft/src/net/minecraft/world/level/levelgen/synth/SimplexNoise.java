package net.minecraft.world.level.levelgen.synth;

import java.util.Random;
import net.minecraft.util.Mth;

public class SimplexNoise {
	protected static final int[][] GRADIENT = new int[][]{
		{1, 1, 0},
		{-1, 1, 0},
		{1, -1, 0},
		{-1, -1, 0},
		{1, 0, 1},
		{-1, 0, 1},
		{1, 0, -1},
		{-1, 0, -1},
		{0, 1, 1},
		{0, -1, 1},
		{0, 1, -1},
		{0, -1, -1},
		{1, 1, 0},
		{0, -1, 1},
		{-1, 1, 0},
		{0, -1, -1}
	};
	private static final double SQRT_3 = Math.sqrt(3.0);
	private static final double F2 = 0.5 * (SQRT_3 - 1.0);
	private static final double G2 = (3.0 - SQRT_3) / 6.0;
	private final int[] p = new int[512];
	public final double xo;
	public final double yo;
	public final double zo;

	public SimplexNoise(Random random) {
		this.xo = random.nextDouble() * 256.0;
		this.yo = random.nextDouble() * 256.0;
		this.zo = random.nextDouble() * 256.0;
		int i = 0;

		while (i < 256) {
			this.p[i] = i++;
		}

		for (int ix = 0; ix < 256; ix++) {
			int j = random.nextInt(256 - ix);
			int k = this.p[ix];
			this.p[ix] = this.p[j + ix];
			this.p[j + ix] = k;
		}
	}

	private int p(int i) {
		return this.p[i & 0xFF];
	}

	protected static double dot(int[] is, double d, double e, double f) {
		return (double)is[0] * d + (double)is[1] * e + (double)is[2] * f;
	}

	private double getCornerNoise3D(int i, double d, double e, double f, double g) {
		double h = g - d * d - e * e - f * f;
		double j;
		if (h < 0.0) {
			j = 0.0;
		} else {
			h *= h;
			j = h * h * dot(GRADIENT[i], d, e, f);
		}

		return j;
	}

	public double getValue(double d, double e) {
		double f = (d + e) * F2;
		int i = Mth.floor(d + f);
		int j = Mth.floor(e + f);
		double g = (double)(i + j) * G2;
		double h = (double)i - g;
		double k = (double)j - g;
		double l = d - h;
		double m = e - k;
		int n;
		int o;
		if (l > m) {
			n = 1;
			o = 0;
		} else {
			n = 0;
			o = 1;
		}

		double p = l - (double)n + G2;
		double q = m - (double)o + G2;
		double r = l - 1.0 + 2.0 * G2;
		double s = m - 1.0 + 2.0 * G2;
		int t = i & 0xFF;
		int u = j & 0xFF;
		int v = this.p(t + this.p(u)) % 12;
		int w = this.p(t + n + this.p(u + o)) % 12;
		int x = this.p(t + 1 + this.p(u + 1)) % 12;
		double y = this.getCornerNoise3D(v, l, m, 0.0, 0.5);
		double z = this.getCornerNoise3D(w, p, q, 0.0, 0.5);
		double aa = this.getCornerNoise3D(x, r, s, 0.0, 0.5);
		return 70.0 * (y + z + aa);
	}

	public double getValue(double d, double e, double f) {
		double g = 0.3333333333333333;
		double h = (d + e + f) * 0.3333333333333333;
		int i = Mth.floor(d + h);
		int j = Mth.floor(e + h);
		int k = Mth.floor(f + h);
		double l = 0.16666666666666666;
		double m = (double)(i + j + k) * 0.16666666666666666;
		double n = (double)i - m;
		double o = (double)j - m;
		double p = (double)k - m;
		double q = d - n;
		double r = e - o;
		double s = f - p;
		int t;
		int u;
		int v;
		int w;
		int x;
		int y;
		if (q >= r) {
			if (r >= s) {
				t = 1;
				u = 0;
				v = 0;
				w = 1;
				x = 1;
				y = 0;
			} else if (q >= s) {
				t = 1;
				u = 0;
				v = 0;
				w = 1;
				x = 0;
				y = 1;
			} else {
				t = 0;
				u = 0;
				v = 1;
				w = 1;
				x = 0;
				y = 1;
			}
		} else if (r < s) {
			t = 0;
			u = 0;
			v = 1;
			w = 0;
			x = 1;
			y = 1;
		} else if (q < s) {
			t = 0;
			u = 1;
			v = 0;
			w = 0;
			x = 1;
			y = 1;
		} else {
			t = 0;
			u = 1;
			v = 0;
			w = 1;
			x = 1;
			y = 0;
		}

		double z = q - (double)t + 0.16666666666666666;
		double aa = r - (double)u + 0.16666666666666666;
		double ab = s - (double)v + 0.16666666666666666;
		double ac = q - (double)w + 0.3333333333333333;
		double ad = r - (double)x + 0.3333333333333333;
		double ae = s - (double)y + 0.3333333333333333;
		double af = q - 1.0 + 0.5;
		double ag = r - 1.0 + 0.5;
		double ah = s - 1.0 + 0.5;
		int ai = i & 0xFF;
		int aj = j & 0xFF;
		int ak = k & 0xFF;
		int al = this.p(ai + this.p(aj + this.p(ak))) % 12;
		int am = this.p(ai + t + this.p(aj + u + this.p(ak + v))) % 12;
		int an = this.p(ai + w + this.p(aj + x + this.p(ak + y))) % 12;
		int ao = this.p(ai + 1 + this.p(aj + 1 + this.p(ak + 1))) % 12;
		double ap = this.getCornerNoise3D(al, q, r, s, 0.6);
		double aq = this.getCornerNoise3D(am, z, aa, ab, 0.6);
		double ar = this.getCornerNoise3D(an, ac, ad, ae, 0.6);
		double as = this.getCornerNoise3D(ao, af, ag, ah, 0.6);
		return 32.0 * (ap + aq + ar + as);
	}
}
