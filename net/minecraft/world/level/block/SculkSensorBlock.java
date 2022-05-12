/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SculkSensorBlock
extends BaseEntityBlock
implements SimpleWaterloggedBlock {
    public static final int ACTIVE_TICKS = 40;
    public static final int COOLDOWN_TICKS = 1;
    public static final Object2IntMap<GameEvent> VIBRATION_FREQUENCY_FOR_EVENT = Object2IntMaps.unmodifiable(Util.make(new Object2IntOpenHashMap(), object2IntOpenHashMap -> {
        object2IntOpenHashMap.put(GameEvent.STEP, 1);
        object2IntOpenHashMap.put(GameEvent.FLAP, 2);
        object2IntOpenHashMap.put(GameEvent.SWIM, 3);
        object2IntOpenHashMap.put(GameEvent.ELYTRA_GLIDE, 4);
        object2IntOpenHashMap.put(GameEvent.HIT_GROUND, 5);
        object2IntOpenHashMap.put(GameEvent.SPLASH, 6);
        object2IntOpenHashMap.put(GameEvent.ENTITY_SHAKE, 6);
        object2IntOpenHashMap.put(GameEvent.BLOCK_CHANGE, 6);
        object2IntOpenHashMap.put(GameEvent.NOTE_BLOCK_PLAY, 6);
        object2IntOpenHashMap.put(GameEvent.PROJECTILE_SHOOT, 7);
        object2IntOpenHashMap.put(GameEvent.DRINK, 7);
        object2IntOpenHashMap.put(GameEvent.PRIME_FUSE, 7);
        object2IntOpenHashMap.put(GameEvent.PROJECTILE_LAND, 8);
        object2IntOpenHashMap.put(GameEvent.EAT, 8);
        object2IntOpenHashMap.put(GameEvent.ENTITY_INTERACT, 8);
        object2IntOpenHashMap.put(GameEvent.ENTITY_DAMAGE, 8);
        object2IntOpenHashMap.put(GameEvent.EQUIP, 9);
        object2IntOpenHashMap.put(GameEvent.SHEAR, 9);
        object2IntOpenHashMap.put(GameEvent.ENTITY_ROAR, 9);
        object2IntOpenHashMap.put(GameEvent.BLOCK_CLOSE, 10);
        object2IntOpenHashMap.put(GameEvent.BLOCK_DEACTIVATE, 10);
        object2IntOpenHashMap.put(GameEvent.BLOCK_DETACH, 10);
        object2IntOpenHashMap.put(GameEvent.DISPENSE_FAIL, 10);
        object2IntOpenHashMap.put(GameEvent.BLOCK_OPEN, 11);
        object2IntOpenHashMap.put(GameEvent.BLOCK_ACTIVATE, 11);
        object2IntOpenHashMap.put(GameEvent.BLOCK_ATTACH, 11);
        object2IntOpenHashMap.put(GameEvent.ENTITY_PLACE, 12);
        object2IntOpenHashMap.put(GameEvent.BLOCK_PLACE, 12);
        object2IntOpenHashMap.put(GameEvent.FLUID_PLACE, 12);
        object2IntOpenHashMap.put(GameEvent.ENTITY_DIE, 13);
        object2IntOpenHashMap.put(GameEvent.BLOCK_DESTROY, 13);
        object2IntOpenHashMap.put(GameEvent.FLUID_PICKUP, 13);
        object2IntOpenHashMap.put(GameEvent.ITEM_INTERACT_FINISH, 14);
        object2IntOpenHashMap.put(GameEvent.CONTAINER_CLOSE, 14);
        object2IntOpenHashMap.put(GameEvent.PISTON_CONTRACT, 14);
        object2IntOpenHashMap.put(GameEvent.PISTON_EXTEND, 15);
        object2IntOpenHashMap.put(GameEvent.CONTAINER_OPEN, 15);
        object2IntOpenHashMap.put(GameEvent.ITEM_INTERACT_START, 15);
        object2IntOpenHashMap.put(GameEvent.EXPLODE, 15);
        object2IntOpenHashMap.put(GameEvent.LIGHTNING_STRIKE, 15);
    }));
    public static final EnumProperty<SculkSensorPhase> PHASE = BlockStateProperties.SCULK_SENSOR_PHASE;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    private final int listenerRange;

    public SculkSensorBlock(BlockBehaviour.Properties properties, int i) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(PHASE, SculkSensorPhase.INACTIVE)).setValue(POWER, 0)).setValue(WATERLOGGED, false));
        this.listenerRange = i;
    }

    public int getListenerRange() {
        return this.listenerRange;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPos);
        return (BlockState)this.defaultBlockState().setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (SculkSensorBlock.getPhase(blockState) != SculkSensorPhase.ACTIVE) {
            if (SculkSensorBlock.getPhase(blockState) == SculkSensorPhase.COOLDOWN) {
                serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(PHASE, SculkSensorPhase.INACTIVE), 3);
            }
            return;
        }
        SculkSensorBlock.deactivate(serverLevel, blockPos, blockState);
    }

    @Override
    public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
        if (!level.isClientSide() && SculkSensorBlock.canActivate(blockState) && entity.getType() != EntityType.WARDEN) {
            SculkSensorBlock.activate(entity, level, blockPos, blockState, 1);
        }
        super.stepOn(level, blockPos, blockState, entity);
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (level.isClientSide() || blockState.is(blockState2.getBlock())) {
            return;
        }
        if (blockState.getValue(POWER) > 0 && !level.getBlockTicks().hasScheduledTick(blockPos, this)) {
            level.setBlock(blockPos, (BlockState)blockState.setValue(POWER, 0), 18);
        }
        level.scheduleTick(new BlockPos(blockPos), blockState.getBlock(), 1);
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState.is(blockState2.getBlock())) {
            return;
        }
        if (SculkSensorBlock.getPhase(blockState) == SculkSensorPhase.ACTIVE) {
            SculkSensorBlock.updateNeighbours(level, blockPos);
        }
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    private static void updateNeighbours(Level level, BlockPos blockPos) {
        level.updateNeighborsAt(blockPos, Blocks.SCULK_SENSOR);
        level.updateNeighborsAt(blockPos.relative(Direction.UP.getOpposite()), Blocks.SCULK_SENSOR);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SculkSensorBlockEntity(blockPos, blockState);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> GameEventListener getListener(ServerLevel serverLevel, T blockEntity) {
        if (blockEntity instanceof SculkSensorBlockEntity) {
            return ((SculkSensorBlockEntity)blockEntity).getListener();
        }
        return null;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level2, BlockState blockState2, BlockEntityType<T> blockEntityType) {
        if (!level2.isClientSide) {
            return SculkSensorBlock.createTickerHelper(blockEntityType, BlockEntityType.SCULK_SENSOR, (level, blockPos, blockState, sculkSensorBlockEntity) -> sculkSensorBlockEntity.getListener().tick(level));
        }
        return null;
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockState.getValue(POWER);
    }

    public static SculkSensorPhase getPhase(BlockState blockState) {
        return blockState.getValue(PHASE);
    }

    public static boolean canActivate(BlockState blockState) {
        return SculkSensorBlock.getPhase(blockState) == SculkSensorPhase.INACTIVE;
    }

    public static void deactivate(Level level, BlockPos blockPos, BlockState blockState) {
        level.setBlock(blockPos, (BlockState)((BlockState)blockState.setValue(PHASE, SculkSensorPhase.COOLDOWN)).setValue(POWER, 0), 3);
        level.scheduleTick(blockPos, blockState.getBlock(), 1);
        if (!blockState.getValue(WATERLOGGED).booleanValue()) {
            level.playSound(null, blockPos, SoundEvents.SCULK_CLICKING_STOP, SoundSource.BLOCKS, 1.0f, level.random.nextFloat() * 0.2f + 0.8f);
        }
        SculkSensorBlock.updateNeighbours(level, blockPos);
    }

    public static void activate(@Nullable Entity entity, Level level, BlockPos blockPos, BlockState blockState, int i) {
        level.setBlock(blockPos, (BlockState)((BlockState)blockState.setValue(PHASE, SculkSensorPhase.ACTIVE)).setValue(POWER, i), 3);
        level.scheduleTick(blockPos, blockState.getBlock(), 40);
        SculkSensorBlock.updateNeighbours(level, blockPos);
        level.gameEvent(entity, GameEvent.SCULK_SENSOR_TENDRILS_CLICKING, blockPos);
        if (!blockState.getValue(WATERLOGGED).booleanValue()) {
            level.playSound(null, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, SoundEvents.SCULK_CLICKING, SoundSource.BLOCKS, 1.0f, level.random.nextFloat() * 0.2f + 0.8f);
        }
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (SculkSensorBlock.getPhase(blockState) != SculkSensorPhase.ACTIVE) {
            return;
        }
        Direction direction = Direction.getRandom(randomSource);
        if (direction == Direction.UP || direction == Direction.DOWN) {
            return;
        }
        double d = (double)blockPos.getX() + 0.5 + (direction.getStepX() == 0 ? 0.5 - randomSource.nextDouble() : (double)direction.getStepX() * 0.6);
        double e = (double)blockPos.getY() + 0.25;
        double f = (double)blockPos.getZ() + 0.5 + (direction.getStepZ() == 0 ? 0.5 - randomSource.nextDouble() : (double)direction.getStepZ() * 0.6);
        double g = (double)randomSource.nextFloat() * 0.04;
        level.addParticle(DustColorTransitionOptions.SCULK_TO_REDSTONE, d, e, f, 0.0, g, 0.0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PHASE, POWER, WATERLOGGED);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof SculkSensorBlockEntity) {
            SculkSensorBlockEntity sculkSensorBlockEntity = (SculkSensorBlockEntity)blockEntity;
            return SculkSensorBlock.getPhase(blockState) == SculkSensorPhase.ACTIVE ? sculkSensorBlockEntity.getLastVibrationFrequency() : 0;
        }
        return 0;
    }

    @Override
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override
    public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
        super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack, bl);
        if (bl) {
            this.tryDropExperience(serverLevel, blockPos, itemStack, ConstantInt.of(5));
        }
    }
}

