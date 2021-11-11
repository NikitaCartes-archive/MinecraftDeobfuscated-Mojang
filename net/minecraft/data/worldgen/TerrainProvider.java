/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen;

import net.minecraft.util.CubicSpline;
import net.minecraft.world.level.biome.TerrainShaper;

public class TerrainProvider {
    public static TerrainShaper overworld(boolean bl) {
        return TerrainShaper.overworld(bl);
    }

    public static TerrainShaper nether() {
        return new TerrainShaper(CubicSpline.constant(0.0f), CubicSpline.constant(0.0f), CubicSpline.constant(0.0f));
    }

    public static TerrainShaper end() {
        return new TerrainShaper(CubicSpline.constant(0.0f), CubicSpline.constant(1.0f), CubicSpline.constant(0.0f));
    }
}

