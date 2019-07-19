package net.minecraft.core;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockSourceImpl implements BlockSource {
	private final Level level;
	private final BlockPos pos;

	public BlockSourceImpl(Level level, BlockPos blockPos) {
		this.level = level;
		this.pos = blockPos;
	}

	@Override
	public Level getLevel() {
		return this.level;
	}

	@Override
	public double x() {
		return (double)this.pos.getX() + 0.5;
	}

	@Override
	public double y() {
		return (double)this.pos.getY() + 0.5;
	}

	@Override
	public double z() {
		return (double)this.pos.getZ() + 0.5;
	}

	@Override
	public BlockPos getPos() {
		return this.pos;
	}

	@Override
	public BlockState getBlockState() {
		return this.level.getBlockState(this.pos);
	}

	@Override
	public <T extends BlockEntity> T getEntity() {
		return (T)this.level.getBlockEntity(this.pos);
	}
}
