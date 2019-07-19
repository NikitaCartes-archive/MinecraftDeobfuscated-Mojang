/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class GrassColor {
    private static int[] pixels = new int[65536];

    public static void init(int[] is) {
        pixels = is;
    }

    public static int get(double d, double e) {
        int j = (int)((1.0 - (e *= d)) * 255.0);
        int i = (int)((1.0 - d) * 255.0);
        int k = j << 8 | i;
        if (k > pixels.length) {
            return -65281;
        }
        return pixels[k];
    }
}

