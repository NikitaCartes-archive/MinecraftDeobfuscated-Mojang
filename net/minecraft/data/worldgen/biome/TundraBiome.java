/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen.biome;

import net.minecraft.world.level.biome.Biome;

public final class TundraBiome
extends Biome {
    public TundraBiome(Biome.BiomeBuilder biomeBuilder) {
        super(biomeBuilder);
    }

    @Override
    public float getCreatureProbability() {
        return 0.07f;
    }
}

