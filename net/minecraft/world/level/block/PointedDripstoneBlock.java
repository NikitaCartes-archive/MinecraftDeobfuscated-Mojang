/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PointedDripstoneBlock
extends Block
implements Fallable,
SimpleWaterloggedBlock {
    public static final DirectionProperty TIP_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;
    public static final EnumProperty<DripstoneThickness> THICKNESS = BlockStateProperties.DRIPSTONE_THICKNESS;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final int MAX_SEARCH_LENGTH_WHEN_CHECKING_DRIP_TYPE = 11;
    private static final int DELAY_BEFORE_FALLING = 2;
    private static final float DRIP_PROBABILITY_PER_ANIMATE_TICK = 0.02f;
    private static final float DRIP_PROBABILITY_PER_ANIMATE_TICK_IF_UNDER_LIQUID_SOURCE = 0.12f;
    private static final int MAX_SEARCH_LENGTH_BETWEEN_STALACTITE_TIP_AND_CAULDRON = 11;
    private static final float WATER_TRANSFER_PROBABILITY_PER_RANDOM_TICK = 0.17578125f;
    private static final float LAVA_TRANSFER_PROBABILITY_PER_RANDOM_TICK = 0.05859375f;
    private static final double MIN_TRIDENT_VELOCITY_TO_BREAK_DRIPSTONE = 0.6;
    private static final float STALACTITE_DAMAGE_PER_FALL_DISTANCE_AND_SIZE = 1.0f;
    private static final int STALACTITE_MAX_DAMAGE = 40;
    private static final int MAX_STALACTITE_HEIGHT_FOR_DAMAGE_CALCULATION = 6;
    private static final float STALAGMITE_FALL_DISTANCE_OFFSET = 2.0f;
    private static final int STALAGMITE_FALL_DAMAGE_MODIFIER = 2;
    private static final float AVERAGE_DAYS_PER_GROWTH = 5.0f;
    private static final float GROWTH_PROBABILITY_PER_RANDOM_TICK = 0.011377778f;
    private static final int MAX_GROWTH_LENGTH = 7;
    private static final int MAX_STALAGMITE_SEARCH_RANGE_WHEN_GROWING = 10;
    private static final float STALACTITE_DRIP_START_PIXEL = 0.6875f;
    private static final VoxelShape TIP_MERGE_SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape TIP_SHAPE_UP = Block.box(5.0, 0.0, 5.0, 11.0, 11.0, 11.0);
    private static final VoxelShape TIP_SHAPE_DOWN = Block.box(5.0, 5.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape FRUSTUM_SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
    private static final VoxelShape MIDDLE_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
    private static final VoxelShape BASE_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
    private static final float MAX_HORIZONTAL_OFFSET = 0.125f;
    private static final VoxelShape REQUIRED_SPACE_TO_DRIP_THROUGH_NON_SOLID_BLOCK = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);

    public PointedDripstoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(TIP_DIRECTION, Direction.UP)).setValue(THICKNESS, DripstoneThickness.TIP)).setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIP_DIRECTION, THICKNESS, WATERLOGGED);
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return PointedDripstoneBlock.isValidPointedDripstonePlacement(levelReader, blockPos, blockState.getValue(TIP_DIRECTION));
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        if (direction != Direction.UP && direction != Direction.DOWN) {
            return blockState;
        }
        Direction direction2 = blockState.getValue(TIP_DIRECTION);
        if (direction2 == Direction.DOWN && levelAccessor.getBlockTicks().hasScheduledTick(blockPos, this)) {
            return blockState;
        }
        if (direction == direction2.getOpposite() && !this.canSurvive(blockState, levelAccessor, blockPos)) {
            if (direction2 == Direction.DOWN) {
                levelAccessor.scheduleTick(blockPos, this, 2);
            } else {
                levelAccessor.scheduleTick(blockPos, this, 1);
            }
            return blockState;
        }
        boolean bl = blockState.getValue(THICKNESS) == DripstoneThickness.TIP_MERGE;
        DripstoneThickness dripstoneThickness = PointedDripstoneBlock.calculateDripstoneThickness(levelAccessor, blockPos, direction2, bl);
        return (BlockState)blockState.setValue(THICKNESS, dripstoneThickness);
    }

    @Override
    public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
        BlockPos blockPos = blockHitResult.getBlockPos();
        if (!level.isClientSide && projectile.mayInteract(level, blockPos) && projectile instanceof ThrownTrident && projectile.getDeltaMovement().length() > 0.6) {
            level.destroyBlock(blockPos, true);
        }
    }

    @Override
    public void fallOn(Level level, BlockState blockState, BlockPos blockPos, Entity entity, float f) {
        if (blockState.getValue(TIP_DIRECTION) == Direction.UP && blockState.getValue(THICKNESS) == DripstoneThickness.TIP) {
            entity.causeFallDamage(f + 2.0f, 2.0f, level.damageSources().stalagmite());
        } else {
            super.fallOn(level, blockState, blockPos, entity, f);
        }
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (!PointedDripstoneBlock.canDrip(blockState)) {
            return;
        }
        float f = randomSource.nextFloat();
        if (f > 0.12f) {
            return;
        }
        PointedDripstoneBlock.getFluidAboveStalactite(level, blockPos, blockState).filter(fluidInfo -> f < 0.02f || PointedDripstoneBlock.canFillCauldron(fluidInfo.fluid)).ifPresent(fluidInfo -> PointedDripstoneBlock.spawnDripParticle(level, blockPos, blockState, fluidInfo.fluid));
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (PointedDripstoneBlock.isStalagmite(blockState) && !this.canSurvive(blockState, serverLevel, blockPos)) {
            serverLevel.destroyBlock(blockPos, true);
        } else {
            PointedDripstoneBlock.spawnFallingStalactite(blockState, serverLevel, blockPos);
        }
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        PointedDripstoneBlock.maybeTransferFluid(blockState, serverLevel, blockPos, randomSource.nextFloat());
        if (randomSource.nextFloat() < 0.011377778f && PointedDripstoneBlock.isStalactiteStartPos(blockState, serverLevel, blockPos)) {
            PointedDripstoneBlock.growStalactiteOrStalagmiteIfPossible(blockState, serverLevel, blockPos, randomSource);
        }
    }

    @VisibleForTesting
    public static void maybeTransferFluid(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, float f) {
        float g;
        if (f > 0.17578125f && f > 0.05859375f) {
            return;
        }
        if (!PointedDripstoneBlock.isStalactiteStartPos(blockState, serverLevel, blockPos)) {
            return;
        }
        Optional<FluidInfo> optional = PointedDripstoneBlock.getFluidAboveStalactite(serverLevel, blockPos, blockState);
        if (optional.isEmpty()) {
            return;
        }
        Fluid fluid = optional.get().fluid;
        if (fluid == Fluids.WATER) {
            g = 0.17578125f;
        } else if (fluid == Fluids.LAVA) {
            g = 0.05859375f;
        } else {
            return;
        }
        if (f >= g) {
            return;
        }
        BlockPos blockPos2 = PointedDripstoneBlock.findTip(blockState, serverLevel, blockPos, 11, false);
        if (blockPos2 == null) {
            return;
        }
        if (optional.get().sourceState.is(Blocks.MUD) && fluid == Fluids.WATER) {
            BlockState blockState2 = Blocks.CLAY.defaultBlockState();
            serverLevel.setBlockAndUpdate(optional.get().pos, blockState2);
            Block.pushEntitiesUp(optional.get().sourceState, blockState2, serverLevel, optional.get().pos);
            serverLevel.gameEvent(GameEvent.BLOCK_CHANGE, optional.get().pos, GameEvent.Context.of(blockState2));
            serverLevel.levelEvent(1504, blockPos2, 0);
            return;
        }
        BlockPos blockPos3 = PointedDripstoneBlock.findFillableCauldronBelowStalactiteTip(serverLevel, blockPos2, fluid);
        if (blockPos3 == null) {
            return;
        }
        serverLevel.levelEvent(1504, blockPos2, 0);
        int i = blockPos2.getY() - blockPos3.getY();
        int j = 50 + i;
        BlockState blockState3 = serverLevel.getBlockState(blockPos3);
        serverLevel.scheduleTick(blockPos3, blockState3.getBlock(), j);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState blockState) {
        return PushReaction.DESTROY;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction direction;
        BlockPos blockPos;
        Level levelAccessor = blockPlaceContext.getLevel();
        Direction direction2 = PointedDripstoneBlock.calculateTipDirection(levelAccessor, blockPos = blockPlaceContext.getClickedPos(), direction = blockPlaceContext.getNearestLookingVerticalDirection().getOpposite());
        if (direction2 == null) {
            return null;
        }
        boolean bl = !blockPlaceContext.isSecondaryUseActive();
        DripstoneThickness dripstoneThickness = PointedDripstoneBlock.calculateDripstoneThickness(levelAccessor, blockPos, direction2, bl);
        if (dripstoneThickness == null) {
            return null;
        }
        return (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(TIP_DIRECTION, direction2)).setValue(THICKNESS, dripstoneThickness)).setValue(WATERLOGGED, levelAccessor.getFluidState(blockPos).getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        DripstoneThickness dripstoneThickness = blockState.getValue(THICKNESS);
        VoxelShape voxelShape = dripstoneThickness == DripstoneThickness.TIP_MERGE ? TIP_MERGE_SHAPE : (dripstoneThickness == DripstoneThickness.TIP ? (blockState.getValue(TIP_DIRECTION) == Direction.DOWN ? TIP_SHAPE_DOWN : TIP_SHAPE_UP) : (dripstoneThickness == DripstoneThickness.FRUSTUM ? FRUSTUM_SHAPE : (dripstoneThickness == DripstoneThickness.MIDDLE ? MIDDLE_SHAPE : BASE_SHAPE)));
        Vec3 vec3 = blockState.getOffset(blockGetter, blockPos);
        return voxelShape.move(vec3.x, 0.0, vec3.z);
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return false;
    }

    @Override
    public float getMaxHorizontalOffset() {
        return 0.125f;
    }

    @Override
    public void onBrokenAfterFall(Level level, BlockPos blockPos, FallingBlockEntity fallingBlockEntity) {
        if (!fallingBlockEntity.isSilent()) {
            level.levelEvent(1045, blockPos, 0);
        }
    }

    @Override
    public DamageSource getFallDamageSource(Entity entity) {
        return entity.damageSources().fallingStalactite(entity);
    }

    @Override
    public Predicate<Entity> getHurtsEntitySelector() {
        return EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE);
    }

    private static void spawnFallingStalactite(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        BlockState blockState2 = blockState;
        while (PointedDripstoneBlock.isStalactite(blockState2)) {
            FallingBlockEntity fallingBlockEntity = FallingBlockEntity.fall(serverLevel, mutableBlockPos, blockState2);
            if (PointedDripstoneBlock.isTip(blockState2, true)) {
                int i = Math.max(1 + blockPos.getY() - mutableBlockPos.getY(), 6);
                float f = 1.0f * (float)i;
                fallingBlockEntity.setHurtsEntities(f, 40);
                break;
            }
            mutableBlockPos.move(Direction.DOWN);
            blockState2 = serverLevel.getBlockState(mutableBlockPos);
        }
    }

    @VisibleForTesting
    public static void growStalactiteOrStalagmiteIfPossible(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        BlockState blockState3;
        BlockState blockState2 = serverLevel.getBlockState(blockPos.above(1));
        if (!PointedDripstoneBlock.canGrow(blockState2, blockState3 = serverLevel.getBlockState(blockPos.above(2)))) {
            return;
        }
        BlockPos blockPos2 = PointedDripstoneBlock.findTip(blockState, serverLevel, blockPos, 7, false);
        if (blockPos2 == null) {
            return;
        }
        BlockState blockState4 = serverLevel.getBlockState(blockPos2);
        if (!PointedDripstoneBlock.canDrip(blockState4) || !PointedDripstoneBlock.canTipGrow(blockState4, serverLevel, blockPos2)) {
            return;
        }
        if (randomSource.nextBoolean()) {
            PointedDripstoneBlock.grow(serverLevel, blockPos2, Direction.DOWN);
        } else {
            PointedDripstoneBlock.growStalagmiteBelow(serverLevel, blockPos2);
        }
    }

    private static void growStalagmiteBelow(ServerLevel serverLevel, BlockPos blockPos) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (int i = 0; i < 10; ++i) {
            mutableBlockPos.move(Direction.DOWN);
            BlockState blockState = serverLevel.getBlockState(mutableBlockPos);
            if (!blockState.getFluidState().isEmpty()) {
                return;
            }
            if (PointedDripstoneBlock.isUnmergedTipWithDirection(blockState, Direction.UP) && PointedDripstoneBlock.canTipGrow(blockState, serverLevel, mutableBlockPos)) {
                PointedDripstoneBlock.grow(serverLevel, mutableBlockPos, Direction.UP);
                return;
            }
            if (PointedDripstoneBlock.isValidPointedDripstonePlacement(serverLevel, mutableBlockPos, Direction.UP) && !serverLevel.isWaterAt((BlockPos)mutableBlockPos.below())) {
                PointedDripstoneBlock.grow(serverLevel, (BlockPos)mutableBlockPos.below(), Direction.UP);
                return;
            }
            if (PointedDripstoneBlock.canDripThrough(serverLevel, mutableBlockPos, blockState)) continue;
            return;
        }
    }

    private static void grow(ServerLevel serverLevel, BlockPos blockPos, Direction direction) {
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState = serverLevel.getBlockState(blockPos2);
        if (PointedDripstoneBlock.isUnmergedTipWithDirection(blockState, direction.getOpposite())) {
            PointedDripstoneBlock.createMergedTips(blockState, serverLevel, blockPos2);
        } else if (blockState.isAir() || blockState.is(Blocks.WATER)) {
            PointedDripstoneBlock.createDripstone(serverLevel, blockPos2, direction, DripstoneThickness.TIP);
        }
    }

    private static void createDripstone(LevelAccessor levelAccessor, BlockPos blockPos, Direction direction, DripstoneThickness dripstoneThickness) {
        BlockState blockState = (BlockState)((BlockState)((BlockState)Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(TIP_DIRECTION, direction)).setValue(THICKNESS, dripstoneThickness)).setValue(WATERLOGGED, levelAccessor.getFluidState(blockPos).getType() == Fluids.WATER);
        levelAccessor.setBlock(blockPos, blockState, 3);
    }

    private static void createMergedTips(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockPos blockPos3;
        BlockPos blockPos2;
        if (blockState.getValue(TIP_DIRECTION) == Direction.UP) {
            blockPos2 = blockPos;
            blockPos3 = blockPos.above();
        } else {
            blockPos3 = blockPos;
            blockPos2 = blockPos.below();
        }
        PointedDripstoneBlock.createDripstone(levelAccessor, blockPos3, Direction.DOWN, DripstoneThickness.TIP_MERGE);
        PointedDripstoneBlock.createDripstone(levelAccessor, blockPos2, Direction.UP, DripstoneThickness.TIP_MERGE);
    }

    public static void spawnDripParticle(Level level, BlockPos blockPos, BlockState blockState) {
        PointedDripstoneBlock.getFluidAboveStalactite(level, blockPos, blockState).ifPresent(fluidInfo -> PointedDripstoneBlock.spawnDripParticle(level, blockPos, blockState, fluidInfo.fluid));
    }

    private static void spawnDripParticle(Level level, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        Vec3 vec3 = blockState.getOffset(level, blockPos);
        double d = 0.0625;
        double e = (double)blockPos.getX() + 0.5 + vec3.x;
        double f = (double)((float)(blockPos.getY() + 1) - 0.6875f) - 0.0625;
        double g = (double)blockPos.getZ() + 0.5 + vec3.z;
        Fluid fluid2 = PointedDripstoneBlock.getDripFluid(level, fluid);
        SimpleParticleType particleOptions = fluid2.is(FluidTags.LAVA) ? ParticleTypes.DRIPPING_DRIPSTONE_LAVA : ParticleTypes.DRIPPING_DRIPSTONE_WATER;
        level.addParticle(particleOptions, e, f, g, 0.0, 0.0, 0.0);
    }

    @Nullable
    private static BlockPos findTip(BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos2, int i, boolean bl) {
        if (PointedDripstoneBlock.isTip(blockState2, bl)) {
            return blockPos2;
        }
        Direction direction = blockState2.getValue(TIP_DIRECTION);
        BiPredicate<BlockPos, BlockState> biPredicate = (blockPos, blockState) -> blockState.is(Blocks.POINTED_DRIPSTONE) && blockState.getValue(TIP_DIRECTION) == direction;
        return PointedDripstoneBlock.findBlockVertical(levelAccessor, blockPos2, direction.getAxisDirection(), biPredicate, blockState -> PointedDripstoneBlock.isTip(blockState, bl), i).orElse(null);
    }

    @Nullable
    private static Direction calculateTipDirection(LevelReader levelReader, BlockPos blockPos, Direction direction) {
        Direction direction2;
        if (PointedDripstoneBlock.isValidPointedDripstonePlacement(levelReader, blockPos, direction)) {
            direction2 = direction;
        } else if (PointedDripstoneBlock.isValidPointedDripstonePlacement(levelReader, blockPos, direction.getOpposite())) {
            direction2 = direction.getOpposite();
        } else {
            return null;
        }
        return direction2;
    }

    private static DripstoneThickness calculateDripstoneThickness(LevelReader levelReader, BlockPos blockPos, Direction direction, boolean bl) {
        Direction direction2 = direction.getOpposite();
        BlockState blockState = levelReader.getBlockState(blockPos.relative(direction));
        if (PointedDripstoneBlock.isPointedDripstoneWithDirection(blockState, direction2)) {
            if (bl || blockState.getValue(THICKNESS) == DripstoneThickness.TIP_MERGE) {
                return DripstoneThickness.TIP_MERGE;
            }
            return DripstoneThickness.TIP;
        }
        if (!PointedDripstoneBlock.isPointedDripstoneWithDirection(blockState, direction)) {
            return DripstoneThickness.TIP;
        }
        DripstoneThickness dripstoneThickness = blockState.getValue(THICKNESS);
        if (dripstoneThickness == DripstoneThickness.TIP || dripstoneThickness == DripstoneThickness.TIP_MERGE) {
            return DripstoneThickness.FRUSTUM;
        }
        BlockState blockState2 = levelReader.getBlockState(blockPos.relative(direction2));
        if (!PointedDripstoneBlock.isPointedDripstoneWithDirection(blockState2, direction)) {
            return DripstoneThickness.BASE;
        }
        return DripstoneThickness.MIDDLE;
    }

    public static boolean canDrip(BlockState blockState) {
        return PointedDripstoneBlock.isStalactite(blockState) && blockState.getValue(THICKNESS) == DripstoneThickness.TIP && blockState.getValue(WATERLOGGED) == false;
    }

    private static boolean canTipGrow(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos) {
        Direction direction = blockState.getValue(TIP_DIRECTION);
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState2 = serverLevel.getBlockState(blockPos2);
        if (!blockState2.getFluidState().isEmpty()) {
            return false;
        }
        if (blockState2.isAir()) {
            return true;
        }
        return PointedDripstoneBlock.isUnmergedTipWithDirection(blockState2, direction.getOpposite());
    }

    private static Optional<BlockPos> findRootBlock(Level level, BlockPos blockPos2, BlockState blockState2, int i) {
        Direction direction = blockState2.getValue(TIP_DIRECTION);
        BiPredicate<BlockPos, BlockState> biPredicate = (blockPos, blockState) -> blockState.is(Blocks.POINTED_DRIPSTONE) && blockState.getValue(TIP_DIRECTION) == direction;
        return PointedDripstoneBlock.findBlockVertical(level, blockPos2, direction.getOpposite().getAxisDirection(), biPredicate, blockState -> !blockState.is(Blocks.POINTED_DRIPSTONE), i);
    }

    private static boolean isValidPointedDripstonePlacement(LevelReader levelReader, BlockPos blockPos, Direction direction) {
        BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
        BlockState blockState = levelReader.getBlockState(blockPos2);
        return blockState.isFaceSturdy(levelReader, blockPos2, direction) || PointedDripstoneBlock.isPointedDripstoneWithDirection(blockState, direction);
    }

    private static boolean isTip(BlockState blockState, boolean bl) {
        if (!blockState.is(Blocks.POINTED_DRIPSTONE)) {
            return false;
        }
        DripstoneThickness dripstoneThickness = blockState.getValue(THICKNESS);
        return dripstoneThickness == DripstoneThickness.TIP || bl && dripstoneThickness == DripstoneThickness.TIP_MERGE;
    }

    private static boolean isUnmergedTipWithDirection(BlockState blockState, Direction direction) {
        return PointedDripstoneBlock.isTip(blockState, false) && blockState.getValue(TIP_DIRECTION) == direction;
    }

    private static boolean isStalactite(BlockState blockState) {
        return PointedDripstoneBlock.isPointedDripstoneWithDirection(blockState, Direction.DOWN);
    }

    private static boolean isStalagmite(BlockState blockState) {
        return PointedDripstoneBlock.isPointedDripstoneWithDirection(blockState, Direction.UP);
    }

    private static boolean isStalactiteStartPos(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return PointedDripstoneBlock.isStalactite(blockState) && !levelReader.getBlockState(blockPos.above()).is(Blocks.POINTED_DRIPSTONE);
    }

    @Override
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }

    private static boolean isPointedDripstoneWithDirection(BlockState blockState, Direction direction) {
        return blockState.is(Blocks.POINTED_DRIPSTONE) && blockState.getValue(TIP_DIRECTION) == direction;
    }

    @Nullable
    private static BlockPos findFillableCauldronBelowStalactiteTip(Level level, BlockPos blockPos2, Fluid fluid) {
        Predicate<BlockState> predicate = blockState -> blockState.getBlock() instanceof AbstractCauldronBlock && ((AbstractCauldronBlock)blockState.getBlock()).canReceiveStalactiteDrip(fluid);
        BiPredicate<BlockPos, BlockState> biPredicate = (blockPos, blockState) -> PointedDripstoneBlock.canDripThrough(level, blockPos, blockState);
        return PointedDripstoneBlock.findBlockVertical(level, blockPos2, Direction.DOWN.getAxisDirection(), biPredicate, predicate, 11).orElse(null);
    }

    @Nullable
    public static BlockPos findStalactiteTipAboveCauldron(Level level, BlockPos blockPos2) {
        BiPredicate<BlockPos, BlockState> biPredicate = (blockPos, blockState) -> PointedDripstoneBlock.canDripThrough(level, blockPos, blockState);
        return PointedDripstoneBlock.findBlockVertical(level, blockPos2, Direction.UP.getAxisDirection(), biPredicate, PointedDripstoneBlock::canDrip, 11).orElse(null);
    }

    public static Fluid getCauldronFillFluidType(ServerLevel serverLevel, BlockPos blockPos) {
        return PointedDripstoneBlock.getFluidAboveStalactite(serverLevel, blockPos, serverLevel.getBlockState(blockPos)).map(fluidInfo -> fluidInfo.fluid).filter(PointedDripstoneBlock::canFillCauldron).orElse(Fluids.EMPTY);
    }

    private static Optional<FluidInfo> getFluidAboveStalactite(Level level, BlockPos blockPos2, BlockState blockState) {
        if (!PointedDripstoneBlock.isStalactite(blockState)) {
            return Optional.empty();
        }
        return PointedDripstoneBlock.findRootBlock(level, blockPos2, blockState, 11).map(blockPos -> {
            BlockPos blockPos2 = blockPos.above();
            BlockState blockState = level.getBlockState(blockPos2);
            Fluid fluid = blockState.is(Blocks.MUD) && !level.dimensionType().ultraWarm() ? Fluids.WATER : level.getFluidState(blockPos2).getType();
            return new FluidInfo(blockPos2, fluid, blockState);
        });
    }

    private static boolean canFillCauldron(Fluid fluid) {
        return fluid == Fluids.LAVA || fluid == Fluids.WATER;
    }

    private static boolean canGrow(BlockState blockState, BlockState blockState2) {
        return blockState.is(Blocks.DRIPSTONE_BLOCK) && blockState2.is(Blocks.WATER) && blockState2.getFluidState().isSource();
    }

    private static Fluid getDripFluid(Level level, Fluid fluid) {
        if (fluid.isSame(Fluids.EMPTY)) {
            return level.dimensionType().ultraWarm() ? Fluids.LAVA : Fluids.WATER;
        }
        return fluid;
    }

    private static Optional<BlockPos> findBlockVertical(LevelAccessor levelAccessor, BlockPos blockPos, Direction.AxisDirection axisDirection, BiPredicate<BlockPos, BlockState> biPredicate, Predicate<BlockState> predicate, int i) {
        Direction direction = Direction.get(axisDirection, Direction.Axis.Y);
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (int j = 1; j < i; ++j) {
            mutableBlockPos.move(direction);
            BlockState blockState = levelAccessor.getBlockState(mutableBlockPos);
            if (predicate.test(blockState)) {
                return Optional.of(mutableBlockPos.immutable());
            }
            if (!levelAccessor.isOutsideBuildHeight(mutableBlockPos.getY()) && biPredicate.test(mutableBlockPos, blockState)) continue;
            return Optional.empty();
        }
        return Optional.empty();
    }

    private static boolean canDripThrough(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        if (blockState.isAir()) {
            return true;
        }
        if (blockState.isSolidRender(blockGetter, blockPos)) {
            return false;
        }
        if (!blockState.getFluidState().isEmpty()) {
            return false;
        }
        VoxelShape voxelShape = blockState.getCollisionShape(blockGetter, blockPos);
        return !Shapes.joinIsNotEmpty(REQUIRED_SPACE_TO_DRIP_THROUGH_NON_SOLID_BLOCK, voxelShape, BooleanOp.AND);
    }

    record FluidInfo(BlockPos pos, Fluid fluid, BlockState sourceState) {
    }
}

