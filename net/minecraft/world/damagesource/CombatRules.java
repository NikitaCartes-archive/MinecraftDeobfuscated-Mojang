/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.damagesource;

import net.minecraft.util.Mth;

public class CombatRules {
    public static final float MAX_ARMOR = 20.0f;
    public static final float ARMOR_PROTECTION_DIVIDER = 25.0f;
    public static final float BASE_ARMOR_TOUGHNESS = 2.0f;
    public static final float MIN_ARMOR_RATIO = 0.2f;
    private static final int NUM_ARMOR_ITEMS = 4;

    public static float getDamageAfterAbsorb(float f, float g, float h) {
        float i = 2.0f + h / 4.0f;
        float j = Mth.clamp(g - f / i, g * 0.2f, 20.0f);
        return f * (1.0f - j / 25.0f);
    }

    public static float getDamageAfterMagicAbsorb(float f, float g) {
        float h = Mth.clamp(g, 0.0f, 20.0f);
        return f * (1.0f - h / 25.0f);
    }
}

