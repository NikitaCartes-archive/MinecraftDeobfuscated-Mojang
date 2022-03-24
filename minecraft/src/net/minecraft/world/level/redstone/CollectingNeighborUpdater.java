package net.minecraft.world.level.redstone;

import com.mojang.logging.LogUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class CollectingNeighborUpdater implements NeighborUpdater {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Level level;
	private final int maxChainedNeighborUpdates;
	private final ArrayDeque<CollectingNeighborUpdater.NeighborUpdates> stack = new ArrayDeque();
	private final List<CollectingNeighborUpdater.NeighborUpdates> addedThisLayer = new ArrayList();
	private int count = 0;

	public CollectingNeighborUpdater(Level level, int i) {
		this.level = level;
		this.maxChainedNeighborUpdates = i;
	}

	@Override
	public void shapeUpdate(Direction direction, BlockState blockState, BlockPos blockPos, BlockPos blockPos2, int i, int j) {
		this.addAndRun(blockPos, new CollectingNeighborUpdater.ShapeUpdate(direction, blockState, blockPos.immutable(), blockPos2.immutable(), i));
	}

	@Override
	public void neighborChanged(BlockPos blockPos, Block block, BlockPos blockPos2) {
		this.addAndRun(blockPos, new CollectingNeighborUpdater.SimpleNeighborUpdate(blockPos, block, blockPos2.immutable()));
	}

	@Override
	public void neighborChanged(BlockState blockState, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		this.addAndRun(blockPos, new CollectingNeighborUpdater.FullNeighborUpdate(blockState, blockPos.immutable(), block, blockPos2.immutable(), bl));
	}

	@Override
	public void updateNeighborsAtExceptFromFacing(BlockPos blockPos, Block block, @Nullable Direction direction) {
		this.addAndRun(blockPos, new CollectingNeighborUpdater.MultiNeighborUpdate(blockPos.immutable(), block, direction));
	}

	private void addAndRun(BlockPos blockPos, CollectingNeighborUpdater.NeighborUpdates neighborUpdates) {
		boolean bl = this.count > 0;
		boolean bl2 = this.maxChainedNeighborUpdates >= 0 && this.count >= this.maxChainedNeighborUpdates;
		this.count++;
		if (!bl2) {
			if (bl) {
				this.addedThisLayer.add(neighborUpdates);
			} else {
				this.stack.push(neighborUpdates);
			}
		} else if (this.count - 1 == this.maxChainedNeighborUpdates) {
			LOGGER.error("Too many chained neighbor updates. Skipping the rest. First skipped position: " + blockPos.toShortString());
		}

		if (!bl) {
			this.runUpdates();
		}
	}

	private void runUpdates() {
		try {
			while (!this.stack.isEmpty() || !this.addedThisLayer.isEmpty()) {
				for (int i = this.addedThisLayer.size() - 1; i >= 0; i--) {
					this.stack.push((CollectingNeighborUpdater.NeighborUpdates)this.addedThisLayer.get(i));
				}

				this.addedThisLayer.clear();
				CollectingNeighborUpdater.NeighborUpdates neighborUpdates = (CollectingNeighborUpdater.NeighborUpdates)this.stack.peek();

				while (this.addedThisLayer.isEmpty()) {
					if (!neighborUpdates.runNext(this.level)) {
						this.stack.pop();
						break;
					}
				}
			}
		} finally {
			this.stack.clear();
			this.addedThisLayer.clear();
			this.count = 0;
		}
	}

	static record FullNeighborUpdate(BlockState state, BlockPos pos, Block block, BlockPos neighborPos, boolean movedByPiston)
		implements CollectingNeighborUpdater.NeighborUpdates {
		@Override
		public boolean runNext(Level level) {
			NeighborUpdater.executeUpdate(level, this.state, this.pos, this.block, this.neighborPos, this.movedByPiston);
			return false;
		}
	}

	static final class MultiNeighborUpdate implements CollectingNeighborUpdater.NeighborUpdates {
		private final BlockPos sourcePos;
		private final Block sourceBlock;
		@Nullable
		private final Direction skipDirection;
		private int idx = 0;

		MultiNeighborUpdate(BlockPos blockPos, Block block, @Nullable Direction direction) {
			this.sourcePos = blockPos;
			this.sourceBlock = block;
			this.skipDirection = direction;
			if (NeighborUpdater.UPDATE_ORDER[this.idx] == direction) {
				this.idx++;
			}
		}

		@Override
		public boolean runNext(Level level) {
			BlockPos blockPos = this.sourcePos.relative(NeighborUpdater.UPDATE_ORDER[this.idx++]);
			BlockState blockState = level.getBlockState(blockPos);
			blockState.neighborChanged(level, blockPos, this.sourceBlock, this.sourcePos, false);
			if (this.idx < NeighborUpdater.UPDATE_ORDER.length && NeighborUpdater.UPDATE_ORDER[this.idx] == this.skipDirection) {
				this.idx++;
			}

			return this.idx < NeighborUpdater.UPDATE_ORDER.length;
		}
	}

	interface NeighborUpdates {
		boolean runNext(Level level);
	}

	static record ShapeUpdate(Direction direction, BlockState state, BlockPos pos, BlockPos neighborPos, int updateFlags)
		implements CollectingNeighborUpdater.NeighborUpdates {
		@Override
		public boolean runNext(Level level) {
			NeighborUpdater.executeShapeUpdate(level, this.direction, this.state, this.pos, this.neighborPos, this.updateFlags, 512);
			return false;
		}
	}

	static record SimpleNeighborUpdate(BlockPos pos, Block block, BlockPos neighborPos) implements CollectingNeighborUpdater.NeighborUpdates {
		@Override
		public boolean runNext(Level level) {
			BlockState blockState = level.getBlockState(this.pos);
			NeighborUpdater.executeUpdate(level, blockState, this.pos, this.block, this.neighborPos, false);
			return false;
		}
	}
}
