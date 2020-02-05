/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RailState;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class DetectorRailBlock
extends BaseRailBlock {
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public DetectorRailBlock(Block.Properties properties) {
        super(true, properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(POWERED, false)).setValue(SHAPE, RailShape.NORTH_SOUTH));
    }

    @Override
    public int getTickDelay(LevelReader levelReader) {
        return 20;
    }

    @Override
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (level.isClientSide) {
            return;
        }
        if (blockState.getValue(POWERED).booleanValue()) {
            return;
        }
        this.checkPressed(level, blockPos, blockState);
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (!blockState.getValue(POWERED).booleanValue()) {
            return;
        }
        this.checkPressed(serverLevel, blockPos, blockState);
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockState.getValue(POWERED) != false ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (!blockState.getValue(POWERED).booleanValue()) {
            return 0;
        }
        return direction == Direction.UP ? 15 : 0;
    }

    private void checkPressed(Level level, BlockPos blockPos, BlockState blockState) {
        BlockState blockState2;
        boolean bl = blockState.getValue(POWERED);
        boolean bl2 = false;
        List<AbstractMinecart> list = this.getInteractingMinecartOfType(level, blockPos, AbstractMinecart.class, null);
        if (!list.isEmpty()) {
            bl2 = true;
        }
        if (bl2 && !bl) {
            blockState2 = (BlockState)blockState.setValue(POWERED, true);
            level.setBlock(blockPos, blockState2, 3);
            this.updatePowerToConnected(level, blockPos, blockState2, true);
            level.updateNeighborsAt(blockPos, this);
            level.updateNeighborsAt(blockPos.below(), this);
            level.setBlocksDirty(blockPos, blockState, blockState2);
        }
        if (!bl2 && bl) {
            blockState2 = (BlockState)blockState.setValue(POWERED, false);
            level.setBlock(blockPos, blockState2, 3);
            this.updatePowerToConnected(level, blockPos, blockState2, false);
            level.updateNeighborsAt(blockPos, this);
            level.updateNeighborsAt(blockPos.below(), this);
            level.setBlocksDirty(blockPos, blockState, blockState2);
        }
        if (bl2) {
            level.getBlockTicks().scheduleTick(blockPos, this, this.getTickDelay(level));
        }
        level.updateNeighbourForOutputSignal(blockPos, this);
    }

    protected void updatePowerToConnected(Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
        RailState railState = new RailState(level, blockPos, blockState);
        List<BlockPos> list = railState.getConnections();
        for (BlockPos blockPos2 : list) {
            BlockState blockState2 = level.getBlockState(blockPos2);
            blockState2.neighborChanged(level, blockPos2, blockState2.getBlock(), blockPos, false);
        }
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState2.getBlock() == blockState.getBlock()) {
            return;
        }
        this.checkPressed(level, blockPos, this.updateState(blockState, level, blockPos, bl));
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        if (blockState.getValue(POWERED).booleanValue()) {
            List<MinecartCommandBlock> list = this.getInteractingMinecartOfType(level, blockPos, MinecartCommandBlock.class, null);
            if (!list.isEmpty()) {
                return list.get(0).getCommandBlock().getSuccessCount();
            }
            List<AbstractMinecart> list2 = this.getInteractingMinecartOfType(level, blockPos, AbstractMinecart.class, EntitySelector.CONTAINER_ENTITY_SELECTOR);
            if (!list2.isEmpty()) {
                return AbstractContainerMenu.getRedstoneSignalFromContainer((Container)((Object)list2.get(0)));
            }
        }
        return 0;
    }

    protected <T extends AbstractMinecart> List<T> getInteractingMinecartOfType(Level level, BlockPos blockPos, Class<T> class_, @Nullable Predicate<Entity> predicate) {
        return level.getEntitiesOfClass(class_, this.getSearchBB(blockPos), predicate);
    }

    private AABB getSearchBB(BlockPos blockPos) {
        float f = 0.2f;
        return new AABB((float)blockPos.getX() + 0.2f, blockPos.getY(), (float)blockPos.getZ() + 0.2f, (float)(blockPos.getX() + 1) - 0.2f, (float)(blockPos.getY() + 1) - 0.2f, (float)(blockPos.getZ() + 1) - 0.2f);
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
        builder.add(SHAPE, POWERED);
    }
}

