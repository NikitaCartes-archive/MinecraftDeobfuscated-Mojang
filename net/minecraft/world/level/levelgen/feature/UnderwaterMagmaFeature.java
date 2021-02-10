/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.UnderwaterMagmaConfiguration;
import net.minecraft.world.phys.AABB;

public class UnderwaterMagmaFeature
extends Feature<UnderwaterMagmaConfiguration> {
    public UnderwaterMagmaFeature(Codec<UnderwaterMagmaConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<UnderwaterMagmaConfiguration> featurePlaceContext) {
        Vec3i vec3i;
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockPos blockPos2 = featurePlaceContext.origin();
        UnderwaterMagmaConfiguration underwaterMagmaConfiguration = featurePlaceContext.config();
        Random random = featurePlaceContext.random();
        OptionalInt optionalInt = UnderwaterMagmaFeature.getFloorY(worldGenLevel, blockPos2, underwaterMagmaConfiguration);
        if (!optionalInt.isPresent()) {
            return false;
        }
        BlockPos blockPos22 = blockPos2.atY(optionalInt.getAsInt());
        AABB aABB = new AABB(blockPos22.subtract(vec3i = new Vec3i(underwaterMagmaConfiguration.placementRadiusAroundFloor, underwaterMagmaConfiguration.placementRadiusAroundFloor, underwaterMagmaConfiguration.placementRadiusAroundFloor)), blockPos22.offset(vec3i));
        return BlockPos.betweenClosedStream(aABB).filter(blockPos -> random.nextFloat() < underwaterMagmaConfiguration.placementProbabilityPerValidPosition).filter(blockPos -> this.isValidPlacement(worldGenLevel, (BlockPos)blockPos)).mapToInt(blockPos -> {
            worldGenLevel.setBlock((BlockPos)blockPos, Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
            return 1;
        }).sum() > 0;
    }

    private static OptionalInt getFloorY(WorldGenLevel worldGenLevel, BlockPos blockPos, UnderwaterMagmaConfiguration underwaterMagmaConfiguration) {
        Predicate<BlockState> predicate = blockState -> blockState.is(Blocks.WATER);
        Predicate<BlockState> predicate2 = blockState -> !blockState.is(Blocks.WATER);
        Optional<Column> optional = Column.scan(worldGenLevel, blockPos, underwaterMagmaConfiguration.floorSearchRange, predicate, predicate2);
        return optional.map(Column::getFloor).orElseGet(OptionalInt::empty);
    }

    private boolean isValidPlacement(WorldGenLevel worldGenLevel, BlockPos blockPos) {
        if (this.isWaterOrAir(worldGenLevel, blockPos) || this.isWaterOrAir(worldGenLevel, blockPos.below())) {
            return false;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (!this.isWaterOrAir(worldGenLevel, blockPos.relative(direction))) continue;
            return false;
        }
        return true;
    }

    private boolean isWaterOrAir(LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        return blockState.is(Blocks.WATER) || blockState.isAir();
    }
}

