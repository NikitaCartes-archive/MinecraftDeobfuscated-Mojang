package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public interface LevelWriter {
	boolean setBlock(BlockPos blockPos, BlockState blockState, int i);

	boolean removeBlock(BlockPos blockPos, boolean bl);

	default boolean destroyBlock(BlockPos blockPos, boolean bl) {
		return this.destroyBlock(blockPos, bl, null);
	}

	boolean destroyBlock(BlockPos blockPos, boolean bl, @Nullable Entity entity);

	default boolean addFreshEntity(Entity entity) {
		return false;
	}
}
