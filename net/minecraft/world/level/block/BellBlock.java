/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BellBlock
extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final EnumProperty<BellAttachType> ATTACHMENT = BlockStateProperties.BELL_ATTACHMENT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final VoxelShape NORTH_SOUTH_FLOOR_SHAPE = Block.box(0.0, 0.0, 4.0, 16.0, 16.0, 12.0);
    private static final VoxelShape EAST_WEST_FLOOR_SHAPE = Block.box(4.0, 0.0, 0.0, 12.0, 16.0, 16.0);
    private static final VoxelShape BELL_TOP_SHAPE = Block.box(5.0, 6.0, 5.0, 11.0, 13.0, 11.0);
    private static final VoxelShape BELL_BOTTOM_SHAPE = Block.box(4.0, 4.0, 4.0, 12.0, 6.0, 12.0);
    private static final VoxelShape BELL_SHAPE = Shapes.or(BELL_BOTTOM_SHAPE, BELL_TOP_SHAPE);
    private static final VoxelShape NORTH_SOUTH_BETWEEN = Shapes.or(BELL_SHAPE, Block.box(7.0, 13.0, 0.0, 9.0, 15.0, 16.0));
    private static final VoxelShape EAST_WEST_BETWEEN = Shapes.or(BELL_SHAPE, Block.box(0.0, 13.0, 7.0, 16.0, 15.0, 9.0));
    private static final VoxelShape TO_WEST = Shapes.or(BELL_SHAPE, Block.box(0.0, 13.0, 7.0, 13.0, 15.0, 9.0));
    private static final VoxelShape TO_EAST = Shapes.or(BELL_SHAPE, Block.box(3.0, 13.0, 7.0, 16.0, 15.0, 9.0));
    private static final VoxelShape TO_NORTH = Shapes.or(BELL_SHAPE, Block.box(7.0, 13.0, 0.0, 9.0, 15.0, 13.0));
    private static final VoxelShape TO_SOUTH = Shapes.or(BELL_SHAPE, Block.box(7.0, 13.0, 3.0, 9.0, 15.0, 16.0));
    private static final VoxelShape CEILING_SHAPE = Shapes.or(BELL_SHAPE, Block.box(7.0, 13.0, 7.0, 9.0, 16.0, 9.0));

    public BellBlock(Block.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(ATTACHMENT, BellAttachType.FLOOR)).setValue(POWERED, false));
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        boolean bl2 = level.hasNeighborSignal(blockPos);
        if (bl2 != blockState.getValue(POWERED)) {
            if (bl2) {
                this.attemptToRing(level, blockPos, null);
            }
            level.setBlock(blockPos, (BlockState)blockState.setValue(POWERED, bl2), 3);
        }
    }

    @Override
    public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Entity entity) {
        if (entity instanceof AbstractArrow) {
            Entity entity2 = ((AbstractArrow)entity).getOwner();
            Player player = entity2 instanceof Player ? (Player)entity2 : null;
            this.onHit(level, blockState, blockHitResult, player, true);
        }
    }

    @Override
    public boolean use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        return this.onHit(level, blockState, blockHitResult, player, true);
    }

    public boolean onHit(Level level, BlockState blockState, BlockHitResult blockHitResult, @Nullable Player player, boolean bl) {
        boolean bl3;
        boolean bl2;
        Direction direction = blockHitResult.getDirection();
        BlockPos blockPos = blockHitResult.getBlockPos();
        boolean bl4 = bl2 = !bl || this.isProperHit(blockState, direction, blockHitResult.getLocation().y - (double)blockPos.getY());
        if (bl2 && (bl3 = this.attemptToRing(level, blockPos, direction)) && player != null) {
            player.awardStat(Stats.BELL_RING);
        }
        return true;
    }

    private boolean isProperHit(BlockState blockState, Direction direction, double d) {
        if (direction.getAxis() == Direction.Axis.Y || d > (double)0.8124f) {
            return false;
        }
        Direction direction2 = blockState.getValue(FACING);
        BellAttachType bellAttachType = blockState.getValue(ATTACHMENT);
        switch (bellAttachType) {
            case FLOOR: {
                return direction2.getAxis() == direction.getAxis();
            }
            case SINGLE_WALL: 
            case DOUBLE_WALL: {
                return direction2.getAxis() != direction.getAxis();
            }
            case CEILING: {
                return true;
            }
        }
        return false;
    }

    public boolean attemptToRing(Level level, BlockPos blockPos, @Nullable Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!level.isClientSide && blockEntity instanceof BellBlockEntity) {
            if (direction == null) {
                direction = level.getBlockState(blockPos).getValue(FACING);
            }
            ((BellBlockEntity)blockEntity).onHit(direction);
            level.playSound(null, blockPos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0f, 1.0f);
            return true;
        }
        return false;
    }

    private VoxelShape getVoxelShape(BlockState blockState) {
        Direction direction = blockState.getValue(FACING);
        BellAttachType bellAttachType = blockState.getValue(ATTACHMENT);
        if (bellAttachType == BellAttachType.FLOOR) {
            if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                return NORTH_SOUTH_FLOOR_SHAPE;
            }
            return EAST_WEST_FLOOR_SHAPE;
        }
        if (bellAttachType == BellAttachType.CEILING) {
            return CEILING_SHAPE;
        }
        if (bellAttachType == BellAttachType.DOUBLE_WALL) {
            if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                return NORTH_SOUTH_BETWEEN;
            }
            return EAST_WEST_BETWEEN;
        }
        if (direction == Direction.NORTH) {
            return TO_NORTH;
        }
        if (direction == Direction.SOUTH) {
            return TO_SOUTH;
        }
        if (direction == Direction.EAST) {
            return TO_EAST;
        }
        return TO_WEST;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.getVoxelShape(blockState);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.getVoxelShape(blockState);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction direction = blockPlaceContext.getClickedFace();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        Level level = blockPlaceContext.getLevel();
        Direction.Axis axis = direction.getAxis();
        if (axis == Direction.Axis.Y) {
            BlockState blockState = (BlockState)((BlockState)this.defaultBlockState().setValue(ATTACHMENT, direction == Direction.DOWN ? BellAttachType.CEILING : BellAttachType.FLOOR)).setValue(FACING, blockPlaceContext.getHorizontalDirection());
            if (blockState.canSurvive(blockPlaceContext.getLevel(), blockPos)) {
                return blockState;
            }
        } else {
            boolean bl = axis == Direction.Axis.X && level.getBlockState(blockPos.west()).isFaceSturdy(level, blockPos.west(), Direction.EAST) && level.getBlockState(blockPos.east()).isFaceSturdy(level, blockPos.east(), Direction.WEST) || axis == Direction.Axis.Z && level.getBlockState(blockPos.north()).isFaceSturdy(level, blockPos.north(), Direction.SOUTH) && level.getBlockState(blockPos.south()).isFaceSturdy(level, blockPos.south(), Direction.NORTH);
            BlockState blockState = (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, direction.getOpposite())).setValue(ATTACHMENT, bl ? BellAttachType.DOUBLE_WALL : BellAttachType.SINGLE_WALL);
            if (blockState.canSurvive(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos())) {
                return blockState;
            }
            boolean bl2 = level.getBlockState(blockPos.below()).isFaceSturdy(level, blockPos.below(), Direction.UP);
            if ((blockState = (BlockState)blockState.setValue(ATTACHMENT, bl2 ? BellAttachType.FLOOR : BellAttachType.CEILING)).canSurvive(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos())) {
                return blockState;
            }
        }
        return null;
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        BellAttachType bellAttachType = blockState.getValue(ATTACHMENT);
        Direction direction2 = BellBlock.getConnectedDirection(blockState).getOpposite();
        if (direction2 == direction && !blockState.canSurvive(levelAccessor, blockPos) && bellAttachType != BellAttachType.DOUBLE_WALL) {
            return Blocks.AIR.defaultBlockState();
        }
        if (direction.getAxis() == blockState.getValue(FACING).getAxis()) {
            if (bellAttachType == BellAttachType.DOUBLE_WALL && !blockState2.isFaceSturdy(levelAccessor, blockPos2, direction)) {
                return (BlockState)((BlockState)blockState.setValue(ATTACHMENT, BellAttachType.SINGLE_WALL)).setValue(FACING, direction.getOpposite());
            }
            if (bellAttachType == BellAttachType.SINGLE_WALL && direction2.getOpposite() == direction && blockState2.isFaceSturdy(levelAccessor, blockPos2, blockState.getValue(FACING))) {
                return (BlockState)blockState.setValue(ATTACHMENT, BellAttachType.DOUBLE_WALL);
            }
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return FaceAttachedHorizontalDirectionalBlock.canAttach(levelReader, blockPos, BellBlock.getConnectedDirection(blockState).getOpposite());
    }

    private static Direction getConnectedDirection(BlockState blockState) {
        switch (blockState.getValue(ATTACHMENT)) {
            case CEILING: {
                return Direction.DOWN;
            }
            case FLOOR: {
                return Direction.UP;
            }
        }
        return blockState.getValue(FACING).getOpposite();
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState blockState) {
        return PushReaction.DESTROY;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ATTACHMENT, POWERED);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new BellBlockEntity();
    }

    @Override
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }
}

