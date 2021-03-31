package net.minecraft.world.level.block.piston;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

public class PistonStructureResolver {
	public static final int MAX_PUSH_DEPTH = 12;
	private final Level level;
	private final BlockPos pistonPos;
	private final boolean extending;
	private final BlockPos startPos;
	private final Direction pushDirection;
	private final List<BlockPos> toPush = Lists.<BlockPos>newArrayList();
	private final List<BlockPos> toDestroy = Lists.<BlockPos>newArrayList();
	private final Direction pistonDirection;

	public PistonStructureResolver(Level level, BlockPos blockPos, Direction direction, boolean bl) {
		this.level = level;
		this.pistonPos = blockPos;
		this.pistonDirection = direction;
		this.extending = bl;
		if (bl) {
			this.pushDirection = direction;
			this.startPos = blockPos.relative(direction);
		} else {
			this.pushDirection = direction.getOpposite();
			this.startPos = blockPos.relative(direction, 2);
		}
	}

	public boolean resolve() {
		this.toPush.clear();
		this.toDestroy.clear();
		BlockState blockState = this.level.getBlockState(this.startPos);
		if (!PistonBaseBlock.isPushable(blockState, this.level, this.startPos, this.pushDirection, false, this.pistonDirection)) {
			if (this.extending && blockState.getPistonPushReaction() == PushReaction.DESTROY) {
				this.toDestroy.add(this.startPos);
				return true;
			} else {
				return false;
			}
		} else if (!this.addBlockLine(this.startPos, this.pushDirection)) {
			return false;
		} else {
			for (int i = 0; i < this.toPush.size(); i++) {
				BlockPos blockPos = (BlockPos)this.toPush.get(i);
				if (isSticky(this.level.getBlockState(blockPos)) && !this.addBranchingBlocks(blockPos)) {
					return false;
				}
			}

			return true;
		}
	}

	private static boolean isSticky(BlockState blockState) {
		return blockState.is(Blocks.SLIME_BLOCK) || blockState.is(Blocks.HONEY_BLOCK);
	}

	private static boolean canStickToEachOther(BlockState blockState, BlockState blockState2) {
		if (blockState.is(Blocks.HONEY_BLOCK) && blockState2.is(Blocks.SLIME_BLOCK)) {
			return false;
		} else {
			return blockState.is(Blocks.SLIME_BLOCK) && blockState2.is(Blocks.HONEY_BLOCK) ? false : isSticky(blockState) || isSticky(blockState2);
		}
	}

	private boolean addBlockLine(BlockPos blockPos, Direction direction) {
		BlockState blockState = this.level.getBlockState(blockPos);
		if (blockState.isAir()) {
			return true;
		} else if (!PistonBaseBlock.isPushable(blockState, this.level, blockPos, this.pushDirection, false, direction)) {
			return true;
		} else if (blockPos.equals(this.pistonPos)) {
			return true;
		} else if (this.toPush.contains(blockPos)) {
			return true;
		} else {
			int i = 1;
			if (i + this.toPush.size() > 12) {
				return false;
			} else {
				while (isSticky(blockState)) {
					BlockPos blockPos2 = blockPos.relative(this.pushDirection.getOpposite(), i);
					BlockState blockState2 = blockState;
					blockState = this.level.getBlockState(blockPos2);
					if (blockState.isAir()
						|| !canStickToEachOther(blockState2, blockState)
						|| !PistonBaseBlock.isPushable(blockState, this.level, blockPos2, this.pushDirection, false, this.pushDirection.getOpposite())
						|| blockPos2.equals(this.pistonPos)) {
						break;
					}

					if (++i + this.toPush.size() > 12) {
						return false;
					}
				}

				int j = 0;

				for (int k = i - 1; k >= 0; k--) {
					this.toPush.add(blockPos.relative(this.pushDirection.getOpposite(), k));
					j++;
				}

				int k = 1;

				while (true) {
					BlockPos blockPos3 = blockPos.relative(this.pushDirection, k);
					int l = this.toPush.indexOf(blockPos3);
					if (l > -1) {
						this.reorderListAtCollision(j, l);

						for (int m = 0; m <= l + j; m++) {
							BlockPos blockPos4 = (BlockPos)this.toPush.get(m);
							if (isSticky(this.level.getBlockState(blockPos4)) && !this.addBranchingBlocks(blockPos4)) {
								return false;
							}
						}

						return true;
					}

					blockState = this.level.getBlockState(blockPos3);
					if (blockState.isAir()) {
						return true;
					}

					if (!PistonBaseBlock.isPushable(blockState, this.level, blockPos3, this.pushDirection, true, this.pushDirection) || blockPos3.equals(this.pistonPos)) {
						return false;
					}

					if (blockState.getPistonPushReaction() == PushReaction.DESTROY) {
						this.toDestroy.add(blockPos3);
						return true;
					}

					if (this.toPush.size() >= 12) {
						return false;
					}

					this.toPush.add(blockPos3);
					j++;
					k++;
				}
			}
		}
	}

	private void reorderListAtCollision(int i, int j) {
		List<BlockPos> list = Lists.<BlockPos>newArrayList();
		List<BlockPos> list2 = Lists.<BlockPos>newArrayList();
		List<BlockPos> list3 = Lists.<BlockPos>newArrayList();
		list.addAll(this.toPush.subList(0, j));
		list2.addAll(this.toPush.subList(this.toPush.size() - i, this.toPush.size()));
		list3.addAll(this.toPush.subList(j, this.toPush.size() - i));
		this.toPush.clear();
		this.toPush.addAll(list);
		this.toPush.addAll(list2);
		this.toPush.addAll(list3);
	}

	private boolean addBranchingBlocks(BlockPos blockPos) {
		BlockState blockState = this.level.getBlockState(blockPos);

		for (Direction direction : Direction.values()) {
			if (direction.getAxis() != this.pushDirection.getAxis()) {
				BlockPos blockPos2 = blockPos.relative(direction);
				BlockState blockState2 = this.level.getBlockState(blockPos2);
				if (canStickToEachOther(blockState2, blockState) && !this.addBlockLine(blockPos2, direction)) {
					return false;
				}
			}
		}

		return true;
	}

	public Direction getPushDirection() {
		return this.pushDirection;
	}

	public List<BlockPos> getToPush() {
		return this.toPush;
	}

	public List<BlockPos> getToDestroy() {
		return this.toDestroy;
	}
}
