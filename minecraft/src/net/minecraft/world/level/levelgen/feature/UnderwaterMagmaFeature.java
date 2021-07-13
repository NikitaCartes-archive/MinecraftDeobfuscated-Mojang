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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.UnderwaterMagmaConfiguration;
import net.minecraft.world.phys.AABB;

public class UnderwaterMagmaFeature extends Feature<UnderwaterMagmaConfiguration> {
	public UnderwaterMagmaFeature(Codec<UnderwaterMagmaConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<UnderwaterMagmaConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		UnderwaterMagmaConfiguration underwaterMagmaConfiguration = featurePlaceContext.config();
		Random random = featurePlaceContext.random();
		if (featurePlaceContext.config().minDistanceBelowSurface > 0) {
			int i = worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, blockPos.getX(), blockPos.getZ());
			if (blockPos.getY() >= i - featurePlaceContext.config().minDistanceBelowSurface) {
				return false;
			}
		}

		OptionalInt optionalInt = getFloorY(worldGenLevel, blockPos, underwaterMagmaConfiguration);
		if (!optionalInt.isPresent()) {
			return false;
		} else {
			BlockPos blockPos2 = blockPos.atY(optionalInt.getAsInt());
			Vec3i vec3i = new Vec3i(
				underwaterMagmaConfiguration.placementRadiusAroundFloor,
				underwaterMagmaConfiguration.placementRadiusAroundFloor,
				underwaterMagmaConfiguration.placementRadiusAroundFloor
			);
			AABB aABB = new AABB(blockPos2.subtract(vec3i), blockPos2.offset(vec3i));
			return BlockPos.betweenClosedStream(aABB)
					.filter(blockPosx -> random.nextFloat() < underwaterMagmaConfiguration.placementProbabilityPerValidPosition)
					.filter(blockPosx -> this.isValidPlacement(worldGenLevel, blockPosx))
					.mapToInt(blockPosx -> {
						worldGenLevel.setBlock(blockPosx, Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
						return 1;
					})
					.sum()
				> 0;
		}
	}

	private static OptionalInt getFloorY(WorldGenLevel worldGenLevel, BlockPos blockPos, UnderwaterMagmaConfiguration underwaterMagmaConfiguration) {
		Predicate<BlockState> predicate = blockState -> blockState.is(Blocks.WATER);
		Predicate<BlockState> predicate2 = blockState -> !blockState.is(Blocks.WATER);
		Optional<Column> optional = Column.scan(worldGenLevel, blockPos, underwaterMagmaConfiguration.floorSearchRange, predicate, predicate2);
		return (OptionalInt)optional.map(Column::getFloor).orElseGet(OptionalInt::empty);
	}

	private boolean isValidPlacement(WorldGenLevel worldGenLevel, BlockPos blockPos) {
		if (!this.isWaterOrAir(worldGenLevel, blockPos) && !this.isWaterOrAir(worldGenLevel, blockPos.below())) {
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				if (this.isWaterOrAir(worldGenLevel, blockPos.relative(direction))) {
					return false;
				}
			}

			return true;
		} else {
			return false;
		}
	}

	private boolean isWaterOrAir(LevelAccessor levelAccessor, BlockPos blockPos) {
		BlockState blockState = levelAccessor.getBlockState(blockPos);
		return blockState.is(Blocks.WATER) || blockState.isAir();
	}
}
