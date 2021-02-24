/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.JigsawFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

public class PillagerOutpostFeature
extends JigsawFeature {
    private static final List<MobSpawnSettings.SpawnerData> OUTPOST_ENEMIES = ImmutableList.of(new MobSpawnSettings.SpawnerData(EntityType.PILLAGER, 1, 1, 1));

    public PillagerOutpostFeature(Codec<JigsawConfiguration> codec) {
        super(codec, 0, true, true);
    }

    @Override
    public List<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
        return OUTPOST_ENEMIES;
    }

    @Override
    protected boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long l, WorldgenRandom worldgenRandom, ChunkPos chunkPos, Biome biome, ChunkPos chunkPos2, JigsawConfiguration jigsawConfiguration, LevelHeightAccessor levelHeightAccessor) {
        int i = chunkPos.x >> 4;
        int j = chunkPos.z >> 4;
        worldgenRandom.setSeed((long)(i ^ j << 4) ^ l);
        worldgenRandom.nextInt();
        if (worldgenRandom.nextInt(5) != 0) {
            return false;
        }
        return !this.isNearVillage(chunkGenerator, l, worldgenRandom, chunkPos);
    }

    private boolean isNearVillage(ChunkGenerator chunkGenerator, long l, WorldgenRandom worldgenRandom, ChunkPos chunkPos) {
        StructureFeatureConfiguration structureFeatureConfiguration = chunkGenerator.getSettings().getConfig(StructureFeature.VILLAGE);
        if (structureFeatureConfiguration == null) {
            return false;
        }
        int i = chunkPos.x;
        int j = chunkPos.z;
        for (int k = i - 10; k <= i + 10; ++k) {
            for (int m = j - 10; m <= j + 10; ++m) {
                ChunkPos chunkPos2 = StructureFeature.VILLAGE.getPotentialFeatureChunk(structureFeatureConfiguration, l, worldgenRandom, k, m);
                if (k != chunkPos2.x || m != chunkPos2.z) continue;
                return true;
            }
        }
        return false;
    }
}

