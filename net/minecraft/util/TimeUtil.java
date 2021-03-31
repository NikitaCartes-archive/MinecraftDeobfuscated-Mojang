/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import net.minecraft.util.valueproviders.UniformInt;

public class TimeUtil {
    public static UniformInt rangeOfSeconds(int i, int j) {
        return UniformInt.of(i * 20, j * 20);
    }
}

