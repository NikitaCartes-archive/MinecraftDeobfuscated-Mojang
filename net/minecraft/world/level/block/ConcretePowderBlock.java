/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ConcretePowderBlock
extends FallingBlock {
    private final BlockState concrete;

    public ConcretePowderBlock(Block block, BlockBehaviour.Properties properties) {
        super(properties);
        this.concrete = block.defaultBlockState();
    }

    @Override
    public void onLand(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2, FallingBlockEntity fallingBlockEntity) {
        if (ConcretePowderBlock.shouldSolidify(level, blockPos, blockState2)) {
            level.setBlock(blockPos, this.concrete, 3);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState;
        BlockPos blockPos;
        Level blockGetter = blockPlaceContext.getLevel();
        if (ConcretePowderBlock.shouldSolidify(blockGetter, blockPos = blockPlaceContext.getClickedPos(), blockState = blockGetter.getBlockState(blockPos))) {
            return this.concrete;
        }
        return super.getStateForPlacement(blockPlaceContext);
    }

    private static boolean shouldSolidify(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return ConcretePowderBlock.canSolidify(blockState) || ConcretePowderBlock.touchesLiquid(blockGetter, blockPos);
    }

    private static boolean touchesLiquid(BlockGetter blockGetter, BlockPos blockPos) {
        boolean bl = false;
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (Direction direction : Direction.values()) {
            BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
            if (direction == Direction.DOWN && !ConcretePowderBlock.canSolidify(blockState)) continue;
            mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
            blockState = blockGetter.getBlockState(mutableBlockPos);
            if (!ConcretePowderBlock.canSolidify(blockState) || blockState.isFaceSturdy(blockGetter, blockPos, direction.getOpposite())) continue;
            bl = true;
            break;
        }
        return bl;
    }

    private static boolean canSolidify(BlockState blockState) {
        return blockState.getFluidState().is(FluidTags.WATER);
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (ConcretePowderBlock.touchesLiquid(levelAccessor, blockPos)) {
            return this.concrete;
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    public int getDustColor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return blockState.getMapColor((BlockGetter)blockGetter, (BlockPos)blockPos).col;
    }
}

