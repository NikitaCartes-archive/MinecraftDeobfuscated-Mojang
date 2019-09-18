/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import java.util.Random;
import java.util.UUID;
import java.util.function.IntPredicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.Vec3i;
import org.apache.commons.lang3.math.NumberUtils;

public class Mth {
    public static final float SQRT_OF_TWO = Mth.sqrt(2.0f);
    private static final float[] SIN = Util.make(new float[65536], fs -> {
        for (int i = 0; i < ((float[])fs).length; ++i) {
            fs[i] = (float)Math.sin((double)i * Math.PI * 2.0 / 65536.0);
        }
    });
    private static final Random RANDOM = new Random();
    private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};
    private static final double FRAC_BIAS = Double.longBitsToDouble(4805340802404319232L);
    private static final double[] ASIN_TAB = new double[257];
    private static final double[] COS_TAB = new double[257];

    public static float sin(float f) {
        return SIN[(int)(f * 10430.378f) & 0xFFFF];
    }

    public static float cos(float f) {
        return SIN[(int)(f * 10430.378f + 16384.0f) & 0xFFFF];
    }

    public static float sqrt(float f) {
        return (float)Math.sqrt(f);
    }

    public static float sqrt(double d) {
        return (float)Math.sqrt(d);
    }

    public static int floor(float f) {
        int i = (int)f;
        return f < (float)i ? i - 1 : i;
    }

    @Environment(value=EnvType.CLIENT)
    public static int fastFloor(double d) {
        return (int)(d + 1024.0) - 1024;
    }

    public static int floor(double d) {
        int i = (int)d;
        return d < (double)i ? i - 1 : i;
    }

    public static long lfloor(double d) {
        long l = (long)d;
        return d < (double)l ? l - 1L : l;
    }

    @Environment(value=EnvType.CLIENT)
    public static int absFloor(double d) {
        return (int)(d >= 0.0 ? d : -d + 1.0);
    }

    public static float abs(float f) {
        return Math.abs(f);
    }

    public static int abs(int i) {
        return Math.abs(i);
    }

    public static int ceil(float f) {
        int i = (int)f;
        return f > (float)i ? i + 1 : i;
    }

    public static int ceil(double d) {
        int i = (int)d;
        return d > (double)i ? i + 1 : i;
    }

    public static int clamp(int i, int j, int k) {
        if (i < j) {
            return j;
        }
        if (i > k) {
            return k;
        }
        return i;
    }

    public static float clamp(float f, float g, float h) {
        if (f < g) {
            return g;
        }
        if (f > h) {
            return h;
        }
        return f;
    }

    public static double clamp(double d, double e, double f) {
        if (d < e) {
            return e;
        }
        if (d > f) {
            return f;
        }
        return d;
    }

    public static double clampedLerp(double d, double e, double f) {
        if (f < 0.0) {
            return d;
        }
        if (f > 1.0) {
            return e;
        }
        return Mth.lerp(f, d, e);
    }

    public static double absMax(double d, double e) {
        if (d < 0.0) {
            d = -d;
        }
        if (e < 0.0) {
            e = -e;
        }
        return d > e ? d : e;
    }

    public static int intFloorDiv(int i, int j) {
        return Math.floorDiv(i, j);
    }

    public static int nextInt(Random random, int i, int j) {
        if (i >= j) {
            return i;
        }
        return random.nextInt(j - i + 1) + i;
    }

    public static float nextFloat(Random random, float f, float g) {
        if (f >= g) {
            return f;
        }
        return random.nextFloat() * (g - f) + f;
    }

    public static double nextDouble(Random random, double d, double e) {
        if (d >= e) {
            return d;
        }
        return random.nextDouble() * (e - d) + d;
    }

    public static double average(long[] ls) {
        long l = 0L;
        for (long m : ls) {
            l += m;
        }
        return (double)l / (double)ls.length;
    }

    @Environment(value=EnvType.CLIENT)
    public static boolean equal(float f, float g) {
        return Math.abs(g - f) < 1.0E-5f;
    }

    public static boolean equal(double d, double e) {
        return Math.abs(e - d) < (double)1.0E-5f;
    }

    public static int positiveModulo(int i, int j) {
        return Math.floorMod(i, j);
    }

    @Environment(value=EnvType.CLIENT)
    public static float positiveModulo(float f, float g) {
        return (f % g + g) % g;
    }

    @Environment(value=EnvType.CLIENT)
    public static double positiveModulo(double d, double e) {
        return (d % e + e) % e;
    }

    @Environment(value=EnvType.CLIENT)
    public static int wrapDegrees(int i) {
        int j = i % 360;
        if (j >= 180) {
            j -= 360;
        }
        if (j < -180) {
            j += 360;
        }
        return j;
    }

    public static float wrapDegrees(float f) {
        float g = f % 360.0f;
        if (g >= 180.0f) {
            g -= 360.0f;
        }
        if (g < -180.0f) {
            g += 360.0f;
        }
        return g;
    }

    public static double wrapDegrees(double d) {
        double e = d % 360.0;
        if (e >= 180.0) {
            e -= 360.0;
        }
        if (e < -180.0) {
            e += 360.0;
        }
        return e;
    }

    public static float degreesDifference(float f, float g) {
        return Mth.wrapDegrees(g - f);
    }

    public static float degreesDifferenceAbs(float f, float g) {
        return Mth.abs(Mth.degreesDifference(f, g));
    }

    public static float rotateIfNecessary(float f, float g, float h) {
        float i = Mth.degreesDifference(f, g);
        float j = Mth.clamp(i, -h, h);
        return g - j;
    }

    public static float approach(float f, float g, float h) {
        h = Mth.abs(h);
        if (f < g) {
            return Mth.clamp(f + h, f, g);
        }
        return Mth.clamp(f - h, g, f);
    }

    public static float approachDegrees(float f, float g, float h) {
        float i = Mth.degreesDifference(f, g);
        return Mth.approach(f, f + i, h);
    }

    @Environment(value=EnvType.CLIENT)
    public static int getInt(String string, int i) {
        return NumberUtils.toInt(string, i);
    }

    @Environment(value=EnvType.CLIENT)
    public static int getInt(String string, int i, int j) {
        return Math.max(j, Mth.getInt(string, i));
    }

    @Environment(value=EnvType.CLIENT)
    public static double getDouble(String string, double d) {
        try {
            return Double.parseDouble(string);
        } catch (Throwable throwable) {
            return d;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static double getDouble(String string, double d, double e) {
        return Math.max(e, Mth.getDouble(string, d));
    }

    public static int smallestEncompassingPowerOfTwo(int i) {
        int j = i - 1;
        j |= j >> 1;
        j |= j >> 2;
        j |= j >> 4;
        j |= j >> 8;
        j |= j >> 16;
        return j + 1;
    }

    private static boolean isPowerOfTwo(int i) {
        return i != 0 && (i & i - 1) == 0;
    }

    public static int ceillog2(int i) {
        i = Mth.isPowerOfTwo(i) ? i : Mth.smallestEncompassingPowerOfTwo(i);
        return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int)((long)i * 125613361L >> 27) & 0x1F];
    }

    public static int log2(int i) {
        return Mth.ceillog2(i) - (Mth.isPowerOfTwo(i) ? 0 : 1);
    }

    public static int roundUp(int i, int j) {
        int k;
        if (j == 0) {
            return 0;
        }
        if (i == 0) {
            return j;
        }
        if (i < 0) {
            j *= -1;
        }
        if ((k = i % j) == 0) {
            return i;
        }
        return i + j - k;
    }

    @Environment(value=EnvType.CLIENT)
    public static int color(float f, float g, float h) {
        return Mth.color(Mth.floor(f * 255.0f), Mth.floor(g * 255.0f), Mth.floor(h * 255.0f));
    }

    @Environment(value=EnvType.CLIENT)
    public static int color(int i, int j, int k) {
        int l = i;
        l = (l << 8) + j;
        l = (l << 8) + k;
        return l;
    }

    @Environment(value=EnvType.CLIENT)
    public static int colorMultiply(int i, int j) {
        int k = (i & 0xFF0000) >> 16;
        int l = (j & 0xFF0000) >> 16;
        int m = (i & 0xFF00) >> 8;
        int n = (j & 0xFF00) >> 8;
        int o = (i & 0xFF) >> 0;
        int p = (j & 0xFF) >> 0;
        int q = (int)((float)k * (float)l / 255.0f);
        int r = (int)((float)m * (float)n / 255.0f);
        int s = (int)((float)o * (float)p / 255.0f);
        return i & 0xFF000000 | q << 16 | r << 8 | s;
    }

    @Environment(value=EnvType.CLIENT)
    public static float frac(float f) {
        return f - (float)Mth.floor(f);
    }

    public static double frac(double d) {
        return d - (double)Mth.lfloor(d);
    }

    public static long getSeed(Vec3i vec3i) {
        return Mth.getSeed(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public static long getSeed(int i, int j, int k) {
        long l = (long)(i * 3129871) ^ (long)k * 116129781L ^ (long)j;
        l = l * l * 42317861L + l * 11L;
        return l >> 16;
    }

    public static UUID createInsecureUUID(Random random) {
        long l = random.nextLong() & 0xFFFFFFFFFFFF0FFFL | 0x4000L;
        long m = random.nextLong() & 0x3FFFFFFFFFFFFFFFL | Long.MIN_VALUE;
        return new UUID(l, m);
    }

    public static UUID createInsecureUUID() {
        return Mth.createInsecureUUID(RANDOM);
    }

    public static double pct(double d, double e, double f) {
        return (d - e) / (f - e);
    }

    public static double atan2(double d, double e) {
        double g;
        boolean bl3;
        boolean bl2;
        boolean bl;
        double f = e * e + d * d;
        if (Double.isNaN(f)) {
            return Double.NaN;
        }
        boolean bl4 = bl = d < 0.0;
        if (bl) {
            d = -d;
        }
        boolean bl5 = bl2 = e < 0.0;
        if (bl2) {
            e = -e;
        }
        boolean bl6 = bl3 = d > e;
        if (bl3) {
            g = e;
            e = d;
            d = g;
        }
        g = Mth.fastInvSqrt(f);
        e *= g;
        double h = FRAC_BIAS + (d *= g);
        int i = (int)Double.doubleToRawLongBits(h);
        double j = ASIN_TAB[i];
        double k = COS_TAB[i];
        double l = h - FRAC_BIAS;
        double m = d * k - e * l;
        double n = (6.0 + m * m) * m * 0.16666666666666666;
        double o = j + n;
        if (bl3) {
            o = 1.5707963267948966 - o;
        }
        if (bl2) {
            o = Math.PI - o;
        }
        if (bl) {
            o = -o;
        }
        return o;
    }

    public static double fastInvSqrt(double d) {
        double e = 0.5 * d;
        long l = Double.doubleToRawLongBits(d);
        l = 6910469410427058090L - (l >> 1);
        d = Double.longBitsToDouble(l);
        d *= 1.5 - e * d * d;
        return d;
    }

    @Environment(value=EnvType.CLIENT)
    public static int hsvToRgb(float f, float g, float h) {
        float p;
        float o;
        float n;
        int i = (int)(f * 6.0f) % 6;
        float j = f * 6.0f - (float)i;
        float k = h * (1.0f - g);
        float l = h * (1.0f - j * g);
        float m = h * (1.0f - (1.0f - j) * g);
        switch (i) {
            case 0: {
                n = h;
                o = m;
                p = k;
                break;
            }
            case 1: {
                n = l;
                o = h;
                p = k;
                break;
            }
            case 2: {
                n = k;
                o = h;
                p = m;
                break;
            }
            case 3: {
                n = k;
                o = l;
                p = h;
                break;
            }
            case 4: {
                n = m;
                o = k;
                p = h;
                break;
            }
            case 5: {
                n = h;
                o = k;
                p = l;
                break;
            }
            default: {
                throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + f + ", " + g + ", " + h);
            }
        }
        int q = Mth.clamp((int)(n * 255.0f), 0, 255);
        int r = Mth.clamp((int)(o * 255.0f), 0, 255);
        int s = Mth.clamp((int)(p * 255.0f), 0, 255);
        return q << 16 | r << 8 | s;
    }

    public static int murmurHash3Mixer(int i) {
        i ^= i >>> 16;
        i *= -2048144789;
        i ^= i >>> 13;
        i *= -1028477387;
        i ^= i >>> 16;
        return i;
    }

    public static int binarySearch(int i, int j, IntPredicate intPredicate) {
        int k = j - i;
        while (k > 0) {
            int l = k / 2;
            int m = i + l;
            if (intPredicate.test(m)) {
                k = l;
                continue;
            }
            i = m + 1;
            k -= l + 1;
        }
        return i;
    }

    public static float lerp(float f, float g, float h) {
        return g + f * (h - g);
    }

    public static double lerp(double d, double e, double f) {
        return e + d * (f - e);
    }

    public static double lerp2(double d, double e, double f, double g, double h, double i) {
        return Mth.lerp(e, Mth.lerp(d, f, g), Mth.lerp(d, h, i));
    }

    public static double lerp3(double d, double e, double f, double g, double h, double i, double j, double k, double l, double m, double n) {
        return Mth.lerp(f, Mth.lerp2(d, e, g, h, i, j), Mth.lerp2(d, e, k, l, m, n));
    }

    public static double smoothstep(double d) {
        return d * d * d * (d * (d * 6.0 - 15.0) + 10.0);
    }

    public static int sign(double d) {
        if (d == 0.0) {
            return 0;
        }
        return d > 0.0 ? 1 : -1;
    }

    @Environment(value=EnvType.CLIENT)
    public static float rotLerp(float f, float g, float h) {
        return g + f * Mth.wrapDegrees(h - g);
    }

    @Environment(value=EnvType.CLIENT)
    public static float diffuseLight(float f, float g, float h) {
        return Math.min(f * f * 0.6f + g * g * ((3.0f + g) / 4.0f) + h * h * 0.8f, 1.0f);
    }

    static {
        for (int i = 0; i < 257; ++i) {
            double d = (double)i / 256.0;
            double e = Math.asin(d);
            Mth.COS_TAB[i] = Math.cos(e);
            Mth.ASIN_TAB[i] = e;
        }
    }
}

