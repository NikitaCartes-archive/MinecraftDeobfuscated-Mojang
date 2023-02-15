package net.minecraft.util;

public class FastColor {
	public static class ABGR32 {
		public static int alpha(int i) {
			return i >>> 24;
		}

		public static int red(int i) {
			return i & 0xFF;
		}

		public static int green(int i) {
			return i >> 8 & 0xFF;
		}

		public static int blue(int i) {
			return i >> 16 & 0xFF;
		}

		public static int transparent(int i) {
			return i & 16777215;
		}

		public static int opaque(int i) {
			return i | 0xFF000000;
		}

		public static int color(int i, int j, int k, int l) {
			return i << 24 | j << 16 | k << 8 | l;
		}

		public static int color(int i, int j) {
			return i << 24 | j & 16777215;
		}
	}

	public static class ARGB32 {
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

		public static int multiply(int i, int j) {
			return color(alpha(i) * alpha(j) / 255, red(i) * red(j) / 255, green(i) * green(j) / 255, blue(i) * blue(j) / 255);
		}

		public static int lerp(float f, int i, int j) {
			int k = Mth.lerpInt(f, alpha(i), alpha(j));
			int l = Mth.lerpInt(f, red(i), red(j));
			int m = Mth.lerpInt(f, green(i), green(j));
			int n = Mth.lerpInt(f, blue(i), blue(j));
			return color(k, l, m, n);
		}
	}
}
