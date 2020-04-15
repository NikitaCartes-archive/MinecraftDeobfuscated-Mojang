/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class WoodlandMansionFeature
extends StructureFeature<NoneFeatureConfiguration> {
    public WoodlandMansionFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
        super(function);
    }

    @Override
    protected int getSpacing(DimensionType dimensionType, ChunkGeneratorSettings chunkGeneratorSettings) {
        return chunkGeneratorSettings.getWoodlandMansionSpacing();
    }

    @Override
    protected int getSeparation(DimensionType dimensionType, ChunkGeneratorSettings chunkGeneratorSettings) {
        return chunkGeneratorSettings.getWoodlandMansionSeparation();
    }

    @Override
    protected int getRandomSalt(ChunkGeneratorSettings chunkGeneratorSettings) {
        return 10387319;
    }

    @Override
    protected boolean linearSeparation() {
        return false;
    }

    @Override
    protected boolean isFeatureChunk(BiomeManager biomeManager, ChunkGenerator<?> chunkGenerator, WorldgenRandom worldgenRandom, int i, int j, Biome biome, ChunkPos chunkPos) {
        Set<Biome> set = chunkGenerator.getBiomeSource().getBiomesWithin(i * 16 + 9, chunkGenerator.getSeaLevel(), j * 16 + 9, 32);
        for (Biome biome2 : set) {
            if (chunkGenerator.isBiomeValidStartForStructure(biome2, this)) continue;
            return false;
        }
        return true;
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return WoodlandMansionStart::new;
    }

    @Override
    public String getFeatureName() {
        return "Mansion";
    }

    @Override
    public int getLookupRange() {
        return 8;
    }

    public static class WoodlandMansionStart
    extends StructureStart {
        public WoodlandMansionStart(StructureFeature<?> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
            super(structureFeature, i, j, boundingBox, k, l);
        }

        @Override
        public void generatePieces(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int i, int j, Biome biome) {
            Rotation rotation = Rotation.getRandom(this.random);
            int k = 5;
            int l = 5;
            if (rotation == Rotation.CLOCKWISE_90) {
                k = -5;
            } else if (rotation == Rotation.CLOCKWISE_180) {
                k = -5;
                l = -5;
            } else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
                l = -5;
            }
            int m = (i << 4) + 7;
            int n = (j << 4) + 7;
            int o = chunkGenerator.getFirstOccupiedHeight(m, n, Heightmap.Types.WORLD_SURFACE_WG);
            int p = chunkGenerator.getFirstOccupiedHeight(m, n + l, Heightmap.Types.WORLD_SURFACE_WG);
            int q = chunkGenerator.getFirstOccupiedHeight(m + k, n, Heightmap.Types.WORLD_SURFACE_WG);
            int r = chunkGenerator.getFirstOccupiedHeight(m + k, n + l, Heightmap.Types.WORLD_SURFACE_WG);
            int s = Math.min(Math.min(o, p), Math.min(q, r));
            if (s < 60) {
                return;
            }
            BlockPos blockPos = new BlockPos(i * 16 + 8, s + 1, j * 16 + 8);
            LinkedList<WoodlandMansionPieces.WoodlandMansionPiece> list = Lists.newLinkedList();
            WoodlandMansionPieces.generateMansion(structureManager, blockPos, rotation, list, this.random);
            this.pieces.addAll(list);
            this.calculateBoundingBox();
        }

        @Override
        public void postProcess(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, ChunkGenerator<?> chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos) {
            super.postProcess(levelAccessor, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos);
            int i = this.boundingBox.y0;
            for (int j = boundingBox.x0; j <= boundingBox.x1; ++j) {
                for (int k = boundingBox.z0; k <= boundingBox.z1; ++k) {
                    BlockPos blockPos2;
                    BlockPos blockPos = new BlockPos(j, i, k);
                    if (levelAccessor.isEmptyBlock(blockPos) || !this.boundingBox.isInside(blockPos)) continue;
                    boolean bl = false;
                    for (StructurePiece structurePiece : this.pieces) {
                        if (!structurePiece.getBoundingBox().isInside(blockPos)) continue;
                        bl = true;
                        break;
                    }
                    if (!bl) continue;
                    for (int l = i - 1; l > 1 && (levelAccessor.isEmptyBlock(blockPos2 = new BlockPos(j, l, k)) || levelAccessor.getBlockState(blockPos2).getMaterial().isLiquid()); --l) {
                        levelAccessor.setBlock(blockPos2, Blocks.COBBLESTONE.defaultBlockState(), 2);
                    }
                }
            }
        }
    }
}

