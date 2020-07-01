package net.minecraft.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockSource extends Position {
	@Override
	double x();

	@Override
	double y();

	@Override
	double z();

	BlockPos getPos();

	BlockState getBlockState();

	<T extends BlockEntity> T getEntity();

	ServerLevel getLevel();
}
