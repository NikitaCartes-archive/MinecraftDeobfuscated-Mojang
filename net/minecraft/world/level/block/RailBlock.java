/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RailState;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;

public class RailBlock
extends BaseRailBlock {
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE;

    protected RailBlock(BlockBehaviour.Properties properties) {
        super(false, properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(SHAPE, RailShape.NORTH_SOUTH));
    }

    @Override
    protected void updateState(BlockState blockState, Level level, BlockPos blockPos, Block block) {
        if (block.defaultBlockState().isSignalSource() && new RailState(level, blockPos, blockState).countPotentialConnections() == 3) {
            this.updateDir(level, blockPos, blockState, false);
        }
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180: {
                switch (blockState.getValue(SHAPE)) {
                    case ASCENDING_EAST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.ASCENDING_WEST);
                    }
                    case ASCENDING_WEST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.ASCENDING_EAST);
                    }
                    case ASCENDING_NORTH: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                    }
                    case ASCENDING_SOUTH: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                    }
                    case SOUTH_EAST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.NORTH_WEST);
                    }
                    case SOUTH_WEST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.NORTH_EAST);
                    }
                    case NORTH_WEST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.SOUTH_EAST);
                    }
                    case NORTH_EAST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.SOUTH_WEST);
                    }
                }
            }
            case COUNTERCLOCKWISE_90: {
                switch (blockState.getValue(SHAPE)) {
                    case NORTH_SOUTH: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.EAST_WEST);
                    }
                    case EAST_WEST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.NORTH_SOUTH);
                    }
                    case ASCENDING_EAST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                    }
                    case ASCENDING_WEST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                    }
                    case ASCENDING_NORTH: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.ASCENDING_WEST);
                    }
                    case ASCENDING_SOUTH: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.ASCENDING_EAST);
                    }
                    case SOUTH_EAST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.NORTH_EAST);
                    }
                    case SOUTH_WEST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.SOUTH_EAST);
                    }
                    case NORTH_WEST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.SOUTH_WEST);
                    }
                    case NORTH_EAST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.NORTH_WEST);
                    }
                }
            }
            case CLOCKWISE_90: {
                switch (blockState.getValue(SHAPE)) {
                    case NORTH_SOUTH: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.EAST_WEST);
                    }
                    case EAST_WEST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.NORTH_SOUTH);
                    }
                    case ASCENDING_EAST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                    }
                    case ASCENDING_WEST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                    }
                    case ASCENDING_NORTH: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.ASCENDING_EAST);
                    }
                    case ASCENDING_SOUTH: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.ASCENDING_WEST);
                    }
                    case SOUTH_EAST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.SOUTH_WEST);
                    }
                    case SOUTH_WEST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.NORTH_WEST);
                    }
                    case NORTH_WEST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.NORTH_EAST);
                    }
                    case NORTH_EAST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.SOUTH_EAST);
                    }
                }
            }
        }
        return blockState;
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        RailShape railShape = blockState.getValue(SHAPE);
        switch (mirror) {
            case LEFT_RIGHT: {
                switch (railShape) {
                    case ASCENDING_NORTH: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                    }
                    case ASCENDING_SOUTH: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                    }
                    case SOUTH_EAST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.NORTH_EAST);
                    }
                    case SOUTH_WEST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.NORTH_WEST);
                    }
                    case NORTH_WEST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.SOUTH_WEST);
                    }
                    case NORTH_EAST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.SOUTH_EAST);
                    }
                }
                break;
            }
            case FRONT_BACK: {
                switch (railShape) {
                    case ASCENDING_EAST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.ASCENDING_WEST);
                    }
                    case ASCENDING_WEST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.ASCENDING_EAST);
                    }
                    case SOUTH_EAST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.SOUTH_WEST);
                    }
                    case SOUTH_WEST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.SOUTH_EAST);
                    }
                    case NORTH_WEST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.NORTH_EAST);
                    }
                    case NORTH_EAST: {
                        return (BlockState)blockState.setValue(SHAPE, RailShape.NORTH_WEST);
                    }
                }
                break;
            }
        }
        return super.mirror(blockState, mirror);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE);
    }
}

