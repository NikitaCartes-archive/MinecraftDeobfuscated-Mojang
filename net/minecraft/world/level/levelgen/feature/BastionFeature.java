/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.JigsawFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;

public class BastionFeature
extends JigsawFeature {
    private static final int BASTION_SPAWN_HEIGHT = 33;

    public BastionFeature(Codec<JigsawConfiguration> codec) {
        super(codec, 33, false, false);
    }

    @Override
    protected boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long l, WorldgenRandom worldgenRandom, ChunkPos chunkPos, Biome biome, ChunkPos chunkPos2, JigsawConfiguration jigsawConfiguration, LevelHeightAccessor levelHeightAccessor) {
        return worldgenRandom.nextInt(5) >= 2;
    }
}

