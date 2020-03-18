/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SeaPickleBlock
extends BushBlock
implements BonemealableBlock,
SimpleWaterloggedBlock {
    public static final IntegerProperty PICKLES = BlockStateProperties.PICKLES;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape ONE_AABB = Block.box(6.0, 0.0, 6.0, 10.0, 6.0, 10.0);
    protected static final VoxelShape TWO_AABB = Block.box(3.0, 0.0, 3.0, 13.0, 6.0, 13.0);
    protected static final VoxelShape THREE_AABB = Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0);
    protected static final VoxelShape FOUR_AABB = Block.box(2.0, 0.0, 2.0, 14.0, 7.0, 14.0);

    protected SeaPickleBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(PICKLES, 1)).setValue(WATERLOGGED, true));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos());
        if (blockState.getBlock() == this) {
            return (BlockState)blockState.setValue(PICKLES, Math.min(4, blockState.getValue(PICKLES) + 1));
        }
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        boolean bl = fluidState.is(FluidTags.WATER) && fluidState.getAmount() == 8;
        return (BlockState)super.getStateForPlacement(blockPlaceContext).setValue(WATERLOGGED, bl);
    }

    public static boolean isDead(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) == false;
    }

    @Override
    protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return !blockState.getCollisionShape(blockGetter, blockPos).getFaceShape(Direction.UP).isEmpty();
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.below();
        return this.mayPlaceOn(levelReader.getBlockState(blockPos2), levelReader, blockPos2);
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (!blockState.canSurvive(levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        if (blockPlaceContext.getItemInHand().getItem() == this.asItem() && blockState.getValue(PICKLES) < 4) {
            return true;
        }
        return super.canBeReplaced(blockState, blockPlaceContext);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        switch (blockState.getValue(PICKLES)) {
            default: {
                return ONE_AABB;
            }
            case 2: {
                return TWO_AABB;
            }
            case 3: {
                return THREE_AABB;
            }
            case 4: 
        }
        return FOUR_AABB;
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PICKLES, WATERLOGGED);
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
        if (!SeaPickleBlock.isDead(blockState) && serverLevel.getBlockState(blockPos.below()).is(BlockTags.CORAL_BLOCKS)) {
            int i = 5;
            int j = 1;
            int k = 2;
            int l = 0;
            int m = blockPos.getX() - 2;
            int n = 0;
            for (int o = 0; o < 5; ++o) {
                for (int p = 0; p < j; ++p) {
                    int q = 2 + blockPos.getY() - 1;
                    for (int r = q - 2; r < q; ++r) {
                        BlockState blockState2;
                        BlockPos blockPos2 = new BlockPos(m + o, r, blockPos.getZ() - n + p);
                        if (blockPos2 == blockPos || random.nextInt(6) != 0 || serverLevel.getBlockState(blockPos2).getBlock() != Blocks.WATER || !(blockState2 = serverLevel.getBlockState(blockPos2.below())).is(BlockTags.CORAL_BLOCKS)) continue;
                        serverLevel.setBlock(blockPos2, (BlockState)Blocks.SEA_PICKLE.defaultBlockState().setValue(PICKLES, random.nextInt(4) + 1), 3);
                    }
                }
                if (l < 2) {
                    j += 2;
                    ++n;
                } else {
                    j -= 2;
                    --n;
                }
                ++l;
            }
            serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(PICKLES, 4), 2);
        }
    }
}

