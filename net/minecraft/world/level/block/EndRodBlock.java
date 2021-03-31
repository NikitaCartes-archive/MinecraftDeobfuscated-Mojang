/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RodBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.PushReaction;

public class EndRodBlock
extends RodBlock {
    protected EndRodBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.UP));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction direction = blockPlaceContext.getClickedFace();
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().relative(direction.getOpposite()));
        if (blockState.is(this) && blockState.getValue(FACING) == direction) {
            return (BlockState)this.defaultBlockState().setValue(FACING, direction.getOpposite());
        }
        return (BlockState)this.defaultBlockState().setValue(FACING, direction);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        Direction direction = blockState.getValue(FACING);
        double d = (double)blockPos.getX() + 0.55 - (double)(random.nextFloat() * 0.1f);
        double e = (double)blockPos.getY() + 0.55 - (double)(random.nextFloat() * 0.1f);
        double f = (double)blockPos.getZ() + 0.55 - (double)(random.nextFloat() * 0.1f);
        double g = 0.4f - (random.nextFloat() + random.nextFloat()) * 0.4f;
        if (random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.END_ROD, d + (double)direction.getStepX() * g, e + (double)direction.getStepY() * g, f + (double)direction.getStepZ() * g, random.nextGaussian() * 0.005, random.nextGaussian() * 0.005, random.nextGaussian() * 0.005);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState blockState) {
        return PushReaction.NORMAL;
    }
}

