package net.minecraft.util;

import java.util.Locale;
import java.util.UUID;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.math.NumberUtils;

public class Mth {
	private static final long UUID_VERSION = 61440L;
	private static final long UUID_VERSION_TYPE_4 = 16384L;
	private static final long UUID_VARIANT = -4611686018427387904L;
	private static final long UUID_VARIANT_2 = Long.MIN_VALUE;
	public static final float PI = (float) Math.PI;
	public static final float HALF_PI = (float) (Math.PI / 2);
	public static final float TWO_PI = (float) (Math.PI * 2);
	public static final float DEG_TO_RAD = (float) (Math.PI / 180.0);
	public static final float RAD_TO_DEG = 180.0F / (float)Math.PI;
	public static final float EPSILON = 1.0E-5F;
	public static final float SQRT_OF_TWO = sqrt(2.0F);
	private static final float SIN_SCALE = 10430.378F;
	private static final float[] SIN = Util.make(new float[65536], fs -> {
		for (int ix = 0; ix < fs.length; ix++) {
			fs[ix] = (float)Math.sin((double)ix * Math.PI * 2.0 / 65536.0);
		}
	});
	private static final RandomSource RANDOM = RandomSource.createThreadSafe();
	private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[]{
		0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9
	};
	private static final double ONE_SIXTH = 0.16666666666666666;
	private static final int FRAC_EXP = 8;
	private static final int LUT_SIZE = 257;
	private static final double FRAC_BIAS = Double.longBitsToDouble(4805340802404319232L);
	private static final double[] ASIN_TAB = new double[257];
	private static final double[] COS_TAB = new double[257];

	public static float sin(float f) {
		return SIN[(int)(f * 10430.378F) & 65535];
	}

	public static float cos(float f) {
		return SIN[(int)(f * 10430.378F + 16384.0F) & 65535];
	}

	public static float sqrt(float f) {
		return (float)Math.sqrt((double)f);
	}

	public static int floor(float f) {
		int i = (int)f;
		return f < (float)i ? i - 1 : i;
	}

	public static int floor(double d) {
		int i = (int)d;
		return d < (double)i ? i - 1 : i;
	}

	public static long lfloor(double d) {
		long l = (long)d;
		return d < (double)l ? l - 1L : l;
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
		return Math.min(Math.max(i, j), k);
	}

	public static float clamp(float f, float g, float h) {
		return f < g ? g : Math.min(f, h);
	}

	public static double clamp(double d, double e, double f) {
		return d < e ? e : Math.min(d, f);
	}

	public static double clampedLerp(double d, double e, double f) {
		if (f < 0.0) {
			return d;
		} else {
			return f > 1.0 ? e : lerp(f, d, e);
		}
	}

	public static float clampedLerp(float f, float g, float h) {
		if (h < 0.0F) {
			return f;
		} else {
			return h > 1.0F ? g : lerp(h, f, g);
		}
	}

	public static double absMax(double d, double e) {
		if (d < 0.0) {
			d = -d;
		}

		if (e < 0.0) {
			e = -e;
		}

		return Math.max(d, e);
	}

	public static int floorDiv(int i, int j) {
		return Math.floorDiv(i, j);
	}

	public static int nextInt(RandomSource randomSource, int i, int j) {
		return i >= j ? i : randomSource.nextInt(j - i + 1) + i;
	}

	public static float nextFloat(RandomSource randomSource, float f, float g) {
		return f >= g ? f : randomSource.nextFloat() * (g - f) + f;
	}

	public static double nextDouble(RandomSource randomSource, double d, double e) {
		return d >= e ? d : randomSource.nextDouble() * (e - d) + d;
	}

	public static boolean equal(float f, float g) {
		return Math.abs(g - f) < 1.0E-5F;
	}

	public static boolean equal(double d, double e) {
		return Math.abs(e - d) < 1.0E-5F;
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

	public static boolean isMultipleOf(int i, int j) {
		return i % j == 0;
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
		float g = f % 360.0F;
		if (g >= 180.0F) {
			g -= 360.0F;
		}

		if (g < -180.0F) {
			g += 360.0F;
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
		return wrapDegrees(g - f);
	}

	public static float degreesDifferenceAbs(float f, float g) {
		return abs(degreesDifference(f, g));
	}

	public static float rotateIfNecessary(float f, float g, float h) {
		float i = degreesDifference(f, g);
		float j = clamp(i, -h, h);
		return g - j;
	}

	public static float approach(float f, float g, float h) {
		h = abs(h);
		return f < g ? clamp(f + h, f, g) : clamp(f - h, g, f);
	}

	public static float approachDegrees(float f, float g, float h) {
		float i = degreesDifference(f, g);
		return approach(f, f + i, h);
	}

	public static int getInt(String string, int i) {
		return NumberUtils.toInt(string, i);
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
		i = isPowerOfTwo(i) ? i : smallestEncompassingPowerOfTwo(i);
		return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int)((long)i * 125613361L >> 27) & 31];
	}

	public static int log2(int i) {
		return ceillog2(i) - (isPowerOfTwo(i) ? 0 : 1);
	}

