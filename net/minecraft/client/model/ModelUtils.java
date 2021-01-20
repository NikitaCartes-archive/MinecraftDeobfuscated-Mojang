/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class ModelUtils {
    public static float rotlerpRad(float f, float g, float h) {
        float i;
        for (i = g - f; i < (float)(-Math.PI); i += (float)Math.PI * 2) {
        }
        while (i >= (float)Math.PI) {
            i -= (float)Math.PI * 2;
        }
        return f + h * i;
    }
}

