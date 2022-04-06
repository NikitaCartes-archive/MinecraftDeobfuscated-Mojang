/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;

public class RootSystemFeature
extends Feature<RootSystemConfiguration> {
    public RootSystemFeature(Codec<RootSystemConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<RootSystemConfiguration> featurePlaceContext) {
        BlockPos blockPos;
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        if (!worldGenLevel.getBlockState(blockPos = featurePlaceContext.origin()).isAir()) {
            return false;
        }
        RandomSource randomSource = featurePlaceContext.random();
        BlockPos blockPos2 = featurePlaceContext.origin();
        RootSystemConfiguration rootSystemConfiguration = featurePlaceContext.config();
        BlockPos.MutableBlockPos mutableBlockPos = blockPos2.mutable();
        if (RootSystemFeature.placeDirtAndTree(worldGenLevel, featurePlaceContext.chunkGenerator(), rootSystemConfiguration, randomSource, mutableBlockPos, blockPos2)) {
            RootSystemFeature.placeRoots(worldGenLevel, rootSystemConfiguration, randomSource, blockPos2, mutableBlockPos);
        }
        return true;
    }

    private static boolean spaceForTree(WorldGenLevel worldGenLevel, RootSystemConfiguration rootSystemConfiguration, BlockPos blockPos) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (int i = 1; i <= rootSystemConfiguration.requiredVerticalSpaceForTree; ++i) {
            mutableBlockPos.move(Direction.UP);
            BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos);
            if (RootSystemFeature.isAllowedTreeSpace(blockState, i, rootSystemConfiguration.allowedVerticalWaterForTree)) continue;
            return false;
        }
        return true;
    }

    private static boolean isAllowedTreeSpace(BlockState blockState, int i, int j) {
        if (blockState.isAir()) {
            return true;
        }
        int k = i + 1;
        return k <= j && blockState.getFluidState().is(FluidTags.WATER);
    }

    private static boolean placeDirtAndTree(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, RootSystemConfiguration rootSystemConfiguration, RandomSource randomSource, BlockPos.MutableBlockPos mutableBlockPos, BlockPos blockPos) {
        for (int i = 0; i < rootSystemConfiguration.rootColumnMaxHeight; ++i) {
            mutableBlockPos.move(Direction.UP);
            if (!rootSystemConfiguration.allowedTreePosition.test(worldGenLevel, mutableBlockPos) || !RootSystemFeature.spaceForTree(worldGenLevel, rootSystemConfiguration, mutableBlockPos)) continue;
            Vec3i blockPos2 = mutableBlockPos.below();
            if (worldGenLevel.getFluidState((BlockPos)blockPos2).is(FluidTags.LAVA) || !worldGenLevel.getBlockState((BlockPos)blockPos2).getMaterial().isSolid()) {
                return false;
            }
            if (!rootSystemConfiguration.treeFeature.value().place(worldGenLevel, chunkGenerator, randomSource, mutableBlockPos)) continue;
            RootSystemFeature.placeDirt(blockPos, blockPos.getY() + i, worldGenLevel, rootSystemConfiguration, randomSource);
            return true;
        }
        return false;
    }

    private static void placeDirt(BlockPos blockPos, int i, WorldGenLevel worldGenLevel, RootSystemConfiguration rootSystemConfiguration, RandomSource randomSource) {
        int j = blockPos.getX();
        int k = blockPos.getZ();
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (int l = blockPos.getY(); l < i; ++l) {
            RootSystemFeature.placeRootedDirt(worldGenLevel, rootSystemConfiguration, randomSource, j, k, mutableBlockPos.set(j, l, k));
        }
    }

    private static void placeRootedDirt(WorldGenLevel worldGenLevel, RootSystemConfiguration rootSystemConfiguration, RandomSource randomSource, int i, int j, BlockPos.MutableBlockPos mutableBlockPos) {
        int k = rootSystemConfiguration.rootRadius;
        Predicate<BlockState> predicate = blockState -> blockState.is(rootSystemConfiguration.rootReplaceable);
        for (int l = 0; l < rootSystemConfiguration.rootPlacementAttempts; ++l) {
            mutableBlockPos.setWithOffset(mutableBlockPos, randomSource.nextInt(k) - randomSource.nextInt(k), 0, randomSource.nextInt(k) - randomSource.nextInt(k));
            if (predicate.test(worldGenLevel.getBlockState(mutableBlockPos))) {
                worldGenLevel.setBlock(mutableBlockPos, rootSystemConfiguration.rootStateProvider.getState(randomSource, mutableBlockPos), 2);
            }
            mutableBlockPos.setX(i);
            mutableBlockPos.setZ(j);
        }
    }

    private static void placeRoots(WorldGenLevel worldGenLevel, RootSystemConfiguration rootSystemConfiguration, RandomSource randomSource, BlockPos blockPos, BlockPos.MutableBlockPos mutableBlockPos) {
        int i = rootSystemConfiguration.hangingRootRadius;
        int j = rootSystemConfiguration.hangingRootsVerticalSpan;
        for (int k = 0; k < rootSystemConfiguration.hangingRootPlacementAttempts; ++k) {
            BlockState blockState;
            mutableBlockPos.setWithOffset(blockPos, randomSource.nextInt(i) - randomSource.nextInt(i), randomSource.nextInt(j) - randomSource.nextInt(j), randomSource.nextInt(i) - randomSource.nextInt(i));
            if (!worldGenLevel.isEmptyBlock(mutableBlockPos) || !(blockState = rootSystemConfiguration.hangingRootStateProvider.getState(randomSource, mutableBlockPos)).canSurvive(worldGenLevel, mutableBlockPos) || !worldGenLevel.getBlockState((BlockPos)mutableBlockPos.above()).isFaceSturdy(worldGenLevel, mutableBlockPos, Direction.DOWN)) continue;
            worldGenLevel.setBlock(mutableBlockPos, blockState, 2);
        }
    }
}

