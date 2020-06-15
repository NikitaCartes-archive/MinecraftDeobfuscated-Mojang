package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public interface LevelWriter {
	boolean setBlock(BlockPos blockPos, BlockState blockState, int i, int j);

	default boolean setBlock(BlockPos blockPos, BlockState blockState, int i) {
		return this.setBlock(blockPos, blockState, i, 512);
	}

	boolean removeBlock(BlockPos blockPos, boolean bl);

	default boolean destroyBlock(BlockPos blockPos, boolean bl) {
		return this.destroyBlock(blockPos, bl, null);
	}

	default boolean destroyBlock(BlockPos blockPos, boolean bl, @Nullable Entity entity) {
		return this.destroyBlock(blockPos, bl, entity, 512);
	}

	boolean destroyBlock(BlockPos blockPos, boolean bl, @Nullable Entity entity, int i);

	default boolean addFreshEntity(Entity entity) {
		return false;
	}
}
