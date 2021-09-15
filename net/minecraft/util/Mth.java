/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import java.util.Random;
import java.util.UUID;
import java.util.function.IntPredicate;
import net.minecraft.Util;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.math.NumberUtils;

public class Mth {
    private static final int BIG_ENOUGH_INT = 1024;
    private static final float BIG_ENOUGH_FLOAT = 1024.0f;
    private static final long UUID_VERSION = 61440L;
    private static final long UUID_VERSION_TYPE_4 = 16384L;
    private static final long UUID_VARIANT = -4611686018427387904L;
    private static final long UUID_VARIANT_2 = Long.MIN_VALUE;
    public static final float PI = (float)Math.PI;
    public static final float HALF_PI = 1.5707964f;
    public static final float TWO_PI = (float)Math.PI * 2;
    public static final float DEG_TO_RAD = (float)Math.PI / 180;
    public static final float RAD_TO_DEG = 57.295776f;
    public static final float EPSILON = 1.0E-5f;
    public static final float SQRT_OF_TWO = Mth.sqrt(2.0f);
    private static final float SIN_SCALE = 10430.378f;
    private static final float[] SIN = Util.make(new float[65536], fs -> {
        for (int i = 0; i < ((float[])fs).length; ++i) {
            fs[i] = (float)Math.sin((double)i * Math.PI * 2.0 / 65536.0);
        }
    });
    private static final Random RANDOM = new Random();
    private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};
    private static final double ONE_SIXTH = 0.16666666666666666;
    private static final int FRAC_EXP = 8;
    private static final int LUT_SIZE = 257;
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

    public static int floor(float f) {
        int i = (int)f;
        return f < (float)i ? i - 1 : i;
    }

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

    public static byte clamp(byte b, byte c, byte d) {
        if (b < c) {
            return c;
        }
        if (b > d) {
            return d;
        }
        return b;
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

    public static long clamp(long l, long m, long n) {
        if (l < m) {
            return m;
        }
        if (l > n) {
            return n;
        }
        return l;
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

    public static float clampedLerp(float f, float g, float h) {
        if (h < 0.0f) {
            return f;
        }
        if (h > 1.0f) {
            return g;
        }
        return Mth.lerp(h, f, g);
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

    public static boolean equal(float f, float g) {
        return Math.abs(g - f) < 1.0E-5f;
    }

    public static boolean equal(double d, double e) {
        return Math.abs(e - d) < (double)1.0E-5f;
    }

    public static int positiveModulo(int i, int j) {
        return Math.floorMod(i, j);
    }

    public static float positiveModulo(float f, float g) {
        return (f % g + g) % g;
    }

    public static double positiveModulo(double d, double e) {
        return (d % e + e) % e;
    }

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

    public static int getInt(String string, int i) {
        return NumberUtils.toInt(string, i);
    }

    public static int getInt(String string, int i, int j) {
        return Math.max(j, Mth.getInt(string, i));
    }

    public static double getDouble(String string, double d) {
        try {
            return Double.parseDouble(string);
        } catch (Throwable throwable) {
            return d;
        }
    }

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

    public static boolean isPowerOfTwo(int i) {
        return i != 0 && (i & i - 1) == 0;
    }

    public static int ceillog2(int i) {
        i = Mth.isPowerOfTwo(i) ? i : Mth.smallestEncompassingPowerOfTwo(i);
        return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int)((long)i * 125613361L >> 27) & 0x1F];
    }

    public static int log2(int i) {
        return Mth.ceillog2(i) - (Mth.isPowerOfTwo(i) ? 0 : 1);
    }

    public static int color(float f, float g, float h) {
        return Mth.color(Mth.floor(f * 255.0f), Mth.floor(g * 255.0f), Mth.floor(h * 255.0f));
    }

    public static int color(int i, int j, int k) {
        int l = i;
        l = (l << 8) + j;
        l = (l << 8) + k;
        return l;
    }

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

    public static int colorMultiply(int i, float f, float g, float h) {
        int j = (i & 0xFF0000) >> 16;
        int k = (i & 0xFF00) >> 8;
        int l = (i & 0xFF) >> 0;
        int m = (int)((float)j * f);
        int n = (int)((float)k * g);
        int o = (int)((float)l * h);
        return i & 0xFF000000 | m << 16 | n << 8 | o;
    }

    public static float frac(float f) {
        return f - (float)Mth.floor(f);
    }

    public static double frac(double d) {
        return d - (double)Mth.lfloor(d);
    }

    public static Vec3 catmullRomSplinePos(Vec3 vec3, Vec3 vec32, Vec3 vec33, Vec3 vec34, double d) {
        double e = ((-d + 2.0) * d - 1.0) * d * 0.5;
        double f = ((3.0 * d - 5.0) * d * d + 2.0) * 0.5;
        double g = ((-3.0 * d + 4.0) * d + 1.0) * d * 0.5;
        double h = (d - 1.0) * d * d * 0.5;
        return new Vec3(vec3.x * e + vec32.x * f + vec33.x * g + vec34.x * h, vec3.y * e + vec32.y * f + vec33.y * g + vec34.y * h, vec3.z * e + vec32.z * f + vec33.z * g + vec34.z * h);
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

    public static double inverseLerp(double d, double e, double f) {
        return (d - e) / (f - e);
    }

    public static float inverseLerp(float f, float g, float h) {
        return (f - g) / (h - g);
    }

    public static boolean rayIntersectsAABB(Vec3 vec3, Vec3 vec32, AABB aABB) {
        double d = (aABB.minX + aABB.maxX) * 0.5;
        double e = (aABB.maxX - aABB.minX) * 0.5;
        double f = vec3.x - d;
        if (Math.abs(f) > e && f * vec32.x >= 0.0) {
            return false;
        }
        double g = (aABB.minY + aABB.maxY) * 0.5;
        double h = (aABB.maxY - aABB.minY) * 0.5;
        double i = vec3.y - g;
        if (Math.abs(i) > h && i * vec32.y >= 0.0) {
            return false;
        }
        double j = (aABB.minZ + aABB.maxZ) * 0.5;
        double k = (aABB.maxZ - aABB.minZ) * 0.5;
        double l = vec3.z - j;
        if (Math.abs(l) > k && l * vec32.z >= 0.0) {
            return false;
        }
        double m = Math.abs(vec32.x);
        double n = Math.abs(vec32.y);
        double o = Math.abs(vec32.z);
        double p = vec32.y * l - vec32.z * i;
        if (Math.abs(p) > h * o + k * n) {
            return false;
        }
        p = vec32.z * f - vec32.x * l;
        if (Math.abs(p) > e * o + k * m) {
            return false;
        }
        p = vec32.x * i - vec32.y * f;
        return Math.abs(p) < e * n + h * m;
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

    public static float fastInvSqrt(float f) {
        float g = 0.5f * f;
        int i = Float.floatToIntBits(f);
        i = 1597463007 - (i >> 1);
        f = Float.intBitsToFloat(i);
        f *= 1.5f - g * f * f;
        return f;
    }

    public static double fastInvSqrt(double d) {
        double e = 0.5 * d;
        long l = Double.doubleToRawLongBits(d);
        l = 6910469410427058090L - (l >> 1);
        d = Double.longBitsToDouble(l);
        d *= 1.5 - e * d * d;
        return d;
    }

    public static float fastInvCubeRoot(float f) {
        int i = Float.floatToIntBits(f);
        i = 1419967116 - i / 3;
        float g = Float.intBitsToFloat(i);
        g = 0.6666667f * g + 1.0f / (3.0f * g * g * f);
        g = 0.6666667f * g + 1.0f / (3.0f * g * g * f);
        return g;
    }

    public static int hsvToRgb(float f, float g, float h) {
        float o;
        float n;
        int i = (int)(f * 6.0f) % 6;
        float j = f * 6.0f - (float)i;
        float k = h * (1.0f - g);
        float l = h * (1.0f - j * g);
        float m = h * (1.0f - (1.0f - j) * g);
        float p = switch (i) {
            case 0 -> {
                n = h;
                o = m;
                yield k;
            }
            case 1 -> {
                n = l;
                o = h;
                yield k;
            }
            case 2 -> {
                n = k;
                o = h;
                yield m;
            }
            case 3 -> {
                n = k;
                o = l;
                yield h;
            }
            case 4 -> {
                n = m;
                o = k;
                yield h;
            }
            case 5 -> {
                n = h;
                o = k;
                yield l;
            }
            default -> throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + f + ", " + g + ", " + h);
        };
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

    public static long murmurHash3Mixer(long l) {
        l ^= l >>> 33;
        l *= -49064778989728563L;
        l ^= l >>> 33;
        l *= -4265267296055464877L;
        l ^= l >>> 33;
        return l;
    }

    public static double[] cumulativeSum(double ... ds) {
        float f = 0.0f;
        for (double d : ds) {
            f = (float)((double)f + d);
        }
        int i = 0;
        while (i < ds.length) {
            int n = i++;
            ds[n] = ds[n] / (double)f;
        }
        for (i = 0; i < ds.length; ++i) {
            ds[i] = (i == 0 ? 0.0 : ds[i - 1]) + ds[i];
        }
        return ds;
    }

    public static int getRandomForDistributionIntegral(Random random, double[] ds) {
        double d = random.nextDouble();
        for (int i = 0; i < ds.length; ++i) {
            if (!(d < ds[i])) continue;
            return i;
        }
        return ds.length;
    }

    public static double[] binNormalDistribution(double d, double e, double f, int i, int j) {
        double[] ds = new double[j - i + 1];
        int k = 0;
        for (int l = i; l <= j; ++l) {
            ds[k] = Math.max(0.0, d * StrictMath.exp(-((double)l - f) * ((double)l - f) / (2.0 * e * e)));
            ++k;
        }
        return ds;
    }

    public static double[] binBiModalNormalDistribution(double d, double e, double f, double g, double h, double i, int j, int k) {
        double[] ds = new double[k - j + 1];
        int l = 0;
        for (int m = j; m <= k; ++m) {
            ds[l] = Math.max(0.0, d * StrictMath.exp(-((double)m - f) * ((double)m - f) / (2.0 * e * e)) + g * StrictMath.exp(-((double)m - i) * ((double)m - i) / (2.0 * h * h)));
            ++l;
        }
        return ds;
    }

    public static double[] binLogDistribution(double d, double e, int i, int j) {
        double[] ds = new double[j - i + 1];
        int k = 0;
        for (int l = i; l <= j; ++l) {
            ds[k] = Math.max(d * StrictMath.log(l) + e, 0.0);
            ++k;
        }
        return ds;
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

    public static double smoothstepDerivative(double d) {
        return 30.0 * d * d * (d - 1.0) * (d - 1.0);
    }

    public static int sign(double d) {
        if (d == 0.0) {
            return 0;
        }
        return d > 0.0 ? 1 : -1;
    }

    public static float rotLerp(float f, float g, float h) {
        return g + f * Mth.wrapDegrees(h - g);
    }

    public static float diffuseLight(float f, float g, float h) {
        return Math.min(f * f * 0.6f + g * g * ((3.0f + g) / 4.0f) + h * h * 0.8f, 1.0f);
    }

    @Deprecated
    public static float rotlerp(float f, float g, float h) {
        float i;
        for (i = g - f; i < -180.0f; i += 360.0f) {
        }
        while (i >= 180.0f) {
            i -= 360.0f;
        }
        return f + h * i;
    }

    @Deprecated
    public static float rotWrap(double d) {
        while (d >= 180.0) {
            d -= 360.0;
        }
        while (d < -180.0) {
            d += 360.0;
        }
        return (float)d;
    }

    public static float triangleWave(float f, float g) {
        return (Math.abs(f % g - g * 0.5f) - g * 0.25f) / (g * 0.25f);
    }

    public static float square(float f) {
        return f * f;
    }

    public static double square(double d) {
        return d * d;
    }

    public static int square(int i) {
        return i * i;
    }

    public static double clampedMap(double d, double e, double f, double g, double h) {
        return Mth.clampedLerp(g, h, Mth.inverseLerp(d, e, f));
    }

    public static float clampedMap(float f, float g, float h, float i, float j) {
        return Mth.clampedLerp(i, j, Mth.inverseLerp(f, g, h));
    }

    public static double map(double d, double e, double f, double g, double h) {
        return Mth.lerp(Mth.inverseLerp(d, e, f), g, h);
    }

    public static float map(float f, float g, float h, float i, float j) {
        return Mth.lerp(Mth.inverseLerp(f, g, h), i, j);
    }

    public static double wobble(double d) {
        return d + (2.0 * new Random(Mth.floor(d * 3000.0)).nextDouble() - 1.0) * 1.0E-7 / 2.0;
    }

    public static int roundToward(int i, int j) {
        return (i + j - 1) / j * j;
    }

    public static int randomBetweenInclusive(Random random, int i, int j) {
        return random.nextInt(j - i + 1) + i;
    }

    public static float randomBetween(Random random, float f, float g) {
        return random.nextFloat() * (g - f) + f;
    }

    public static float normal(Random random, float f, float g) {
        return f + (float)random.nextGaussian() * g;
    }

    public static double length(int i, double d, int j) {
        return Math.sqrt((double)(i * i) + d * d + (double)(j * j));
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

