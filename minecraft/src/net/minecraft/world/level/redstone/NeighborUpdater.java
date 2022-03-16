package net.minecraft.world.level.redstone;

import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface NeighborUpdater {
	Direction[] UPDATE_ORDER = new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};
	NeighborUpdater NOOP = new NeighborUpdater() {
		@Override
		public void neighborChanged(BlockPos blockPos, Block block, BlockPos blockPos2) {
		}

		@Override
		public void neighborChanged(BlockState blockState, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		}

		@Override
		public void updateNeighborsAtExceptFromFacing(BlockPos blockPos, Block block, @Nullable Direction direction) {
		}
	};

	void neighborChanged(BlockPos blockPos, Block block, BlockPos blockPos2);

	void neighborChanged(BlockState blockState, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl);

	default void updateNeighborsAtExceptFromFacing(BlockPos blockPos, Block block, @Nullable Direction direction) {
		for (Direction direction2 : UPDATE_ORDER) {
			if (direction2 != direction) {
				this.neighborChanged(blockPos.relative(direction2), block, blockPos);
			}
		}
	}

	static void executeUpdate(ServerLevel serverLevel, BlockState blockState, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		try {
			blockState.neighborChanged(serverLevel, blockPos, block, blockPos2, bl);
		} catch (Throwable var9) {
			CrashReport crashReport = CrashReport.forThrowable(var9, "Exception while updating neighbours");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Block being updated");
			crashReportCategory.setDetail("Source block type", (CrashReportDetail<String>)(() -> {
				try {
					return String.format("ID #%s (%s // %s)", Registry.BLOCK.getKey(block), block.getDescriptionId(), block.getClass().getCanonicalName());
				} catch (Throwable var2) {
					return "ID #" + Registry.BLOCK.getKey(block);
				}
			}));
			CrashReportCategory.populateBlockDetails(crashReportCategory, serverLevel, blockPos, blockState);
			throw new ReportedException(crashReport);
		}
	}
}
