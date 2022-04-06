/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class RepeaterBlock
extends DiodeBlock {
    public static final BooleanProperty LOCKED = BlockStateProperties.LOCKED;
    public static final IntegerProperty DELAY = BlockStateProperties.DELAY;

    protected RepeaterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(DELAY, 1)).setValue(LOCKED, false)).setValue(POWERED, false));
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        }
        level.setBlock(blockPos, (BlockState)blockState.cycle(DELAY), 3);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected int getDelay(BlockState blockState) {
        return blockState.getValue(DELAY) * 2;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = super.getStateForPlacement(blockPlaceContext);
        return (BlockState)blockState.setValue(LOCKED, this.isLocked(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), blockState));
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (!levelAccessor.isClientSide() && direction.getAxis() != blockState.getValue(FACING).getAxis()) {
            return (BlockState)blockState.setValue(LOCKED, this.isLocked(levelAccessor, blockPos, blockState));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    public boolean isLocked(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return this.getAlternateSignal(levelReader, blockPos, blockState) > 0;
    }

    @Override
    protected boolean isAlternateInput(BlockState blockState) {
        return RepeaterBlock.isDiode(blockState);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (!blockState.getValue(POWERED).booleanValue()) {
            return;
        }
        Direction direction = blockState.getValue(FACING);
        double d = (double)blockPos.getX() + 0.5 + (randomSource.nextDouble() - 0.5) * 0.2;
        double e = (double)blockPos.getY() + 0.4 + (randomSource.nextDouble() - 0.5) * 0.2;
        double f = (double)blockPos.getZ() + 0.5 + (randomSource.nextDouble() - 0.5) * 0.2;
        float g = -5.0f;
        if (randomSource.nextBoolean()) {
            g = blockState.getValue(DELAY) * 2 - 1;
        }
        double h = (g /= 16.0f) * (float)direction.getStepX();
        double i = g * (float)direction.getStepZ();
        level.addParticle(DustParticleOptions.REDSTONE, d + h, e, f + i, 0.0, 0.0, 0.0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, DELAY, LOCKED, POWERED);
    }
}

