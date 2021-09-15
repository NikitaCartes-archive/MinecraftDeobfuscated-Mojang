/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.SwamplandHutPiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class SwamplandHutFeature
extends StructureFeature<NoneFeatureConfiguration> {
    private static final WeightedRandomList<MobSpawnSettings.SpawnerData> SWAMPHUT_ENEMIES = WeightedRandomList.create((WeightedEntry[])new MobSpawnSettings.SpawnerData[]{new MobSpawnSettings.SpawnerData(EntityType.WITCH, 1, 1, 1)});
    private static final WeightedRandomList<MobSpawnSettings.SpawnerData> SWAMPHUT_ANIMALS = WeightedRandomList.create((WeightedEntry[])new MobSpawnSettings.SpawnerData[]{new MobSpawnSettings.SpawnerData(EntityType.CAT, 1, 1, 1)});

    public SwamplandHutFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return FeatureStart::new;
    }

    @Override
    public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
        return SWAMPHUT_ENEMIES;
    }

    @Override
    public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialAnimals() {
        return SWAMPHUT_ANIMALS;
    }

    public static class FeatureStart
    extends StructureStart<NoneFeatureConfiguration> {
        public FeatureStart(StructureFeature<NoneFeatureConfiguration> structureFeature, ChunkPos chunkPos, int i, long l) {
            super(structureFeature, chunkPos, i, l);
        }

        @Override
        public void generatePieces(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, StructureManager structureManager, ChunkPos chunkPos, NoneFeatureConfiguration noneFeatureConfiguration, LevelHeightAccessor levelHeightAccessor, Predicate<Biome> predicate) {
            if (!StructureFeature.validBiomeOnTop(chunkGenerator, levelHeightAccessor, predicate, Heightmap.Types.WORLD_SURFACE_WG, chunkPos.getMiddleBlockX(), chunkPos.getMiddleBlockZ())) {
                return;
            }
            SwamplandHutPiece swamplandHutPiece = new SwamplandHutPiece(this.random, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
            this.addPiece(swamplandHutPiece);
        }
    }
}

