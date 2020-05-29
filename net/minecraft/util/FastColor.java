/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class FastColor {

    public static class ARGB32 {
        @Environment(value=EnvType.CLIENT)
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

        @Environment(value=EnvType.CLIENT)
        public static int color(int i, int j, int k, int l) {
            return i << 24 | j << 16 | k << 8 | l;
        }

        @Environment(value=EnvType.CLIENT)
        public static int multiply(int i, int j) {
            return ARGB32.color(ARGB32.alpha(i) * ARGB32.alpha(j) / 255, ARGB32.red(i) * ARGB32.red(j) / 255, ARGB32.green(i) * ARGB32.green(j) / 255, ARGB32.blue(i) * ARGB32.blue(j) / 255);
        }
    }
}

