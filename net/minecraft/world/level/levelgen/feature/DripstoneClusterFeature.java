/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ClampedNormalFloat;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.DripstoneUtils;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration;

public class DripstoneClusterFeature
extends Feature<DripstoneClusterConfiguration> {
    public DripstoneClusterFeature(Codec<DripstoneClusterConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<DripstoneClusterConfiguration> featurePlaceContext) {
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockPos blockPos = featurePlaceContext.origin();
        DripstoneClusterConfiguration dripstoneClusterConfiguration = featurePlaceContext.config();
        RandomSource randomSource = featurePlaceContext.random();
        if (!DripstoneUtils.isEmptyOrWater(worldGenLevel, blockPos)) {
            return false;
        }
        int i = dripstoneClusterConfiguration.height.sample(randomSource);
        float f = dripstoneClusterConfiguration.wetness.sample(randomSource);
        float g = dripstoneClusterConfiguration.density.sample(randomSource);
        int j = dripstoneClusterConfiguration.radius.sample(randomSource);
        int k = dripstoneClusterConfiguration.radius.sample(randomSource);
        for (int l = -j; l <= j; ++l) {
            for (int m = -k; m <= k; ++m) {
                double d = this.getChanceOfStalagmiteOrStalactite(j, k, l, m, dripstoneClusterConfiguration);
                BlockPos blockPos2 = blockPos.offset(l, 0, m);
                this.placeColumn(worldGenLevel, randomSource, blockPos2, l, m, f, d, i, g, dripstoneClusterConfiguration);
            }
        }
        return true;
    }

    private void placeColumn(WorldGenLevel worldGenLevel, RandomSource randomSource, BlockPos blockPos, int i, int j, float f, double d, int k, float g, DripstoneClusterConfiguration dripstoneClusterConfiguration) {
        boolean bl4;
        int w;
        int p;
        boolean bl3;
        int o;
        int m;
        boolean bl2;
        Column column;
        boolean bl;
        Optional<Column> optional = Column.scan(worldGenLevel, blockPos, dripstoneClusterConfiguration.floorToCeilingSearchRange, DripstoneUtils::isEmptyOrWater, DripstoneUtils::isNeitherEmptyNorWater);
        if (!optional.isPresent()) {
            return;
        }
        OptionalInt optionalInt = optional.get().getCeiling();
        OptionalInt optionalInt2 = optional.get().getFloor();
        if (!optionalInt.isPresent() && !optionalInt2.isPresent()) {
            return;
        }
        boolean bl5 = bl = randomSource.nextFloat() < f;
        if (bl && optionalInt2.isPresent() && this.canPlacePool(worldGenLevel, blockPos.atY(optionalInt2.getAsInt()))) {
            int l = optionalInt2.getAsInt();
            column = optional.get().withFloor(OptionalInt.of(l - 1));
            worldGenLevel.setBlock(blockPos.atY(l), Blocks.WATER.defaultBlockState(), 2);
        } else {
            column = optional.get();
        }
        OptionalInt optionalInt3 = column.getFloor();
        boolean bl6 = bl2 = randomSource.nextDouble() < d;
        if (optionalInt.isPresent() && bl2 && !this.isLava(worldGenLevel, blockPos.atY(optionalInt.getAsInt()))) {
            m = dripstoneClusterConfiguration.dripstoneBlockLayerThickness.sample(randomSource);
            this.replaceBlocksWithDripstoneBlocks(worldGenLevel, blockPos.atY(optionalInt.getAsInt()), m, Direction.UP);
            int n = optionalInt3.isPresent() ? Math.min(k, optionalInt.getAsInt() - optionalInt3.getAsInt()) : k;
            o = this.getDripstoneHeight(randomSource, i, j, g, n, dripstoneClusterConfiguration);
        } else {
            o = 0;
        }
        boolean bl7 = bl3 = randomSource.nextDouble() < d;
        if (optionalInt3.isPresent() && bl3 && !this.isLava(worldGenLevel, blockPos.atY(optionalInt3.getAsInt()))) {
            p = dripstoneClusterConfiguration.dripstoneBlockLayerThickness.sample(randomSource);
            this.replaceBlocksWithDripstoneBlocks(worldGenLevel, blockPos.atY(optionalInt3.getAsInt()), p, Direction.DOWN);
            m = optionalInt.isPresent() ? Math.max(0, o + Mth.randomBetweenInclusive(randomSource, -dripstoneClusterConfiguration.maxStalagmiteStalactiteHeightDiff, dripstoneClusterConfiguration.maxStalagmiteStalactiteHeightDiff)) : this.getDripstoneHeight(randomSource, i, j, g, k, dripstoneClusterConfiguration);
        } else {
            m = 0;
        }
        if (optionalInt.isPresent() && optionalInt3.isPresent() && optionalInt.getAsInt() - o <= optionalInt3.getAsInt() + m) {
            int q = optionalInt3.getAsInt();
            int r = optionalInt.getAsInt();
            int s = Math.max(r - o, q + 1);
            int t = Math.min(q + m, r - 1);
            int u = Mth.randomBetweenInclusive(randomSource, s, t + 1);
            int v = u - 1;
            p = r - u;
            w = v - q;
        } else {
            p = o;
            w = m;
        }
        boolean bl8 = bl4 = randomSource.nextBoolean() && p > 0 && w > 0 && column.getHeight().isPresent() && p + w == column.getHeight().getAsInt();
        if (optionalInt.isPresent()) {
            DripstoneUtils.growPointedDripstone(worldGenLevel, blockPos.atY(optionalInt.getAsInt() - 1), Direction.DOWN, p, bl4);
        }
        if (optionalInt3.isPresent()) {
            DripstoneUtils.growPointedDripstone(worldGenLevel, blockPos.atY(optionalInt3.getAsInt() + 1), Direction.UP, w, bl4);
        }
    }

    private boolean isLava(LevelReader levelReader, BlockPos blockPos) {
        return levelReader.getBlockState(blockPos).is(Blocks.LAVA);
    }

    private int getDripstoneHeight(RandomSource randomSource, int i, int j, float f, int k, DripstoneClusterConfiguration dripstoneClusterConfiguration) {
        if (randomSource.nextFloat() > f) {
            return 0;
        }
        int l = Math.abs(i) + Math.abs(j);
        float g = (float)Mth.clampedMap((double)l, 0.0, (double)dripstoneClusterConfiguration.maxDistanceFromCenterAffectingHeightBias, (double)k / 2.0, 0.0);
        return (int)DripstoneClusterFeature.randomBetweenBiased(randomSource, 0.0f, k, g, dripstoneClusterConfiguration.heightDeviation);
    }

    private boolean canPlacePool(WorldGenLevel worldGenLevel, BlockPos blockPos) {
        BlockState blockState = worldGenLevel.getBlockState(blockPos);
        if (blockState.is(Blocks.WATER) || blockState.is(Blocks.DRIPSTONE_BLOCK) || blockState.is(Blocks.POINTED_DRIPSTONE)) {
            return false;
        }
        if (worldGenLevel.getBlockState(blockPos.above()).getFluidState().is(FluidTags.WATER)) {
            return false;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (this.canBeAdjacentToWater(worldGenLevel, blockPos.relative(direction))) continue;
            return false;
        }
        return this.canBeAdjacentToWater(worldGenLevel, blockPos.below());
    }

    private boolean canBeAdjacentToWater(LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        return blockState.is(BlockTags.BASE_STONE_OVERWORLD) || blockState.getFluidState().is(FluidTags.WATER);
    }

    private void replaceBlocksWithDripstoneBlocks(WorldGenLevel worldGenLevel, BlockPos blockPos, int i, Direction direction) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (int j = 0; j < i; ++j) {
            if (!DripstoneUtils.placeDripstoneBlockIfPossible(worldGenLevel, mutableBlockPos)) {
                return;
            }
            mutableBlockPos.move(direction);
        }
    }

    private double getChanceOfStalagmiteOrStalactite(int i, int j, int k, int l, DripstoneClusterConfiguration dripstoneClusterConfiguration) {
        int m = i - Math.abs(k);
        int n = j - Math.abs(l);
        int o = Math.min(m, n);
        return Mth.clampedMap(o, 0.0f, dripstoneClusterConfiguration.maxDistanceFromEdgeAffectingChanceOfDripstoneColumn, dripstoneClusterConfiguration.chanceOfDripstoneColumnAtMaxDistanceFromCenter, 1.0f);
    }

    private static float randomBetweenBiased(RandomSource randomSource, float f, float g, float h, float i) {
        return ClampedNormalFloat.sample(randomSource, h, i, f, g);
    }
}

