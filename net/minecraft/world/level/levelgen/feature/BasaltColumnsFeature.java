/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;
import org.jetbrains.annotations.Nullable;

public class BasaltColumnsFeature
extends Feature<ColumnFeatureConfiguration> {
    public BasaltColumnsFeature(Function<Dynamic<?>, ? extends ColumnFeatureConfiguration> function) {
        super(function);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, ColumnFeatureConfiguration columnFeatureConfiguration) {
        int i = chunkGenerator.getSeaLevel();
        BlockPos blockPos2 = BasaltColumnsFeature.findSurface(levelAccessor, i, blockPos.mutable().clamp(Direction.Axis.Y, 1, levelAccessor.getMaxBuildHeight() - 1), Integer.MAX_VALUE);
        if (blockPos2 == null) {
            return false;
        }
        int j = BasaltColumnsFeature.calculateHeight(random, columnFeatureConfiguration);
        boolean bl = random.nextFloat() < 0.9f;
        int k = Math.min(j, bl ? 5 : 8);
        int l = bl ? 50 : 15;
        boolean bl2 = false;
        for (BlockPos blockPos3 : BlockPos.randomBetweenClosed(random, l, blockPos2.getX() - k, blockPos2.getY(), blockPos2.getZ() - k, blockPos2.getX() + k, blockPos2.getY(), blockPos2.getZ() + k)) {
            int m = j - blockPos3.distManhattan(blockPos2);
            if (m < 0) continue;
            bl2 |= this.placeColumn(levelAccessor, i, blockPos3, m, BasaltColumnsFeature.calculateReach(random, columnFeatureConfiguration));
        }
        return bl2;
    }

    private boolean placeColumn(LevelAccessor levelAccessor, int i, BlockPos blockPos, int j, int k) {
        boolean bl = false;
        block0: for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.getX() - k, blockPos.getY(), blockPos.getZ() - k, blockPos.getX() + k, blockPos.getY(), blockPos.getZ() + k)) {
            BlockPos blockPos3;
            int l = blockPos2.distManhattan(blockPos);
            BlockPos blockPos4 = blockPos3 = BasaltColumnsFeature.isAirOrLavaOcean(levelAccessor, i, blockPos2) ? BasaltColumnsFeature.findSurface(levelAccessor, i, blockPos2.mutable(), l) : BasaltColumnsFeature.findAir(levelAccessor, blockPos2.mutable(), l);
            if (blockPos3 == null) continue;
            BlockPos.MutableBlockPos mutableBlockPos = blockPos3.mutable();
            for (int m = j - l / 2; m >= 0; --m) {
                if (BasaltColumnsFeature.isAirOrLavaOcean(levelAccessor, i, mutableBlockPos)) {
                    this.setBlock(levelAccessor, mutableBlockPos, Blocks.BASALT.defaultBlockState());
                    mutableBlockPos.move(Direction.UP);
                    bl = true;
                    continue;
                }
                if (levelAccessor.getBlockState(mutableBlockPos).getBlock() != Blocks.BASALT) continue block0;
                mutableBlockPos.move(Direction.UP);
            }
        }
        return bl;
    }

    @Nullable
    private static BlockPos findSurface(LevelAccessor levelAccessor, int i, BlockPos.MutableBlockPos mutableBlockPos, int j) {
        while (mutableBlockPos.getY() > 1 && j > 0) {
            --j;
            if (BasaltColumnsFeature.isAirOrLavaOcean(levelAccessor, i, mutableBlockPos)) {
                BlockState blockState = levelAccessor.getBlockState(mutableBlockPos.move(Direction.DOWN));
                mutableBlockPos.move(Direction.UP);
                Block block = blockState.getBlock();
                if (block != Blocks.LAVA && block != Blocks.BEDROCK && block != Blocks.MAGMA_BLOCK && !blockState.isAir()) {
                    return mutableBlockPos;
                }
            }
            mutableBlockPos.move(Direction.DOWN);
        }
        return null;
    }

    @Nullable
    private static BlockPos findAir(LevelAccessor levelAccessor, BlockPos.MutableBlockPos mutableBlockPos, int i) {
        while (mutableBlockPos.getY() < levelAccessor.getMaxBuildHeight() && i > 0) {
            --i;
            if (levelAccessor.getBlockState(mutableBlockPos).isAir()) {
                return mutableBlockPos;
            }
            mutableBlockPos.move(Direction.UP);
        }
        return null;
    }

    private static int calculateHeight(Random random, ColumnFeatureConfiguration columnFeatureConfiguration) {
        return columnFeatureConfiguration.minimumHeight + random.nextInt(columnFeatureConfiguration.maximumHeight - columnFeatureConfiguration.minimumHeight + 1);
    }

    private static int calculateReach(Random random, ColumnFeatureConfiguration columnFeatureConfiguration) {
        return columnFeatureConfiguration.minimumReach + random.nextInt(columnFeatureConfiguration.maximumReach - columnFeatureConfiguration.minimumReach + 1);
    }

    private static boolean isAirOrLavaOcean(LevelAccessor levelAccessor, int i, BlockPos blockPos) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        return blockState.isAir() || blockState.getBlock() == Blocks.LAVA && blockPos.getY() <= i;
    }
}

