/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.piston;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PistonBaseBlock
extends DirectionalBlock {
    public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;
    protected static final VoxelShape EAST_AABB = Block.box(0.0, 0.0, 0.0, 12.0, 16.0, 16.0);
    protected static final VoxelShape WEST_AABB = Block.box(4.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 12.0);
    protected static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 4.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape UP_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
    protected static final VoxelShape DOWN_AABB = Block.box(0.0, 4.0, 0.0, 16.0, 16.0, 16.0);
    private final boolean isSticky;

    public PistonBaseBlock(boolean bl, BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(EXTENDED, false));
        this.isSticky = bl;
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (blockState.getValue(EXTENDED).booleanValue()) {
            switch (blockState.getValue(FACING)) {
                case DOWN: {
                    return DOWN_AABB;
                }
                default: {
                    return UP_AABB;
                }
                case NORTH: {
                    return NORTH_AABB;
                }
                case SOUTH: {
                    return SOUTH_AABB;
                }
                case WEST: {
                    return WEST_AABB;
                }
                case EAST: 
            }
            return EAST_AABB;
        }
        return Shapes.block();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
        if (!level.isClientSide) {
            this.checkIfExtend(level, blockPos, blockState);
        }
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (!level.isClientSide) {
            this.checkIfExtend(level, blockPos, blockState);
        }
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState2.is(blockState.getBlock())) {
            return;
        }
        if (!level.isClientSide && level.getBlockEntity(blockPos) == null) {
            this.checkIfExtend(level, blockPos, blockState);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, blockPlaceContext.getNearestLookingDirection().getOpposite())).setValue(EXTENDED, false);
    }

    private void checkIfExtend(Level level, BlockPos blockPos, BlockState blockState) {
        Direction direction = blockState.getValue(FACING);
        boolean bl = this.getNeighborSignal(level, blockPos, direction);
        if (bl && !blockState.getValue(EXTENDED).booleanValue()) {
            if (new PistonStructureResolver(level, blockPos, direction, true).resolve()) {
                level.blockEvent(blockPos, this, 0, direction.get3DDataValue());
            }
        } else if (!bl && blockState.getValue(EXTENDED).booleanValue()) {
            PistonMovingBlockEntity pistonMovingBlockEntity;
            BlockEntity blockEntity;
            BlockPos blockPos2 = blockPos.relative(direction, 2);
            BlockState blockState2 = level.getBlockState(blockPos2);
            int i = 1;
            if (blockState2.is(Blocks.MOVING_PISTON) && blockState2.getValue(FACING) == direction && (blockEntity = level.getBlockEntity(blockPos2)) instanceof PistonMovingBlockEntity && (pistonMovingBlockEntity = (PistonMovingBlockEntity)blockEntity).isExtending() && (pistonMovingBlockEntity.getProgress(0.0f) < 0.5f || level.getGameTime() == pistonMovingBlockEntity.getLastTicked() || ((ServerLevel)level).isHandlingTick())) {
                i = 2;
            }
            level.blockEvent(blockPos, this, i, direction.get3DDataValue());
        }
    }

    private boolean getNeighborSignal(Level level, BlockPos blockPos, Direction direction) {
        for (Direction direction2 : Direction.values()) {
            if (direction2 == direction || !level.hasSignal(blockPos.relative(direction2), direction2)) continue;
            return true;
        }
        if (level.hasSignal(blockPos, Direction.DOWN)) {
            return true;
        }
        BlockPos blockPos2 = blockPos.above();
        for (Direction direction3 : Direction.values()) {
            if (direction3 == Direction.DOWN || !level.hasSignal(blockPos2.relative(direction3), direction3)) continue;
            return true;
        }
        return false;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int i, int j) {
        Direction direction = blockState.getValue(FACING);
        if (!level.isClientSide) {
            boolean bl = this.getNeighborSignal(level, blockPos, direction);
            if (bl && (i == 1 || i == 2)) {
                level.setBlock(blockPos, (BlockState)blockState.setValue(EXTENDED, true), 2);
                return false;
            }
            if (!bl && i == 0) {
                return false;
            }
        }
        if (i == 0) {
            if (!this.moveBlocks(level, blockPos, direction, true)) return false;
            level.setBlock(blockPos, (BlockState)blockState.setValue(EXTENDED, true), 67);
            level.playSound(null, blockPos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5f, level.random.nextFloat() * 0.25f + 0.6f);
            return true;
        } else {
            if (i != 1 && i != 2) return true;
            BlockEntity blockEntity = level.getBlockEntity(blockPos.relative(direction));
            if (blockEntity instanceof PistonMovingBlockEntity) {
                ((PistonMovingBlockEntity)blockEntity).finalTick();
            }
            BlockState blockState2 = (BlockState)((BlockState)Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, direction)).setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
            level.setBlock(blockPos, blockState2, 20);
            level.setBlockEntity(blockPos, MovingPistonBlock.newMovingBlockEntity((BlockState)this.defaultBlockState().setValue(FACING, Direction.from3DDataValue(j & 7)), direction, false, true));
            level.blockUpdated(blockPos, blockState2.getBlock());
            blockState2.updateNeighbourShapes(level, blockPos, 2);
            if (this.isSticky) {
                PistonMovingBlockEntity pistonMovingBlockEntity;
                BlockEntity blockEntity2;
                BlockPos blockPos2 = blockPos.offset(direction.getStepX() * 2, direction.getStepY() * 2, direction.getStepZ() * 2);
                BlockState blockState3 = level.getBlockState(blockPos2);
                boolean bl2 = false;
                if (blockState3.is(Blocks.MOVING_PISTON) && (blockEntity2 = level.getBlockEntity(blockPos2)) instanceof PistonMovingBlockEntity && (pistonMovingBlockEntity = (PistonMovingBlockEntity)blockEntity2).getDirection() == direction && pistonMovingBlockEntity.isExtending()) {
                    pistonMovingBlockEntity.finalTick();
                    bl2 = true;
                }
                if (!bl2) {
                    if (i == 1 && !blockState3.isAir() && PistonBaseBlock.isPushable(blockState3, level, blockPos2, direction.getOpposite(), false, direction) && (blockState3.getPistonPushReaction() == PushReaction.NORMAL || blockState3.is(Blocks.PISTON) || blockState3.is(Blocks.STICKY_PISTON))) {
                        this.moveBlocks(level, blockPos, direction, false);
                    } else {
                        level.removeBlock(blockPos.relative(direction), false);
                    }
                }
            } else {
                level.removeBlock(blockPos.relative(direction), false);
            }
            level.playSound(null, blockPos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5f, level.random.nextFloat() * 0.15f + 0.6f);
        }
        return true;
    }

    public static boolean isPushable(BlockState blockState, Level level, BlockPos blockPos, Direction direction, boolean bl, Direction direction2) {
        if (blockPos.getY() < 0 || blockPos.getY() > level.getMaxBuildHeight() - 1 || !level.getWorldBorder().isWithinBounds(blockPos)) {
            return false;
        }
        if (blockState.isAir()) {
            return true;
        }
        if (blockState.is(Blocks.OBSIDIAN) || blockState.is(Blocks.CRYING_OBSIDIAN) || blockState.is(Blocks.RESPAWN_ANCHOR)) {
            return false;
        }
        if (direction == Direction.DOWN && blockPos.getY() == 0) {
            return false;
        }
        if (direction == Direction.UP && blockPos.getY() == level.getMaxBuildHeight() - 1) {
            return false;
        }
        if (blockState.is(Blocks.PISTON) || blockState.is(Blocks.STICKY_PISTON)) {
            if (blockState.getValue(EXTENDED).booleanValue()) {
                return false;
            }
        } else {
            if (blockState.getDestroySpeed(level, blockPos) == -1.0f) {
                return false;
            }
            switch (blockState.getPistonPushReaction()) {
                case BLOCK: {
                    return false;
                }
                case DESTROY: {
                    return bl;
                }
                case PUSH_ONLY: {
                    return direction == direction2;
                }
            }
        }
        return !blockState.getBlock().isEntityBlock();
    }

    private boolean moveBlocks(Level level, BlockPos blockPos, Direction direction, boolean bl) {
        int l;
        BlockPos blockPos6;
        BlockPos blockPos4;
        int k;
        PistonStructureResolver pistonStructureResolver;
        BlockPos blockPos2 = blockPos.relative(direction);
        if (!bl && level.getBlockState(blockPos2).is(Blocks.PISTON_HEAD)) {
            level.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 20);
        }
        if (!(pistonStructureResolver = new PistonStructureResolver(level, blockPos, direction, bl)).resolve()) {
            return false;
        }
        HashMap<BlockPos, BlockState> map = Maps.newHashMap();
        List<BlockPos> list = pistonStructureResolver.getToPush();
        ArrayList<BlockState> list2 = Lists.newArrayList();
        for (int i = 0; i < list.size(); ++i) {
            BlockPos blockPos3 = list.get(i);
            BlockState blockState = level.getBlockState(blockPos3);
            list2.add(blockState);
            map.put(blockPos3, blockState);
        }
        List<BlockPos> list3 = pistonStructureResolver.getToDestroy();
        BlockState[] blockStates = new BlockState[list.size() + list3.size()];
        Direction direction2 = bl ? direction : direction.getOpposite();
        int j = 0;
        for (k = list3.size() - 1; k >= 0; --k) {
            blockPos4 = list3.get(k);
            BlockState blockState = level.getBlockState(blockPos4);
            BlockEntity blockEntity = blockState.getBlock().isEntityBlock() ? level.getBlockEntity(blockPos4) : null;
            PistonBaseBlock.dropResources(blockState, level, blockPos4, blockEntity);
            level.setBlock(blockPos4, Blocks.AIR.defaultBlockState(), 18);
            blockStates[j++] = blockState;
        }
        for (k = list.size() - 1; k >= 0; --k) {
            blockPos4 = list.get(k);
            BlockState blockState = level.getBlockState(blockPos4);
            blockPos4 = blockPos4.relative(direction2);
            map.remove(blockPos4);
            level.setBlock(blockPos4, (BlockState)Blocks.MOVING_PISTON.defaultBlockState().setValue(FACING, direction), 68);
            level.setBlockEntity(blockPos4, MovingPistonBlock.newMovingBlockEntity((BlockState)list2.get(k), direction, bl, false));
            blockStates[j++] = blockState;
        }
        if (bl) {
            PistonType pistonType = this.isSticky ? PistonType.STICKY : PistonType.DEFAULT;
            BlockState blockState3 = (BlockState)((BlockState)Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.FACING, direction)).setValue(PistonHeadBlock.TYPE, pistonType);
            BlockState blockState = (BlockState)((BlockState)Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, direction)).setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
            map.remove(blockPos2);
            level.setBlock(blockPos2, blockState, 68);
            level.setBlockEntity(blockPos2, MovingPistonBlock.newMovingBlockEntity(blockState3, direction, true, true));
        }
        BlockState blockState4 = Blocks.AIR.defaultBlockState();
        for (BlockPos blockPos3 : map.keySet()) {
            level.setBlock(blockPos3, blockState4, 82);
        }
        for (Map.Entry entry : map.entrySet()) {
            blockPos6 = (BlockPos)entry.getKey();
            BlockState blockState5 = (BlockState)entry.getValue();
            blockState5.updateIndirectNeighbourShapes(level, blockPos6, 2);
            blockState4.updateNeighbourShapes(level, blockPos6, 2);
            blockState4.updateIndirectNeighbourShapes(level, blockPos6, 2);
        }
        j = 0;
        for (l = list3.size() - 1; l >= 0; --l) {
            BlockState blockState = blockStates[j++];
            blockPos6 = list3.get(l);
            blockState.updateIndirectNeighbourShapes(level, blockPos6, 2);
            level.updateNeighborsAt(blockPos6, blockState.getBlock());
        }
        for (l = list.size() - 1; l >= 0; --l) {
            level.updateNeighborsAt(list.get(l), blockStates[j++].getBlock());
        }
        if (bl) {
            level.updateNeighborsAt(blockPos2, Blocks.PISTON_HEAD);
        }
        return true;
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, EXTENDED);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return blockState.getValue(EXTENDED);
    }

    @Override
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }
}

