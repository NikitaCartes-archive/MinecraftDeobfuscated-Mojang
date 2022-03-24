/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FenceGateBlock
extends HorizontalDirectionalBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty IN_WALL = BlockStateProperties.IN_WALL;
    protected static final VoxelShape Z_SHAPE = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    protected static final VoxelShape X_SHAPE = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);
    protected static final VoxelShape Z_SHAPE_LOW = Block.box(0.0, 0.0, 6.0, 16.0, 13.0, 10.0);
    protected static final VoxelShape X_SHAPE_LOW = Block.box(6.0, 0.0, 0.0, 10.0, 13.0, 16.0);
    protected static final VoxelShape Z_COLLISION_SHAPE = Block.box(0.0, 0.0, 6.0, 16.0, 24.0, 10.0);
    protected static final VoxelShape X_COLLISION_SHAPE = Block.box(6.0, 0.0, 0.0, 10.0, 24.0, 16.0);
    protected static final VoxelShape Z_OCCLUSION_SHAPE = Shapes.or(Block.box(0.0, 5.0, 7.0, 2.0, 16.0, 9.0), Block.box(14.0, 5.0, 7.0, 16.0, 16.0, 9.0));
    protected static final VoxelShape X_OCCLUSION_SHAPE = Shapes.or(Block.box(7.0, 5.0, 0.0, 9.0, 16.0, 2.0), Block.box(7.0, 5.0, 14.0, 9.0, 16.0, 16.0));
    protected static final VoxelShape Z_OCCLUSION_SHAPE_LOW = Shapes.or(Block.box(0.0, 2.0, 7.0, 2.0, 13.0, 9.0), Block.box(14.0, 2.0, 7.0, 16.0, 13.0, 9.0));
    protected static final VoxelShape X_OCCLUSION_SHAPE_LOW = Shapes.or(Block.box(7.0, 2.0, 0.0, 9.0, 13.0, 2.0), Block.box(7.0, 2.0, 14.0, 9.0, 13.0, 16.0));

    public FenceGateBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(OPEN, false)).setValue(POWERED, false)).setValue(IN_WALL, false));
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (blockState.getValue(IN_WALL).booleanValue()) {
            return blockState.getValue(FACING).getAxis() == Direction.Axis.X ? X_SHAPE_LOW : Z_SHAPE_LOW;
        }
        return blockState.getValue(FACING).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        Direction.Axis axis = direction.getAxis();
        if (blockState.getValue(FACING).getClockWise().getAxis() == axis) {
            boolean bl = this.isWall(blockState2) || this.isWall(levelAccessor.getBlockState(blockPos.relative(direction.getOpposite())));
            return (BlockState)blockState.setValue(IN_WALL, bl);
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (blockState.getValue(OPEN).booleanValue()) {
            return Shapes.empty();
        }
        return blockState.getValue(FACING).getAxis() == Direction.Axis.Z ? Z_COLLISION_SHAPE : X_COLLISION_SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        if (blockState.getValue(IN_WALL).booleanValue()) {
            return blockState.getValue(FACING).getAxis() == Direction.Axis.X ? X_OCCLUSION_SHAPE_LOW : Z_OCCLUSION_SHAPE_LOW;
        }
        return blockState.getValue(FACING).getAxis() == Direction.Axis.X ? X_OCCLUSION_SHAPE : Z_OCCLUSION_SHAPE;
    }

    @Override
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        switch (pathComputationType) {
            case LAND: {
                return blockState.getValue(OPEN);
            }
            case WATER: {
                return false;
            }
            case AIR: {
                return blockState.getValue(OPEN);
            }
        }
        return false;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Level level = blockPlaceContext.getLevel();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        boolean bl = level.hasNeighborSignal(blockPos);
        Direction direction = blockPlaceContext.getHorizontalDirection();
        Direction.Axis axis = direction.getAxis();
        boolean bl2 = axis == Direction.Axis.Z && (this.isWall(level.getBlockState(blockPos.west())) || this.isWall(level.getBlockState(blockPos.east()))) || axis == Direction.Axis.X && (this.isWall(level.getBlockState(blockPos.north())) || this.isWall(level.getBlockState(blockPos.south())));
        return (BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, direction)).setValue(OPEN, bl)).setValue(POWERED, bl)).setValue(IN_WALL, bl2);
    }

    private boolean isWall(BlockState blockState) {
        return blockState.is(BlockTags.WALLS);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (blockState.getValue(OPEN).booleanValue()) {
            blockState = (BlockState)blockState.setValue(OPEN, false);
            level.setBlock(blockPos, blockState, 10);
        } else {
            Direction direction = player.getDirection();
            if (blockState.getValue(FACING) == direction.getOpposite()) {
                blockState = (BlockState)blockState.setValue(FACING, direction);
            }
            blockState = (BlockState)blockState.setValue(OPEN, true);
            level.setBlock(blockPos, blockState, 10);
        }
        boolean bl = blockState.getValue(OPEN);
        level.levelEvent(player, bl ? 1008 : 1014, blockPos, 0);
        level.gameEvent((Entity)player, bl ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (level.isClientSide) {
            return;
        }
        boolean bl2 = level.hasNeighborSignal(blockPos);
        if (blockState.getValue(POWERED) != bl2) {
            level.setBlock(blockPos, (BlockState)((BlockState)blockState.setValue(POWERED, bl2)).setValue(OPEN, bl2), 2);
            if (blockState.getValue(OPEN) != bl2) {
                level.levelEvent(null, bl2 ? 1008 : 1014, blockPos, 0);
                level.gameEvent(null, bl2 ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, POWERED, IN_WALL);
    }

    public static boolean connectsToDirection(BlockState blockState, Direction direction) {
        return blockState.getValue(FACING).getAxis() == direction.getClockWise().getAxis();
    }
}

