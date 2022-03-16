/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.font;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public interface SheetGlyphInfo {
    public int getPixelWidth();

    public int getPixelHeight();

    public void upload(int var1, int var2);

    public boolean isColored();

    public float getOversample();

    default public float getLeft() {
        return this.getBearingX();
    }

    default public float getRight() {
        return this.getLeft() + (float)this.getPixelWidth() / this.getOversample();
    }

    default public float getUp() {
        return this.getBearingY();
    }

    default public float getDown() {
        return this.getUp() + (float)this.getPixelHeight() / this.getOversample();
    }

    default public float getBearingX() {
        return 0.0f;
    }

    default public float getBearingY() {
        return 3.0f;
    }
}

