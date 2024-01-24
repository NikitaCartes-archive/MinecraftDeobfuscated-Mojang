package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

public interface SpawnPlacementTypes {
	SpawnPlacementType NO_RESTRICTIONS = (levelReader, blockPos, entityType) -> true;
	SpawnPlacementType IN_WATER = (levelReader, blockPos, entityType) -> {
		if (entityType != null && levelReader.getWorldBorder().isWithinBounds(blockPos)) {
			BlockPos blockPos2 = blockPos.above();
			return levelReader.getFluidState(blockPos).is(FluidTags.WATER) && !levelReader.getBlockState(blockPos2).isRedstoneConductor(levelReader, blockPos2);
		} else {
			return false;
		}
	};
	SpawnPlacementType IN_LAVA = (levelReader, blockPos, entityType) -> entityType != null && levelReader.getWorldBorder().isWithinBounds(blockPos)
			? levelReader.getFluidState(blockPos).is(FluidTags.LAVA)
			: false;
	SpawnPlacementType ON_GROUND = new SpawnPlacementType() {
		@Override
		public boolean isSpawnPositionOk(LevelReader levelReader, BlockPos blockPos, @Nullable EntityType<?> entityType) {
			if (entityType != null && levelReader.getWorldBorder().isWithinBounds(blockPos)) {
				BlockPos blockPos2 = blockPos.above();
				BlockPos blockPos3 = blockPos.below();
				BlockState blockState = levelReader.getBlockState(blockPos3);
				return !blockState.isValidSpawn(levelReader, blockPos3, entityType)
					? false
					: this.isValidEmptySpawnBlock(levelReader, blockPos, entityType) && this.isValidEmptySpawnBlock(levelReader, blockPos2, entityType);
			} else {
				return false;
			}
		}

		private boolean isValidEmptySpawnBlock(LevelReader levelReader, BlockPos blockPos, EntityType<?> entityType) {
			BlockState blockState = levelReader.getBlockState(blockPos);
			return NaturalSpawner.isValidEmptySpawnBlock(levelReader, blockPos, blockState, blockState.getFluidState(), entityType);
		}

		@Override
		public BlockPos adjustSpawnPosition(LevelReader levelReader, BlockPos blockPos) {
			BlockPos blockPos2 = blockPos.below();
			return levelReader.getBlockState(blockPos2).isPathfindable(levelReader, blockPos2, PathComputationType.LAND) ? blockPos2 : blockPos;
		}
	};
}
