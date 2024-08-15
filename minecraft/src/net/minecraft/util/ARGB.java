package net.minecraft.util;

import net.minecraft.world.phys.Vec3;

public class ARGB {
	public static int alpha(int i) {
		return i >>> 24;
	}

	public static int red(int i) {
		return i >> 16 & 0xFF;
	}

	public static int green(int i) {
		return i >> 8 & 0xFF;
	}

	public static int blue(int i) {
		return i & 0xFF;
	}

	public static int color(int i, int j, int k, int l) {
		return i << 24 | j << 16 | k << 8 | l;
	}

	public static int color(int i, int j, int k) {
		return color(255, i, j, k);
	}

	public static int color(Vec3 vec3) {
		return color(as8BitChannel((float)vec3.x()), as8BitChannel((float)vec3.y()), as8BitChannel((float)vec3.z()));
	}

	public static int multiply(int i, int j) {
		if (i == -1) {
			return j;
		} else {
			return j == -1 ? i : color(alpha(i) * alpha(j) / 255, red(i) * red(j) / 255, green(i) * green(j) / 255, blue(i) * blue(j) / 255);
		}
	}

	public static int scaleRGB(int i, float f) {
		return color(alpha(i), (int)((float)red(i) * f), (int)((float)green(i) * f), (int)((float)blue(i) * f));
	}

	public static int scaleRGB(int i, int j) {
		return color(alpha(i), red(i) * j / 255, green(i) * j / 255, blue(i) * j / 255);
	}

	public static int greyscale(int i) {
		int j = (int)((float)red(i) * 0.3F + (float)green(i) * 0.59F + (float)blue(i) * 0.11F);
		return color(j, j, j);
	}

	public static int lerp(float f, int i, int j) {
		int k = Mth.lerpInt(f, alpha(i), alpha(j));
		int l = Mth.lerpInt(f, red(i), red(j));
		int m = Mth.lerpInt(f, green(i), green(j));
		int n = Mth.lerpInt(f, blue(i), blue(j));
		return color(k, l, m, n);
	}

	public static int opaque(int i) {
		return i | 0xFF000000;
	}

	public static int transparent(int i) {
		return i & 16777215;
	}

	public static int color(int i, int j) {
		return i << 24 | j & 16777215;
	}

	public static int white(float f) {
		return as8BitChannel(f) << 24 | 16777215;
	}

	public static int colorFromFloat(float f, float g, float h, float i) {
		return color(as8BitChannel(f), as8BitChannel(g), as8BitChannel(h), as8BitChannel(i));
	}

	public static int average(int i, int j) {
		return color((alpha(i) + alpha(j)) / 2, (red(i) + red(j)) / 2, (green(i) + green(j)) / 2, (blue(i) + blue(j)) / 2);
	}

	public static int as8BitChannel(float f) {
		return Mth.floor(f * 255.0F);
	}

	public static float from8BitChannel(int i) {
		return (float)i / 255.0F;
	}

	public static int toABGR(int i) {
		return i & -16711936 | (i & 0xFF0000) >> 16 | (i & 0xFF) << 16;
	}

	public static int fromABGR(int i) {
		return toABGR(i);
	}
}
