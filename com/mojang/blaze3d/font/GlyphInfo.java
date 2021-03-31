/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.font;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public interface GlyphInfo {
    public float getAdvance();

    default public float getAdvance(boolean bl) {
        return this.getAdvance() + (bl ? this.getBoldOffset() : 0.0f);
    }

    default public float getBearingX() {
        return 0.0f;
    }

    default public float getBearingY() {
        return 0.0f;
    }

    default public float getBoldOffset() {
        return 1.0f;
    }

    default public float getShadowOffset() {
        return 1.0f;
    }
}

