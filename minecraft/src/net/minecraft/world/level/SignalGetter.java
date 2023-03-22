package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface SignalGetter extends BlockGetter {
	Direction[] DIRECTIONS = Direction.values();

	default int getDirectSignal(BlockPos blockPos, Direction direction) {
		return this.getBlockState(blockPos).getDirectSignal(this, blockPos, direction);
	}

	default int getDirectSignalTo(BlockPos blockPos) {
		int i = 0;
		i = Math.max(i, this.getDirectSignal(blockPos.below(), Direction.DOWN));
		if (i >= 15) {
			return i;
		} else {
			i = Math.max(i, this.getDirectSignal(blockPos.above(), Direction.UP));
			if (i >= 15) {
				return i;
			} else {
				i = Math.max(i, this.getDirectSignal(blockPos.north(), Direction.NORTH));
				if (i >= 15) {
					return i;
				} else {
					i = Math.max(i, this.getDirectSignal(blockPos.south(), Direction.SOUTH));
					if (i >= 15) {
						return i;
					} else {
						i = Math.max(i, this.getDirectSignal(blockPos.west(), Direction.WEST));
						if (i >= 15) {
							return i;
						} else {
							i = Math.max(i, this.getDirectSignal(blockPos.east(), Direction.EAST));
							return i >= 15 ? i : i;
						}
					}
				}
			}
		}
	}

	default int getControlInputSignal(BlockPos blockPos, Direction direction, boolean bl) {
		BlockState blockState = this.getBlockState(blockPos);
		if (bl) {
			return DiodeBlock.isDiode(blockState) ? this.getDirectSignal(blockPos, direction) : 0;
		} else if (blockState.is(Blocks.REDSTONE_BLOCK)) {
			return 15;
		} else if (blockState.is(Blocks.REDSTONE_WIRE)) {
			return (Integer)blockState.getValue(RedStoneWireBlock.POWER);
		} else {
			return blockState.isSignalSource() ? this.getDirectSignal(blockPos, direction) : 0;
		}
	}

	default boolean hasSignal(BlockPos blockPos, Direction direction) {
		return this.getSignal(blockPos, direction) > 0;
	}

	default int getSignal(BlockPos blockPos, Direction direction) {
		BlockState blockState = this.getBlockState(blockPos);
		int i = blockState.getSignal(this, blockPos, direction);
		return blockState.isRedstoneConductor(this, blockPos) ? Math.max(i, this.getDirectSignalTo(blockPos)) : i;
	}

	default boolean hasNeighborSignal(BlockPos blockPos) {
		if (this.getSignal(blockPos.below(), Direction.DOWN) > 0) {
			return true;
		} else if (this.getSignal(blockPos.above(), Direction.UP) > 0) {
			return true;
		} else if (this.getSignal(blockPos.north(), Direction.NORTH) > 0) {
			return true;
		} else if (this.getSignal(blockPos.south(), Direction.SOUTH) > 0) {
			return true;
		} else {
			return this.getSignal(blockPos.west(), Direction.WEST) > 0 ? true : this.getSignal(blockPos.east(), Direction.EAST) > 0;
		}
	}

	default int getBestNeighborSignal(BlockPos blockPos) {
		int i = 0;

		for (Direction direction : DIRECTIONS) {
			int j = this.getSignal(blockPos.relative(direction), direction);
			if (j >= 15) {
				return 15;
			}

			if (j > i) {
				i = j;
			}
		}

		return i;
	}
}
