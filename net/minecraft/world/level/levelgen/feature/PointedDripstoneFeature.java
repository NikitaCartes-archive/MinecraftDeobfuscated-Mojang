/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.DripstoneUtils;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.PointedDripstoneConfiguration;

public class PointedDripstoneFeature
extends Feature<PointedDripstoneConfiguration> {
    public PointedDripstoneFeature(Codec<PointedDripstoneConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<PointedDripstoneConfiguration> featurePlaceContext) {
        WorldGenLevel levelAccessor = featurePlaceContext.level();
        BlockPos blockPos = featurePlaceContext.origin();
        RandomSource randomSource = featurePlaceContext.random();
        PointedDripstoneConfiguration pointedDripstoneConfiguration = featurePlaceContext.config();
        Optional<Direction> optional = PointedDripstoneFeature.getTipDirection(levelAccessor, blockPos, randomSource);
        if (optional.isEmpty()) {
            return false;
        }
        BlockPos blockPos2 = blockPos.relative(optional.get().getOpposite());
        PointedDripstoneFeature.createPatchOfDripstoneBlocks(levelAccessor, randomSource, blockPos2, pointedDripstoneConfiguration);
        int i = randomSource.nextFloat() < pointedDripstoneConfiguration.chanceOfTallerDripstone && DripstoneUtils.isEmptyOrWater(levelAccessor.getBlockState(blockPos.relative(optional.get()))) ? 2 : 1;
        DripstoneUtils.growPointedDripstone(levelAccessor, blockPos, optional.get(), i, false);
        return true;
    }

    private static Optional<Direction> getTipDirection(LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource) {
        boolean bl = DripstoneUtils.isDripstoneBase(levelAccessor.getBlockState(blockPos.above()));
        boolean bl2 = DripstoneUtils.isDripstoneBase(levelAccessor.getBlockState(blockPos.below()));
        if (bl && bl2) {
            return Optional.of(randomSource.nextBoolean() ? Direction.DOWN : Direction.UP);
        }
        if (bl) {
            return Optional.of(Direction.DOWN);
        }
        if (bl2) {
            return Optional.of(Direction.UP);
        }
        return Optional.empty();
    }

    private static void createPatchOfDripstoneBlocks(LevelAccessor levelAccessor, RandomSource randomSource, BlockPos blockPos, PointedDripstoneConfiguration pointedDripstoneConfiguration) {
        DripstoneUtils.placeDripstoneBlockIfPossible(levelAccessor, blockPos);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (randomSource.nextFloat() > pointedDripstoneConfiguration.chanceOfDirectionalSpread) continue;
            BlockPos blockPos2 = blockPos.relative(direction);
            DripstoneUtils.placeDripstoneBlockIfPossible(levelAccessor, blockPos2);
            if (randomSource.nextFloat() > pointedDripstoneConfiguration.chanceOfSpreadRadius2) continue;
            BlockPos blockPos3 = blockPos2.relative(Direction.getRandom(randomSource));
            DripstoneUtils.placeDripstoneBlockIfPossible(levelAccessor, blockPos3);
            if (randomSource.nextFloat() > pointedDripstoneConfiguration.chanceOfSpreadRadius3) continue;
            BlockPos blockPos4 = blockPos3.relative(Direction.getRandom(randomSource));
            DripstoneUtils.placeDripstoneBlockIfPossible(levelAccessor, blockPos4);
        }
    }
}

