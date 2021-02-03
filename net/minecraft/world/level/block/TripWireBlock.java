/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TripWireBlock
extends Block {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    public static final BooleanProperty DISARMED = BlockStateProperties.DISARMED;
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = CrossCollisionBlock.PROPERTY_BY_DIRECTION;
    protected static final VoxelShape AABB = Block.box(0.0, 1.0, 0.0, 16.0, 2.5, 16.0);
    protected static final VoxelShape NOT_ATTACHED_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    private final TripWireHookBlock hook;

    public TripWireBlock(TripWireHookBlock tripWireHookBlock, BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(POWERED, false)).setValue(ATTACHED, false)).setValue(DISARMED, false)).setValue(NORTH, false)).setValue(EAST, false)).setValue(SOUTH, false)).setValue(WEST, false));
        this.hook = tripWireHookBlock;
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return blockState.getValue(ATTACHED) != false ? AABB : NOT_ATTACHED_AABB;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Level blockGetter = blockPlaceContext.getLevel();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        return (BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(NORTH, this.shouldConnectTo(blockGetter.getBlockState(blockPos.north()), Direction.NORTH))).setValue(EAST, this.shouldConnectTo(blockGetter.getBlockState(blockPos.east()), Direction.EAST))).setValue(SOUTH, this.shouldConnectTo(blockGetter.getBlockState(blockPos.south()), Direction.SOUTH))).setValue(WEST, this.shouldConnectTo(blockGetter.getBlockState(blockPos.west()), Direction.WEST));
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction.getAxis().isHorizontal()) {
            return (BlockState)blockState.setValue(PROPERTY_BY_DIRECTION.get(direction), this.shouldConnectTo(blockState2, direction));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState2.is(blockState.getBlock())) {
            return;
        }
        this.updateSource(level, blockPos, blockState);
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (bl || blockState.is(blockState2.getBlock())) {
            return;
        }
        this.updateSource(level, blockPos, (BlockState)blockState.setValue(POWERED, true));
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if (!level.isClientSide && !player.getMainHandItem().isEmpty() && player.getMainHandItem().is(Items.SHEARS)) {
            level.setBlock(blockPos, (BlockState)blockState.setValue(DISARMED, true), 4);
            level.gameEvent((Entity)player, GameEvent.SHEAR, blockPos);
        }
        super.playerWillDestroy(level, blockPos, blockState, player);
    }

    private void updateSource(Level level, BlockPos blockPos, BlockState blockState) {
        block0: for (Direction direction : new Direction[]{Direction.SOUTH, Direction.WEST}) {
            for (int i = 1; i < 42; ++i) {
                BlockPos blockPos2 = blockPos.relative(direction, i);
                BlockState blockState2 = level.getBlockState(blockPos2);
                if (blockState2.is(this.hook)) {
                    if (blockState2.getValue(TripWireHookBlock.FACING) != direction.getOpposite()) continue block0;
                    this.hook.calculateState(level, blockPos2, blockState2, false, true, i, blockState);
                    continue block0;
                }
                if (!blockState2.is(this)) continue block0;
            }
        }
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (level.isClientSide) {
            return;
        }
        if (blockState.getValue(POWERED).booleanValue()) {
            return;
        }
        this.checkPressed(level, blockPos);
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (!serverLevel.getBlockState(blockPos).getValue(POWERED).booleanValue()) {
            return;
        }
        this.checkPressed(serverLevel, blockPos);
    }

    private void checkPressed(Level level, BlockPos blockPos) {
        BlockState blockState = level.getBlockState(blockPos);
        boolean bl = blockState.getValue(POWERED);
        boolean bl2 = false;
        List<Entity> list = level.getEntities(null, blockState.getShape(level, blockPos).bounds().move(blockPos));
        if (!list.isEmpty()) {
            for (Entity entity : list) {
                if (entity.isIgnoringBlockTriggers()) continue;
                bl2 = true;
                break;
            }
        }
        if (bl2 != bl) {
            blockState = (BlockState)blockState.setValue(POWERED, bl2);
            level.setBlock(blockPos, blockState, 3);
            this.updateSource(level, blockPos, blockState);
        }
        if (bl2) {
            level.getBlockTicks().scheduleTick(new BlockPos(blockPos), this, 10);
        }
    }

    public boolean shouldConnectTo(BlockState blockState, Direction direction) {
        if (blockState.is(this.hook)) {
            return blockState.getValue(TripWireHookBlock.FACING) == direction.getOpposite();
        }
        return blockState.is(this);
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180: {
                return (BlockState)((BlockState)((BlockState)((BlockState)blockState.setValue(NORTH, blockState.getValue(SOUTH))).setValue(EAST, blockState.getValue(WEST))).setValue(SOUTH, blockState.getValue(NORTH))).setValue(WEST, blockState.getValue(EAST));
            }
            case COUNTERCLOCKWISE_90: {
                return (BlockState)((BlockState)((BlockState)((BlockState)blockState.setValue(NORTH, blockState.getValue(EAST))).setValue(EAST, blockState.getValue(SOUTH))).setValue(SOUTH, blockState.getValue(WEST))).setValue(WEST, blockState.getValue(NORTH));
            }
            case CLOCKWISE_90: {
                return (BlockState)((BlockState)((BlockState)((BlockState)blockState.setValue(NORTH, blockState.getValue(WEST))).setValue(EAST, blockState.getValue(NORTH))).setValue(SOUTH, blockState.getValue(EAST))).setValue(WEST, blockState.getValue(SOUTH));
            }
        }
        return blockState;
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT: {
                return (BlockState)((BlockState)blockState.setValue(NORTH, blockState.getValue(SOUTH))).setValue(SOUTH, blockState.getValue(NORTH));
            }
            case FRONT_BACK: {
                return (BlockState)((BlockState)blockState.setValue(EAST, blockState.getValue(WEST))).setValue(WEST, blockState.getValue(EAST));
            }
        }
        return super.mirror(blockState, mirror);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, ATTACHED, DISARMED, NORTH, EAST, WEST, SOUTH);
    }
}

