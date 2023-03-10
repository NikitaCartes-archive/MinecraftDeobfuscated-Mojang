/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public class CoralBlock
extends Block {
    private final Block deadBlock;

    public CoralBlock(Block block, BlockBehaviour.Properties properties) {
        super(properties);
        this.deadBlock = block;
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (!this.scanForWater(serverLevel, blockPos)) {
            serverLevel.setBlock(blockPos, this.deadBlock.defaultBlockState(), 2);
        }
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (!this.scanForWater(levelAccessor, blockPos)) {
            levelAccessor.scheduleTick(blockPos, this, 60 + levelAccessor.getRandom().nextInt(40));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    protected boolean scanForWater(BlockGetter blockGetter, BlockPos blockPos) {
        for (Direction direction : Direction.values()) {
            FluidState fluidState = blockGetter.getFluidState(blockPos.relative(direction));
            if (!fluidState.is(FluidTags.WATER)) continue;
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        if (!this.scanForWater(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos())) {
            blockPlaceContext.getLevel().scheduleTick(blockPlaceContext.getClickedPos(), this, 60 + blockPlaceContext.getLevel().getRandom().nextInt(40));
        }
        return this.defaultBlockState();
    }
}