	public static int color(float f, float g, float h) {
		return FastColor.ARGB32.color(0, floor(f * 255.0F), floor(g * 255.0F), floor(h * 255.0F));
	}

	public static float frac(float f) {
		return f - (float)floor(f);
	}

	public static double frac(double d) {
		return d - (double)lfloor(d);
	}

	@Deprecated
	public static long getSeed(Vec3i vec3i) {
		return getSeed(vec3i.getX(), vec3i.getY(), vec3i.getZ());
	}

	@Deprecated
	public static long getSeed(int i, int j, int k) {
		long l = (long)(i * 3129871) ^ (long)k * 116129781L ^ (long)j;
		l = l * l * 42317861L + l * 11L;
		return l >> 16;
	}

	public static UUID createInsecureUUID(RandomSource randomSource) {
		long l = randomSource.nextLong() & -61441L | 16384L;
		long m = randomSource.nextLong() & 4611686018427387903L | Long.MIN_VALUE;
		return new UUID(l, m);
	}

	public static UUID createInsecureUUID() {
		return createInsecureUUID(RANDOM);
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
		} else {
			double g = (aABB.minY + aABB.maxY) * 0.5;
			double h = (aABB.maxY - aABB.minY) * 0.5;
			double i = vec3.y - g;
			if (Math.abs(i) > h && i * vec32.y >= 0.0) {
				return false;
			} else {
				double j = (aABB.minZ + aABB.maxZ) * 0.5;
				double k = (aABB.maxZ - aABB.minZ) * 0.5;
				double l = vec3.z - j;
				if (Math.abs(l) > k && l * vec32.z >= 0.0) {
					return false;
				} else {
					double m = Math.abs(vec32.x);
					double n = Math.abs(vec32.y);
					double o = Math.abs(vec32.z);
					double p = vec32.y * l - vec32.z * i;
					if (Math.abs(p) > h * o + k * n) {
						return false;
					} else {
						p = vec32.z * f - vec32.x * l;
						if (Math.abs(p) > e * o + k * m) {
							return false;
						} else {
							p = vec32.x * i - vec32.y * f;
							return Math.abs(p) < e * n + h * m;
						}
					}
				}
			}
		}
	}

	public static double atan2(double d, double e) {
		double f = e * e + d * d;
		if (Double.isNaN(f)) {
			return Double.NaN;
		} else {
			boolean bl = d < 0.0;
			if (bl) {
				d = -d;
			}

			boolean bl2 = e < 0.0;
			if (bl2) {
				e = -e;
			}

			boolean bl3 = d > e;
			if (bl3) {
				double g = e;
				e = d;
				d = g;
			}

			double g = fastInvSqrt(f);
			e *= g;
			d *= g;
			double h = FRAC_BIAS + d;
			int i = (int)Double.doubleToRawLongBits(h);
			double j = ASIN_TAB[i];
			double k = COS_TAB[i];
			double l = h - FRAC_BIAS;
			double m = d * k - e * l;
			double n = (6.0 + m * m) * m * 0.16666666666666666;
			double o = j + n;
			if (bl3) {
				o = (Math.PI / 2) - o;
			}

			if (bl2) {
				o = Math.PI - o;
			}

			if (bl) {
				o = -o;
			}

			return o;
		}
	}

	public static float invSqrt(float f) {
		return org.joml.Math.invsqrt(f);
	}

	public static double invSqrt(double d) {
		return org.joml.Math.invsqrt(d);
	}

	@Deprecated
	public static double fastInvSqrt(double d) {
		double e = 0.5 * d;
		long l = Double.doubleToRawLongBits(d);
		l = 6910469410427058090L - (l >> 1);
		d = Double.longBitsToDouble(l);
		return d * (1.5 - e * d * d);
	}

	public static float fastInvCubeRoot(float f) {
		int i = Float.floatToIntBits(f);
		i = 1419967116 - i / 3;
		float g = Float.intBitsToFloat(i);
		g = 0.6666667F * g + 1.0F / (3.0F * g * g * f);
		return 0.6666667F * g + 1.0F / (3.0F * g * g * f);
	}

	public static int hsvToRgb(float f, float g, float h) {
		int i = (int)(f * 6.0F) % 6;
		float j = f * 6.0F - (float)i;
		float k = h * (1.0F - g);
		float l = h * (1.0F - j * g);
		float m = h * (1.0F - (1.0F - j) * g);
		float n;
		float o;
		float p;
		switch (i) {
			case 0:
				n = h;
				o = m;
				p = k;
				break;
			case 1:
				n = l;
				o = h;
				p = k;
				break;
			case 2:
				n = k;
				o = h;
				p = m;
				break;
			case 3:
				n = k;
				o = l;
				p = h;
				break;
			case 4:
				n = m;
				o = k;
				p = h;
				break;
			case 5:
				n = h;
				o = k;
				p = l;
				break;
			default:
				throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + f + ", " + g + ", " + h);
		}

		return FastColor.ARGB32.color(0, clamp((int)(n * 255.0F), 0, 255), clamp((int)(o * 255.0F), 0, 255), clamp((int)(p * 255.0F), 0, 255));
	}

	public static int murmurHash3Mixer(int i) {
		i ^= i >>> 16;
		i *= -2048144789;
		i ^= i >>> 13;
		i *= -1028477387;
		return i ^ i >>> 16;
	}

	public static int binarySearch(int i, int j, IntPredicate intPredicate) {
		int k = j - i;

		while (k > 0) {
			int l = k / 2;
			int m = i + l;
			if (intPredicate.test(m)) {
				k = l;
			} else {
				i = m + 1;
				k -= l + 1;
			}
		}

		return i;
	}

	public static int lerp(float f, int i, int j) {
		return i + floor(f * (float)(j - i));
	}

	public static float lerp(float f, float g, float h) {
		return g + f * (h - g);
	}

	public static double lerp(double d, double e, double f) {
		return e + d * (f - e);
	}

	public static double lerp2(double d, double e, double f, double g, double h, double i) {
		return lerp(e, lerp(d, f, g), lerp(d, h, i));
	}

	public static double lerp3(double d, double e, double f, double g, double h, double i, double j, double k, double l, double m, double n) {
		return lerp(f, lerp2(d, e, g, h, i, j), lerp2(d, e, k, l, m, n));
	}

	public static float catmullrom(float f, float g, float h, float i, float j) {
		return 0.5F * (2.0F * h + (i - g) * f + (2.0F * g - 5.0F * h + 4.0F * i - j) * f * f + (3.0F * h - g - 3.0F * i + j) * f * f * f);
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
		} else {
			return d > 0.0 ? 1 : -1;
		}
	}

	public static float rotLerp(float f, float g, float h) {
		return g + f * wrapDegrees(h - g);
	}

	public static float triangleWave(float f, float g) {
		return (Math.abs(f % g - g * 0.5F) - g * 0.25F) / (g * 0.25F);
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

	public static long square(long l) {
		return l * l;
	}

	public static double clampedMap(double d, double e, double f, double g, double h) {
		return clampedLerp(g, h, inverseLerp(d, e, f));
	}

	public static float clampedMap(float f, float g, float h, float i, float j) {
		return clampedLerp(i, j, inverseLerp(f, g, h));
	}

	public static double map(double d, double e, double f, double g, double h) {
		return lerp(inverseLerp(d, e, f), g, h);
	}

	public static float map(float f, float g, float h, float i, float j) {
		return lerp(inverseLerp(f, g, h), i, j);
	}

	public static double wobble(double d) {
		return d + (2.0 * RandomSource.create((long)floor(d * 3000.0)).nextDouble() - 1.0) * 1.0E-7 / 2.0;
	}

	public static int roundToward(int i, int j) {
		return positiveCeilDiv(i, j) * j;
	}

	public static int positiveCeilDiv(int i, int j) {
		return -Math.floorDiv(-i, j);
	}

	public static int randomBetweenInclusive(RandomSource randomSource, int i, int j) {
		return randomSource.nextInt(j - i + 1) + i;
	}

	public static float randomBetween(RandomSource randomSource, float f, float g) {
		return randomSource.nextFloat() * (g - f) + f;
	}

	public static float normal(RandomSource randomSource, float f, float g) {
		return f + (float)randomSource.nextGaussian() * g;
	}

	public static double lengthSquared(double d, double e) {
		return d * d + e * e;
	}

	public static double length(double d, double e) {
		return Math.sqrt(lengthSquared(d, e));
	}

	public static double lengthSquared(double d, double e, double f) {
		return d * d + e * e + f * f;
	}

	public static double length(double d, double e, double f) {
		return Math.sqrt(lengthSquared(d, e, f));
	}

	public static int quantize(double d, int i) {
		return floor(d / (double)i) * i;
	}

	public static IntStream outFromOrigin(int i, int j, int k) {
		return outFromOrigin(i, j, k, 1);
	}

	public static IntStream outFromOrigin(int i, int j, int k, int l) {
		if (j > k) {
			throw new IllegalArgumentException(String.format(Locale.ROOT, "upperbound %d expected to be > lowerBound %d", k, j));
		} else if (l < 1) {
			throw new IllegalArgumentException(String.format(Locale.ROOT, "steps expected to be >= 1, was %d", l));
		} else {
			return i >= j && i <= k ? IntStream.iterate(i, lx -> {
				int m = Math.abs(i - lx);
				return i - m >= j || i + m <= k;
			}, m -> {
				boolean bl = m <= i;
				int n = Math.abs(i - m);
				boolean bl2 = i + n + l <= k;
				if (!bl || !bl2) {
					int o = i - n - (bl ? l : 0);
					if (o >= j) {
						return o;
					}
				}

				return i + n + l;
			}) : IntStream.empty();
		}
	}

	static {
		for (int i = 0; i < 257; i++) {
			double d = (double)i / 256.0;
			double e = Math.asin(d);
			COS_TAB[i] = Math.cos(e);
			ASIN_TAB[i] = e;
		}
	}
}
