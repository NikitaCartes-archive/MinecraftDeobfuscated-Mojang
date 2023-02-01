/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
            return i & 0xFFFFFF;
        }

        public static int opaque(int i) {
            return i | 0xFF000000;
        }

        public static int color(int i, int j, int k, int l) {
            return i << 24 | j << 16 | k << 8 | l;
        }

        public static int color(int i, int j) {
            return i << 24 | j & 0xFFFFFF;
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
            return ARGB32.color(ARGB32.alpha(i) * ARGB32.alpha(j) / 255, ARGB32.red(i) * ARGB32.red(j) / 255, ARGB32.green(i) * ARGB32.green(j) / 255, ARGB32.blue(i) * ARGB32.blue(j) / 255);
        }
    }
}

