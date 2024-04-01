package net.minecraft.world.grid;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SubGridMovementCollider {
	private final LongList edgeBlocks;
	private final BlockPos size;

	private SubGridMovementCollider(LongList longList, BlockPos blockPos) {
		this.edgeBlocks = longList;
		this.size = blockPos;
	}

	public static SubGridMovementCollider generate(SubGridBlocks subGridBlocks, Direction direction) {
		LongList longList = new LongArrayList();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		Direction direction2 = direction.getOpposite();

		for (BlockPos blockPos : getFrontSide(subGridBlocks, direction)) {
			mutableBlockPos.set(blockPos);
			boolean bl = false;
			int i = direction.getAxis().choose(subGridBlocks.sizeX(), subGridBlocks.sizeY(), subGridBlocks.sizeZ());

			for (int j = 0; j < i; j++) {
				BlockState blockState = subGridBlocks.getBlockState(mutableBlockPos);
				if (isCollidable(blockState)) {
					if (!bl) {
						longList.add(mutableBlockPos.asLong());
					}

					bl = true;
				} else {
					bl = false;
				}

				mutableBlockPos.move(direction2);
			}
		}

		return new SubGridMovementCollider(longList, new BlockPos(subGridBlocks.sizeX(), subGridBlocks.sizeY(), subGridBlocks.sizeZ()));
	}

	private static Iterable<BlockPos> getFrontSide(SubGridBlocks subGridBlocks, Direction direction) {
		BlockPos blockPos = new BlockPos(
			Math.max(direction.getStepX(), 0) * (subGridBlocks.sizeX() - 1),
			Math.max(direction.getStepY(), 0) * (subGridBlocks.sizeY() - 1),
			Math.max(direction.getStepZ(), 0) * (subGridBlocks.sizeZ() - 1)
		);
		BlockPos blockPos2 = blockPos.offset(
			direction.getAxis() == Direction.Axis.X ? 0 : subGridBlocks.sizeX() - 1,
			direction.getAxis() == Direction.Axis.Y ? 0 : subGridBlocks.sizeY() - 1,
			direction.getAxis() == Direction.Axis.Z ? 0 : subGridBlocks.sizeZ() - 1
		);
		return BlockPos.betweenClosed(blockPos, blockPos2);
	}

	public boolean checkCollision(Level level, BlockPos blockPos) {
		int i = blockPos.getY();
		int j = i + this.size.getY() - 1;
		if (i >= level.getMinBuildHeight() && j < level.getMaxBuildHeight()) {
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			LongIterator longIterator = this.edgeBlocks.longIterator();

			while (longIterator.hasNext()) {
				mutableBlockPos.set(longIterator.nextLong());
				mutableBlockPos.move(blockPos);
				if (isCollidable(level.getBlockState(mutableBlockPos))) {
					return true;
				}
			}

			return false;
		} else {
			return true;
		}
	}

	private static boolean isCollidable(BlockState blockState) {
		return !blockState.canBeReplaced();
	}
}
