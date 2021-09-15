/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.EndCityPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class EndCityFeature
extends StructureFeature<NoneFeatureConfiguration> {
    private static final int RANDOM_SALT = 10387313;

    public EndCityFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    protected boolean linearSeparation() {
        return false;
    }

    @Override
    protected boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long l, WorldgenRandom worldgenRandom, ChunkPos chunkPos, ChunkPos chunkPos2, NoneFeatureConfiguration noneFeatureConfiguration, LevelHeightAccessor levelHeightAccessor) {
        return EndCityFeature.getYPositionForFeature(chunkPos, chunkGenerator, levelHeightAccessor) >= 60;
    }

    @Override
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return EndCityStart::new;
    }

    static int getYPositionForFeature(ChunkPos chunkPos, ChunkGenerator chunkGenerator, LevelHeightAccessor levelHeightAccessor) {
        Random random = new Random(chunkPos.x + chunkPos.z * 10387313);
        Rotation rotation = Rotation.getRandom(random);
        int i = 5;
        int j = 5;
        if (rotation == Rotation.CLOCKWISE_90) {
            i = -5;
        } else if (rotation == Rotation.CLOCKWISE_180) {
            i = -5;
            j = -5;
        } else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
            j = -5;
        }
        int k = chunkPos.getBlockX(7);
        int l = chunkPos.getBlockZ(7);
        int m = chunkGenerator.getFirstOccupiedHeight(k, l, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
        int n = chunkGenerator.getFirstOccupiedHeight(k, l + j, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
        int o = chunkGenerator.getFirstOccupiedHeight(k + i, l, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
        int p = chunkGenerator.getFirstOccupiedHeight(k + i, l + j, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
        return Math.min(Math.min(m, n), Math.min(o, p));
    }

    public static class EndCityStart
    extends StructureStart<NoneFeatureConfiguration> {
        public EndCityStart(StructureFeature<NoneFeatureConfiguration> structureFeature, ChunkPos chunkPos, int i, long l) {
            super(structureFeature, chunkPos, i, l);
        }

        @Override
        public void generatePieces(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, StructureManager structureManager, ChunkPos chunkPos, NoneFeatureConfiguration noneFeatureConfiguration, LevelHeightAccessor levelHeightAccessor, Predicate<Biome> predicate) {
            Rotation rotation = Rotation.getRandom(this.random);
            int i = EndCityFeature.getYPositionForFeature(chunkPos, chunkGenerator, levelHeightAccessor);
            if (i < 60) {
                return;
            }
            BlockPos blockPos = chunkPos.getMiddleBlockPosition(i);
            if (!predicate.test(chunkGenerator.getNoiseBiome(QuartPos.fromBlock(blockPos.getX()), QuartPos.fromBlock(blockPos.getY()), QuartPos.fromBlock(blockPos.getZ())))) {
                return;
            }
            ArrayList<StructurePiece> list = Lists.newArrayList();
            EndCityPieces.startHouseTower(structureManager, blockPos, rotation, list, this.random);
            list.forEach(this::addPiece);
        }
    }
}

