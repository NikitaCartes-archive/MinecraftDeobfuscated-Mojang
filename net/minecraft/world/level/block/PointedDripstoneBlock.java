/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
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
    private static final VoxelShape TIP_MERGE_SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape TIP_SHAPE_UP = Block.box(5.0, 0.0, 5.0, 11.0, 11.0, 11.0);
    private static final VoxelShape TIP_SHAPE_DOWN = Block.box(5.0, 5.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape FRUSTUM_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
    private static final VoxelShape MIDDLE_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
    private static final VoxelShape BASE_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

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
        boolean bl;
        DripstoneThickness dripstoneThickness;
        Direction direction2;
        if (direction != Direction.UP && direction != Direction.DOWN) {
            return blockState;
        }
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        if (direction == (direction2 = blockState.getValue(TIP_DIRECTION)).getOpposite() && !PointedDripstoneBlock.isValidPointedDripstonePlacement(levelAccessor, blockPos, direction2)) {
            if (direction2 == Direction.DOWN) {
                this.scheduleStalactiteFallTicks(blockState, levelAccessor, blockPos);
                return blockState;
            }
            levelAccessor.destroyBlock(blockPos, true);
            return blockState;
        }
        if (direction2 != PointedDripstoneBlock.calculateTipDirection(levelAccessor, blockPos, direction2)) {
            levelAccessor.destroyBlock(blockPos, true);
        }
        if ((dripstoneThickness = PointedDripstoneBlock.calculateDripstoneThickness(levelAccessor, blockPos, direction2, bl = blockState.getValue(THICKNESS) == DripstoneThickness.TIP_MERGE)) == null) {
            levelAccessor.destroyBlock(blockPos, true);
            return blockState;
        }
        return (BlockState)blockState.setValue(THICKNESS, dripstoneThickness);
    }

    @Override
    public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
        if (projectile instanceof ThrownTrident && projectile.getDeltaMovement().length() > 0.6) {
            level.destroyBlock(blockHitResult.getBlockPos(), true);
        }
    }

    @Override
    public void fallOn(Level level, BlockPos blockPos, Entity entity, float f) {
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.getValue(TIP_DIRECTION) == Direction.UP && blockState.getValue(THICKNESS) == DripstoneThickness.TIP) {
            entity.causeFallDamage(f * 4.0f, 1.0f);
        }
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        float f = random.nextFloat();
        if (f < 0.02f && PointedDripstoneBlock.canDrip(blockState)) {
            PointedDripstoneBlock.spawnDripParticle(level, blockPos, blockState);
            return;
        }
        if (f < 0.12f && PointedDripstoneBlock.canDrip(blockState) && PointedDripstoneBlock.isUnderLiquidSource(level, blockPos)) {
            PointedDripstoneBlock.spawnDripParticle(level, blockPos, blockState);
        }
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        PointedDripstoneBlock.spawnFallingStalactite(blockState, serverLevel, blockPos);
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        PointedDripstoneBlock.maybeFillCauldron(blockState, serverLevel, blockPos, random.nextFloat());
    }

    @VisibleForTesting
    public static void maybeFillCauldron(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, float f) {
        if (f > 0.29296875f && f > 0.17578125f) {
            return;
        }
        if (!PointedDripstoneBlock.isStalactiteStartPos(blockState, serverLevel, blockPos)) {
            return;
        }
        Fluid fluid = PointedDripstoneBlock.getCauldronFillFluidType(serverLevel, blockPos);
        if (fluid == Fluids.EMPTY) {
            return;
        }
        BlockPos blockPos2 = PointedDripstoneBlock.findTip(blockState, serverLevel, blockPos);
        if (blockPos2 == null) {
            return;
        }
        BlockPos blockPos3 = null;
        if (fluid == Fluids.WATER && f < 0.29296875f || fluid == Fluids.LAVA && f < 0.17578125f) {
            blockPos3 = PointedDripstoneBlock.findFillableCauldronBelowStalactiteTip(serverLevel, blockPos, fluid);
        }
        if (blockPos3 == null) {
            return;
        }
        serverLevel.levelEvent(1504, blockPos2, 0);
        int i = blockPos2.getY() - blockPos3.getY();
        int j = 50 + i;
        BlockState blockState2 = serverLevel.getBlockState(blockPos3);
        serverLevel.getBlockTicks().scheduleTick(blockPos3, blockState2.getBlock(), j);
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
    public BlockBehaviour.OffsetType getOffsetType() {
        return BlockBehaviour.OffsetType.XZ;
    }

    @Override
    public void onBrokenAfterFall(Level level, BlockPos blockPos, FallingBlockEntity fallingBlockEntity) {
        if (!fallingBlockEntity.isSilent()) {
            level.levelEvent(1045, blockPos, 0);
        }
    }

    private void scheduleStalactiteFallTicks(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockPos blockPos2 = PointedDripstoneBlock.findTip(blockState, levelAccessor, blockPos);
        if (blockPos2 == null) {
            return;
        }
        BlockPos.MutableBlockPos mutableBlockPos = blockPos2.mutable();
        while (PointedDripstoneBlock.isStalactite(levelAccessor.getBlockState(mutableBlockPos))) {
            levelAccessor.getBlockTicks().scheduleTick(mutableBlockPos, this, 2);
            mutableBlockPos.move(Direction.UP);
        }
    }

    private static void spawnFallingStalactite(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos) {
        Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
        FallingBlockEntity fallingBlockEntity = new FallingBlockEntity(serverLevel, vec3.x, vec3.y, vec3.z, blockState);
        fallingBlockEntity.setHurtsEntities(true);
        fallingBlockEntity.dropItem = true;
        serverLevel.addFreshEntity(fallingBlockEntity);
    }

    @Environment(value=EnvType.CLIENT)
    public static void spawnDripParticle(Level level, BlockPos blockPos, BlockState blockState) {
        DripType dripType = PointedDripstoneBlock.getDripType(level, blockPos, blockState);
        if (dripType == null) {
            return;
        }
        Vec3 vec3 = blockState.getOffset(level, blockPos);
        double d = 0.0625;
        double e = (double)blockPos.getX() + 0.5 + vec3.x;
        double f = (double)((float)(blockPos.getY() + 1) - 0.6875f) - 0.0625;
        double g = (double)blockPos.getZ() + 0.5 + vec3.z;
        level.addParticle(dripType.getDripParticle(), e, f, g, 0.0, 0.0, 0.0);
    }

    @Nullable
    private static BlockPos findTip(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
        Direction direction = blockState.getValue(TIP_DIRECTION);
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        BlockState blockState2 = blockState;
        for (int i = 0; i < 50; ++i) {
            if (PointedDripstoneBlock.isTip(blockState2)) {
                return mutableBlockPos;
            }
            if (!blockState2.is(Blocks.POINTED_DRIPSTONE) || blockState2.getValue(TIP_DIRECTION) != direction) {
                return null;
            }
            mutableBlockPos.move(direction);
            blockState2 = levelAccessor.getBlockState(mutableBlockPos);
        }
        return null;
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

    @Nullable
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
        if (dripstoneThickness == DripstoneThickness.FRUSTUM || dripstoneThickness == DripstoneThickness.MIDDLE) {
            if (PointedDripstoneBlock.isPointedDripstoneWithDirection(blockState2, direction)) {
                return DripstoneThickness.MIDDLE;
            }
            return DripstoneThickness.BASE;
        }
        return null;
    }

    public static boolean canDrip(BlockState blockState) {
        return PointedDripstoneBlock.isStalactite(blockState) && blockState.getValue(THICKNESS) == DripstoneThickness.TIP && blockState.getValue(WATERLOGGED) == false;
    }

    @Nullable
    private static BlockPos findRootBlock(Level level, BlockPos blockPos, BlockState blockState) {
        Direction direction = blockState.getValue(TIP_DIRECTION).getOpposite();
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (int i = 0; i < 50; ++i) {
            mutableBlockPos.move(direction);
            BlockState blockState2 = level.getBlockState(mutableBlockPos);
            if (blockState2.is(Blocks.POINTED_DRIPSTONE)) continue;
            return mutableBlockPos;
        }
        return null;
    }

    private static boolean isValidPointedDripstonePlacement(LevelReader levelReader, BlockPos blockPos, Direction direction) {
        BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
        BlockState blockState = levelReader.getBlockState(blockPos2);
        return Block.canSupportCenter(levelReader, blockPos.relative(direction.getOpposite()), direction) || PointedDripstoneBlock.isPointedDripstoneWithDirection(blockState, direction);
    }

    private static boolean isTip(BlockState blockState) {
        if (!blockState.is(Blocks.POINTED_DRIPSTONE)) {
            return false;
        }
        DripstoneThickness dripstoneThickness = blockState.getValue(THICKNESS);
        return dripstoneThickness == DripstoneThickness.TIP || dripstoneThickness == DripstoneThickness.TIP_MERGE;
    }

    private static boolean isStalactite(BlockState blockState) {
        return PointedDripstoneBlock.isPointedDripstoneWithDirection(blockState, Direction.DOWN);
    }

    private static boolean isStalactiteStartPos(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return PointedDripstoneBlock.isStalactite(blockState) && !levelReader.getBlockState(blockPos.above()).is(Blocks.POINTED_DRIPSTONE);
    }

    private static boolean isPointedDripstoneWithDirection(BlockState blockState, Direction direction) {
        return blockState.is(Blocks.POINTED_DRIPSTONE) && blockState.getValue(TIP_DIRECTION) == direction;
    }

    @Environment(value=EnvType.CLIENT)
    private static boolean isUnderLiquidSource(Level level, BlockPos blockPos) {
        return level.getBlockState(blockPos.above(2)).getFluidState().isSource();
    }

    @Nullable
    private static BlockPos findFillableCauldronBelowStalactiteTip(Level level, BlockPos blockPos, Fluid fluid) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (int i = 0; i < 10; ++i) {
            AbstractCauldronBlock abstractCauldronBlock;
            mutableBlockPos.move(Direction.DOWN);
            BlockState blockState = level.getBlockState(mutableBlockPos);
            if (blockState.isAir()) continue;
            if (blockState.getBlock() instanceof AbstractCauldronBlock && (abstractCauldronBlock = (AbstractCauldronBlock)blockState.getBlock()).canReceiveStalactiteDrip(fluid)) {
                return mutableBlockPos;
            }
            return null;
        }
        return null;
    }

    @Nullable
    public static BlockPos findStalactiteTipAboveCauldron(Level level, BlockPos blockPos) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (int i = 0; i < 10; ++i) {
            mutableBlockPos.move(Direction.UP);
            BlockState blockState = level.getBlockState(mutableBlockPos);
            if (blockState.isAir()) continue;
            if (PointedDripstoneBlock.canDrip(blockState)) {
                return mutableBlockPos;
            }
            return null;
        }
        return null;
    }

    public static Fluid getCauldronFillFluidType(Level level, BlockPos blockPos) {
        DripType dripType = PointedDripstoneBlock.getDripType(level, blockPos, level.getBlockState(blockPos));
        if (dripType != null && dripType.canFillCauldron()) {
            return dripType.getDripFluid();
        }
        return Fluids.EMPTY;
    }

    @Nullable
    private static DripType getDripType(Level level, BlockPos blockPos, BlockState blockState) {
        if (!PointedDripstoneBlock.isStalactite(blockState)) {
            return null;
        }
        BlockPos blockPos2 = PointedDripstoneBlock.findRootBlock(level, blockPos, blockState);
        if (blockPos2 == null) {
            return null;
        }
        FluidState fluidState = level.getFluidState(blockPos2.above());
        return new DripType(fluidState.getType());
    }

    static class DripType {
        private final Fluid fluidAboveRootBlock;

        DripType(Fluid fluid) {
            this.fluidAboveRootBlock = fluid;
        }

        Fluid getDripFluid() {
            return this.fluidAboveRootBlock.is(FluidTags.LAVA) ? Fluids.LAVA : Fluids.WATER;
        }

        boolean canFillCauldron() {
            return this.fluidAboveRootBlock == Fluids.LAVA || this.fluidAboveRootBlock == Fluids.WATER;
        }

        @Environment(value=EnvType.CLIENT)
        ParticleOptions getDripParticle() {
            return this.getDripFluid() == Fluids.WATER ? ParticleTypes.DRIPPING_DRIPSTONE_WATER : ParticleTypes.DRIPPING_DRIPSTONE_LAVA;
        }
    }
}

