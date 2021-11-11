/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

public interface BiomeResolver {
    public Biome getNoiseBiome(int var1, int var2, int var3, Climate.Sampler var4);
}

