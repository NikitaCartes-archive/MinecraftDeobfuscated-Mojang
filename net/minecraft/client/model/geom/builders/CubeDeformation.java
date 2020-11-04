/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model.geom.builders;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class CubeDeformation {
    public static final CubeDeformation NONE = new CubeDeformation(0.0f);
    final float growX;
    final float growY;
    final float growZ;

    public CubeDeformation(float f, float g, float h) {
        this.growX = f;
        this.growY = g;
        this.growZ = h;
    }

    public CubeDeformation(float f) {
        this(f, f, f);
    }

    public CubeDeformation extend(float f) {
        return new CubeDeformation(this.growX + f, this.growY + f, this.growZ + f);
    }

    public CubeDeformation extend(float f, float g, float h) {
        return new CubeDeformation(this.growX + f, this.growY + g, this.growZ + h);
    }
}

