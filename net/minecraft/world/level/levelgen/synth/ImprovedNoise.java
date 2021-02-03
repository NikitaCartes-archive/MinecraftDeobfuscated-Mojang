/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.synth;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public final class ImprovedNoise {
    private final byte[] p;
    public final double xo;
    public final double yo;
    public final double zo;

    public ImprovedNoise(RandomSource randomSource) {
        int i;
        this.xo = randomSource.nextDouble() * 256.0;
        this.yo = randomSource.nextDouble() * 256.0;
        this.zo = randomSource.nextDouble() * 256.0;
        this.p = new byte[256];
        for (i = 0; i < 256; ++i) {
            this.p[i] = (byte)i;
        }
        for (i = 0; i < 256; ++i) {
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
        double s;
        double i = d + this.xo;
        double j = e + this.yo;
        double k = f + this.zo;
        int l = Mth.floor(i);
        int m = Mth.floor(j);
        int n = Mth.floor(k);
        double o = i - (double)l;
        double p = j - (double)m;
        double q = k - (double)n;
        if (g != 0.0) {
            double r = h >= 0.0 && h < p ? h : p;
            s = (double)Mth.floor(r / g + (double)1.0E-7f) * g;
        } else {
            s = 0.0;
        }
        return this.sampleAndLerp(l, m, n, o, p - s, q, p);
    }

    private static double gradDot(int i, double d, double e, double f) {
        return SimplexNoise.dot(SimplexNoise.GRADIENT[i & 0xF], d, e, f);
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
        double h = ImprovedNoise.gradDot(this.p(n + k), d, e, f);
        double r = ImprovedNoise.gradDot(this.p(p + k), d - 1.0, e, f);
        double s = ImprovedNoise.gradDot(this.p(o + k), d, e - 1.0, f);
        double t = ImprovedNoise.gradDot(this.p(q + k), d - 1.0, e - 1.0, f);
        double u = ImprovedNoise.gradDot(this.p(n + k + 1), d, e, f - 1.0);
        double v = ImprovedNoise.gradDot(this.p(p + k + 1), d - 1.0, e, f - 1.0);
        double w = ImprovedNoise.gradDot(this.p(o + k + 1), d, e - 1.0, f - 1.0);
        double x = ImprovedNoise.gradDot(this.p(q + k + 1), d - 1.0, e - 1.0, f - 1.0);
        double y = Mth.smoothstep(d);
        double z = Mth.smoothstep(g);
        double aa = Mth.smoothstep(f);
        return Mth.lerp3(y, z, aa, h, r, s, t, u, v, w, x);
    }
}

