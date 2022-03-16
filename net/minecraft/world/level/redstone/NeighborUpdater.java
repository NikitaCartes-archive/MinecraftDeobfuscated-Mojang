/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.redstone;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface NeighborUpdater {
    public static final Direction[] UPDATE_ORDER = new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};
    public static final NeighborUpdater NOOP = new NeighborUpdater(){

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

    public void neighborChanged(BlockPos var1, Block var2, BlockPos var3);

    public void neighborChanged(BlockState var1, BlockPos var2, Block var3, BlockPos var4, boolean var5);

    default public void updateNeighborsAtExceptFromFacing(BlockPos blockPos, Block block, @Nullable Direction direction) {
        for (Direction direction2 : UPDATE_ORDER) {
            if (direction2 == direction) continue;
            this.neighborChanged(blockPos.relative(direction2), block, blockPos);
        }
    }

    public static void executeUpdate(ServerLevel serverLevel, BlockState blockState, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        try {
            blockState.neighborChanged(serverLevel, blockPos, block, blockPos2, bl);
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Exception while updating neighbours");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Block being updated");
            crashReportCategory.setDetail("Source block type", () -> {
                try {
                    return String.format("ID #%s (%s // %s)", Registry.BLOCK.getKey(block), block.getDescriptionId(), block.getClass().getCanonicalName());
                } catch (Throwable throwable) {
                    return "ID #" + Registry.BLOCK.getKey(block);
                }
            });
            CrashReportCategory.populateBlockDetails(crashReportCategory, serverLevel, blockPos, blockState);
            throw new ReportedException(crashReport);
        }
    }
}

