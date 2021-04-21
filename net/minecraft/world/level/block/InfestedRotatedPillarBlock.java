/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class InfestedRotatedPillarBlock
extends InfestedBlock {
    public InfestedRotatedPillarBlock(Block block, BlockBehaviour.Properties properties) {
        super(block, properties);
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return RotatedPillarBlock.rotatePillar(blockState, rotation);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RotatedPillarBlock.AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState)this.defaultBlockState().setValue(RotatedPillarBlock.AXIS, blockPlaceContext.getClickedFace().getAxis());
    }
}

