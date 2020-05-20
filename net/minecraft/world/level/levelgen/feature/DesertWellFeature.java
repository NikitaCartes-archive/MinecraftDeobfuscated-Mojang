/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class DesertWellFeature
extends Feature<NoneFeatureConfiguration> {
    private static final BlockStatePredicate IS_SAND = BlockStatePredicate.forBlock(Blocks.SAND);
    private final BlockState sandSlab = Blocks.SANDSTONE_SLAB.defaultBlockState();
    private final BlockState sandstone = Blocks.SANDSTONE.defaultBlockState();
    private final BlockState water = Blocks.WATER.defaultBlockState();

    public DesertWellFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        int i;
        int j;
        int i2;
        blockPos = blockPos.above();
        while (worldGenLevel.isEmptyBlock(blockPos) && blockPos.getY() > 2) {
            blockPos = blockPos.below();
        }
        if (!IS_SAND.test(worldGenLevel.getBlockState(blockPos))) {
            return false;
        }
        for (i2 = -2; i2 <= 2; ++i2) {
            for (j = -2; j <= 2; ++j) {
                if (!worldGenLevel.isEmptyBlock(blockPos.offset(i2, -1, j)) || !worldGenLevel.isEmptyBlock(blockPos.offset(i2, -2, j))) continue;
                return false;
            }
        }
        for (i2 = -1; i2 <= 0; ++i2) {
            for (j = -2; j <= 2; ++j) {
                for (int k = -2; k <= 2; ++k) {
                    worldGenLevel.setBlock(blockPos.offset(j, i2, k), this.sandstone, 2);
                }
            }
        }
        worldGenLevel.setBlock(blockPos, this.water, 2);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            worldGenLevel.setBlock(blockPos.relative(direction), this.water, 2);
        }
        for (i = -2; i <= 2; ++i) {
            for (int j2 = -2; j2 <= 2; ++j2) {
                if (i != -2 && i != 2 && j2 != -2 && j2 != 2) continue;
                worldGenLevel.setBlock(blockPos.offset(i, 1, j2), this.sandstone, 2);
            }
        }
        worldGenLevel.setBlock(blockPos.offset(2, 1, 0), this.sandSlab, 2);
        worldGenLevel.setBlock(blockPos.offset(-2, 1, 0), this.sandSlab, 2);
        worldGenLevel.setBlock(blockPos.offset(0, 1, 2), this.sandSlab, 2);
        worldGenLevel.setBlock(blockPos.offset(0, 1, -2), this.sandSlab, 2);
        for (i = -1; i <= 1; ++i) {
            for (int j3 = -1; j3 <= 1; ++j3) {
                if (i == 0 && j3 == 0) {
                    worldGenLevel.setBlock(blockPos.offset(i, 4, j3), this.sandstone, 2);
                    continue;
                }
                worldGenLevel.setBlock(blockPos.offset(i, 4, j3), this.sandSlab, 2);
            }
        }
        for (i = 1; i <= 3; ++i) {
            worldGenLevel.setBlock(blockPos.offset(-1, i, -1), this.sandstone, 2);
            worldGenLevel.setBlock(blockPos.offset(-1, i, 1), this.sandstone, 2);
            worldGenLevel.setBlock(blockPos.offset(1, i, -1), this.sandstone, 2);
            worldGenLevel.setBlock(blockPos.offset(1, i, 1), this.sandstone, 2);
        }
        return true;
    }
}

