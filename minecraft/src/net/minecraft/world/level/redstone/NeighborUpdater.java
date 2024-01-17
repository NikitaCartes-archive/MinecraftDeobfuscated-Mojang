package net.minecraft.world.level.redstone;

import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface NeighborUpdater {
	Direction[] UPDATE_ORDER = new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};

	void shapeUpdate(Direction direction, BlockState blockState, BlockPos blockPos, BlockPos blockPos2, int i, int j);

	void neighborChanged(BlockPos blockPos, Block block, BlockPos blockPos2);

	void neighborChanged(BlockState blockState, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl);

	default void updateNeighborsAtExceptFromFacing(BlockPos blockPos, Block block, @Nullable Direction direction) {
		for (Direction direction2 : UPDATE_ORDER) {
			if (direction2 != direction) {
				this.neighborChanged(blockPos.relative(direction2), block, blockPos);
			}
		}
	}

	static void executeShapeUpdate(LevelAccessor levelAccessor, Direction direction, BlockState blockState, BlockPos blockPos, BlockPos blockPos2, int i, int j) {
		BlockState blockState2 = levelAccessor.getBlockState(blockPos);
		BlockState blockState3 = blockState2.updateShape(direction, blockState, levelAccessor, blockPos, blockPos2);
		Block.updateOrDestroy(blockState2, blockState3, levelAccessor, blockPos, i, j);
	}

	static void executeUpdate(Level level, BlockState blockState, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		try {
			blockState.handleNeighborChanged(level, blockPos, block, blockPos2, bl);
		} catch (Throwable var9) {
			CrashReport crashReport = CrashReport.forThrowable(var9, "Exception while updating neighbours");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Block being updated");
			crashReportCategory.setDetail(
				"Source block type",
				(CrashReportDetail<String>)(() -> {
					try {
						return String.format(
							Locale.ROOT, "ID #%s (%s // %s)", BuiltInRegistries.BLOCK.getKey(block), block.getDescriptionId(), block.getClass().getCanonicalName()
						);
					} catch (Throwable var2) {
						return "ID #" + BuiltInRegistries.BLOCK.getKey(block);
					}
				})
			);
			CrashReportCategory.populateBlockDetails(crashReportCategory, level, blockPos, blockState);
			throw new ReportedException(crashReport);
		}
	}
}
