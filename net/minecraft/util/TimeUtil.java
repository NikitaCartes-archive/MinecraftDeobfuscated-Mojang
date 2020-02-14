/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import net.minecraft.util.IntRange;

public class TimeUtil {
    public static IntRange rangeOfSeconds(int i, int j) {
        return new IntRange(i * 20, j * 20);
    }
}

