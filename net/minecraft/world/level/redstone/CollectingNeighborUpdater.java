/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.redstone;

import com.mojang.logging.LogUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.NeighborUpdater;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class CollectingNeighborUpdater
implements NeighborUpdater {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Level level;
    private final int maxChainedNeighborUpdates;
    private final ArrayDeque<NeighborUpdates> stack = new ArrayDeque();
    private final List<NeighborUpdates> addedThisLayer = new ArrayList<NeighborUpdates>();
    private int count = 0;

    public CollectingNeighborUpdater(Level level, int i) {
        this.level = level;
        this.maxChainedNeighborUpdates = i;
    }

    @Override
    public void shapeUpdate(Direction direction, BlockState blockState, BlockPos blockPos, BlockPos blockPos2, int i, int j) {
        this.addAndRun(blockPos, new ShapeUpdate(direction, blockState, blockPos.immutable(), blockPos2.immutable(), i));
    }

    @Override
    public void neighborChanged(BlockPos blockPos, Block block, BlockPos blockPos2) {
        this.addAndRun(blockPos, new SimpleNeighborUpdate(blockPos, block, blockPos2.immutable()));
    }

    @Override
    public void neighborChanged(BlockState blockState, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        this.addAndRun(blockPos, new FullNeighborUpdate(blockState, blockPos.immutable(), block, blockPos2.immutable(), bl));
    }

    @Override
    public void updateNeighborsAtExceptFromFacing(BlockPos blockPos, Block block, @Nullable Direction direction) {
        this.addAndRun(blockPos, new MultiNeighborUpdate(blockPos.immutable(), block, direction));
    }

    private void addAndRun(BlockPos blockPos, NeighborUpdates neighborUpdates) {
        boolean bl = this.count > 0;
        boolean bl2 = this.maxChainedNeighborUpdates >= 0 && this.count >= this.maxChainedNeighborUpdates;
        ++this.count;
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
            block3: while (!this.stack.isEmpty() || !this.addedThisLayer.isEmpty()) {
                for (int i = this.addedThisLayer.size() - 1; i >= 0; --i) {
                    this.stack.push(this.addedThisLayer.get(i));
                }
                this.addedThisLayer.clear();
                NeighborUpdates neighborUpdates = this.stack.peek();
                while (this.addedThisLayer.isEmpty()) {
                    if (neighborUpdates.runNext(this.level)) continue;
                    this.stack.pop();
                    continue block3;
                }
            }
        } finally {
            this.stack.clear();
            this.addedThisLayer.clear();
            this.count = 0;
        }
    }

    record ShapeUpdate(Direction direction, BlockState state, BlockPos pos, BlockPos neighborPos, int updateFlags) implements NeighborUpdates
    {
        @Override
        public boolean runNext(Level level) {
            NeighborUpdater.executeShapeUpdate(level, this.direction, this.state, this.pos, this.neighborPos, this.updateFlags, 512);
            return false;
        }
    }

    static interface NeighborUpdates {
        public boolean runNext(Level var1);
    }

    record SimpleNeighborUpdate(BlockPos pos, Block block, BlockPos neighborPos) implements NeighborUpdates
    {
        @Override
        public boolean runNext(Level level) {
            BlockState blockState = level.getBlockState(this.pos);
            NeighborUpdater.executeUpdate(level, blockState, this.pos, this.block, this.neighborPos, false);
            return false;
        }
    }

    record FullNeighborUpdate(BlockState state, BlockPos pos, Block block, BlockPos neighborPos, boolean movedByPiston) implements NeighborUpdates
    {
        @Override
        public boolean runNext(Level level) {
            NeighborUpdater.executeUpdate(level, this.state, this.pos, this.block, this.neighborPos, this.movedByPiston);
            return false;
        }
    }

    static final class MultiNeighborUpdate
    implements NeighborUpdates {
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
                ++this.idx;
            }
        }

        @Override
        public boolean runNext(Level level) {
            BlockPos blockPos = this.sourcePos.relative(NeighborUpdater.UPDATE_ORDER[this.idx++]);
            BlockState blockState = level.getBlockState(blockPos);
            blockState.neighborChanged(level, blockPos, this.sourceBlock, this.sourcePos, false);
            if (this.idx < NeighborUpdater.UPDATE_ORDER.length && NeighborUpdater.UPDATE_ORDER[this.idx] == this.skipDirection) {
                ++this.idx;
            }
            return this.idx < NeighborUpdater.UPDATE_ORDER.length;
        }
    }
}

