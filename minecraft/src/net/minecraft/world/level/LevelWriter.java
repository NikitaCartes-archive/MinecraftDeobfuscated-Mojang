package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public interface LevelWriter {
	boolean setBlock(BlockPos blockPos, BlockState blockState, int i);

	boolean removeBlock(BlockPos blockPos, boolean bl);

	boolean destroyBlock(BlockPos blockPos, boolean bl);

	default boolean addFreshEntity(Entity entity) {
		return false;
	}
}
