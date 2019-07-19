/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BasePressurePlateBlock
extends Block {
    protected static final VoxelShape PRESSED_AABB = Block.box(1.0, 0.0, 1.0, 15.0, 0.5, 15.0);
    protected static final VoxelShape AABB = Block.box(1.0, 0.0, 1.0, 15.0, 1.0, 15.0);
    protected static final AABB TOUCH_AABB = new AABB(0.125, 0.0, 0.125, 0.875, 0.25, 0.875);

    protected BasePressurePlateBlock(Block.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.getSignalForState(blockState) > 0 ? PRESSED_AABB : AABB;
    }

    @Override
    public int getTickDelay(LevelReader levelReader) {
        return 20;
    }

    @Override
    public boolean isPossibleToRespawnInThis() {
        return true;
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction == Direction.DOWN && !blockState.canSurvive(levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.below();
        return BasePressurePlateBlock.canSupportRigidBlock(levelReader, blockPos2) || BasePressurePlateBlock.canSupportCenter(levelReader, blockPos2, Direction.UP);
    }

    @Override
    public void tick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if (level.isClientSide) {
            return;
        }
        int i = this.getSignalForState(blockState);
        if (i > 0) {
            this.checkPressed(level, blockPos, blockState, i);
        }
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (level.isClientSide) {
            return;
        }
        int i = this.getSignalForState(blockState);
        if (i == 0) {
            this.checkPressed(level, blockPos, blockState, i);
        }
    }

    protected void checkPressed(Level level, BlockPos blockPos, BlockState blockState, int i) {
        boolean bl2;
        int j = this.getSignalStrength(level, blockPos);
        boolean bl = i > 0;
        boolean bl3 = bl2 = j > 0;
        if (i != j) {
            BlockState blockState2 = this.setSignalForState(blockState, j);
            level.setBlock(blockPos, blockState2, 2);
            this.updateNeighbours(level, blockPos);
            level.setBlocksDirty(blockPos, blockState, blockState2);
        }
        if (!bl2 && bl) {
            this.playOffSound(level, blockPos);
        } else if (bl2 && !bl) {
            this.playOnSound(level, blockPos);
        }
        if (bl2) {
            level.getBlockTicks().scheduleTick(new BlockPos(blockPos), this, this.getTickDelay(level));
        }
    }

    protected abstract void playOnSound(LevelAccessor var1, BlockPos var2);

    protected abstract void playOffSound(LevelAccessor var1, BlockPos var2);

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (bl || blockState.getBlock() == blockState2.getBlock()) {
            return;
        }
        if (this.getSignalForState(blockState) > 0) {
            this.updateNeighbours(level, blockPos);
        }
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    protected void updateNeighbours(Level level, BlockPos blockPos) {
        level.updateNeighborsAt(blockPos, this);
        level.updateNeighborsAt(blockPos.below(), this);
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return this.getSignalForState(blockState);
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (direction == Direction.UP) {
            return this.getSignalForState(blockState);
        }
        return 0;
    }

    @Override
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState blockState) {
        return PushReaction.DESTROY;
    }

    protected abstract int getSignalStrength(Level var1, BlockPos var2);

    protected abstract int getSignalForState(BlockState var1);

    protected abstract BlockState setSignalForState(BlockState var1, int var2);
}

