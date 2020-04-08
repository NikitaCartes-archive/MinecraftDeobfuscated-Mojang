/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class IronBarsBlock
extends CrossCollisionBlock {
    protected IronBarsBlock(BlockBehaviour.Properties properties) {
        super(1.0f, 1.0f, 16.0f, 16.0f, 16.0f, properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(NORTH, false)).setValue(EAST, false)).setValue(SOUTH, false)).setValue(WEST, false)).setValue(WATERLOGGED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Level blockGetter = blockPlaceContext.getLevel();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        BlockPos blockPos2 = blockPos.north();
        BlockPos blockPos3 = blockPos.south();
        BlockPos blockPos4 = blockPos.west();
        BlockPos blockPos5 = blockPos.east();
        BlockState blockState = blockGetter.getBlockState(blockPos2);
        BlockState blockState2 = blockGetter.getBlockState(blockPos3);
        BlockState blockState3 = blockGetter.getBlockState(blockPos4);
        BlockState blockState4 = blockGetter.getBlockState(blockPos5);
        return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(NORTH, this.attachsTo(blockState, blockState.isFaceSturdy(blockGetter, blockPos2, Direction.SOUTH)))).setValue(SOUTH, this.attachsTo(blockState2, blockState2.isFaceSturdy(blockGetter, blockPos3, Direction.NORTH)))).setValue(WEST, this.attachsTo(blockState3, blockState3.isFaceSturdy(blockGetter, blockPos4, Direction.EAST)))).setValue(EAST, this.attachsTo(blockState4, blockState4.isFaceSturdy(blockGetter, blockPos5, Direction.WEST)))).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        if (direction.getAxis().isHorizontal()) {
            return (BlockState)blockState.setValue((Property)PROPERTY_BY_DIRECTION.get(direction), this.attachsTo(blockState2, blockState2.isFaceSturdy(levelAccessor, blockPos2, direction.getOpposite())));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    public VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.empty();
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean skipRendering(BlockState blockState, BlockState blockState2, Direction direction) {
        if (blockState2.getBlock() == this) {
            if (!direction.getAxis().isHorizontal()) {
                return true;
            }
            if (((Boolean)blockState.getValue((Property)PROPERTY_BY_DIRECTION.get(direction))).booleanValue() && ((Boolean)blockState2.getValue((Property)PROPERTY_BY_DIRECTION.get(direction.getOpposite()))).booleanValue()) {
                return true;
            }
        }
        return super.skipRendering(blockState, blockState2, direction);
    }

    public final boolean attachsTo(BlockState blockState, boolean bl) {
        Block block = blockState.getBlock();
        return !IronBarsBlock.isExceptionForConnection(block) && bl || block instanceof IronBarsBlock || block.is(BlockTags.WALLS);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
    }
}

