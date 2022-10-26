/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class RespawnAnchorBlock
extends Block {
    public static final int MIN_CHARGES = 0;
    public static final int MAX_CHARGES = 4;
    public static final IntegerProperty CHARGE = BlockStateProperties.RESPAWN_ANCHOR_CHARGES;
    private static final ImmutableList<Vec3i> RESPAWN_HORIZONTAL_OFFSETS = ImmutableList.of(new Vec3i(0, 0, -1), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(1, 0, 0), new Vec3i(-1, 0, -1), new Vec3i(1, 0, -1), new Vec3i(-1, 0, 1), new Vec3i(1, 0, 1));
    private static final ImmutableList<Vec3i> RESPAWN_OFFSETS = ((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)((ImmutableList.Builder)new ImmutableList.Builder().addAll(RESPAWN_HORIZONTAL_OFFSETS)).addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vec3i::below).iterator())).addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vec3i::above).iterator())).add(new Vec3i(0, 1, 0))).build();

    public RespawnAnchorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(CHARGE, 0));
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (interactionHand == InteractionHand.MAIN_HAND && !RespawnAnchorBlock.isRespawnFuel(itemStack) && RespawnAnchorBlock.isRespawnFuel(player.getItemInHand(InteractionHand.OFF_HAND))) {
            return InteractionResult.PASS;
        }
        if (RespawnAnchorBlock.isRespawnFuel(itemStack) && RespawnAnchorBlock.canBeCharged(blockState)) {
            RespawnAnchorBlock.charge(level, blockPos, blockState);
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (blockState.getValue(CHARGE) == 0) {
            return InteractionResult.PASS;
        }
        if (RespawnAnchorBlock.canSetSpawn(level)) {
            ServerPlayer serverPlayer;
            if (!(level.isClientSide || (serverPlayer = (ServerPlayer)player).getRespawnDimension() == level.dimension() && blockPos.equals(serverPlayer.getRespawnPosition()))) {
                serverPlayer.setRespawnPosition(level.dimension(), blockPos, 0.0f, false, true);
                level.playSound(null, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.CONSUME;
        }
        if (!level.isClientSide) {
            this.explode(blockState, level, blockPos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean isRespawnFuel(ItemStack itemStack) {
        return itemStack.is(Items.GLOWSTONE);
    }

    private static boolean canBeCharged(BlockState blockState) {
        return blockState.getValue(CHARGE) < 4;
    }

    private static boolean isWaterThatWouldFlow(BlockPos blockPos, Level level) {
        FluidState fluidState = level.getFluidState(blockPos);
        if (!fluidState.is(FluidTags.WATER)) {
            return false;
        }
        if (fluidState.isSource()) {
            return true;
        }
        float f = fluidState.getAmount();
        if (f < 2.0f) {
            return false;
        }
        FluidState fluidState2 = level.getFluidState(blockPos.below());
        return !fluidState2.is(FluidTags.WATER);
    }

    private void explode(BlockState blockState, Level level, final BlockPos blockPos2) {
        level.removeBlock(blockPos2, false);
        boolean bl = Direction.Plane.HORIZONTAL.stream().map(blockPos2::relative).anyMatch(blockPos -> RespawnAnchorBlock.isWaterThatWouldFlow(blockPos, level));
        final boolean bl2 = bl || level.getFluidState(blockPos2.above()).is(FluidTags.WATER);
        ExplosionDamageCalculator explosionDamageCalculator = new ExplosionDamageCalculator(){

            @Override
            public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
                if (blockPos.equals(blockPos2) && bl2) {
                    return Optional.of(Float.valueOf(Blocks.WATER.getExplosionResistance()));
                }
                return super.getBlockExplosionResistance(explosion, blockGetter, blockPos, blockState, fluidState);
            }
        };
        Vec3 vec3 = blockPos2.getCenter();
        level.explode(null, DamageSource.badRespawnPointExplosion(vec3), explosionDamageCalculator, vec3, 5.0f, true, Explosion.BlockInteraction.DESTROY);
    }

    public static boolean canSetSpawn(Level level) {
        return level.dimensionType().respawnAnchorWorks();
    }

    public static void charge(Level level, BlockPos blockPos, BlockState blockState) {
        level.setBlock(blockPos, (BlockState)blockState.setValue(CHARGE, blockState.getValue(CHARGE) + 1), 3);
        level.playSound(null, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (blockState.getValue(CHARGE) == 0) {
            return;
        }
        if (randomSource.nextInt(100) == 0) {
            level.playSound(null, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, SoundEvents.RESPAWN_ANCHOR_AMBIENT, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        double d = (double)blockPos.getX() + 0.5 + (0.5 - randomSource.nextDouble());
        double e = (double)blockPos.getY() + 1.0;
        double f = (double)blockPos.getZ() + 0.5 + (0.5 - randomSource.nextDouble());
        double g = (double)randomSource.nextFloat() * 0.04;
        level.addParticle(ParticleTypes.REVERSE_PORTAL, d, e, f, 0.0, g, 0.0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    public static int getScaledChargeLevel(BlockState blockState, int i) {
        return Mth.floor((float)(blockState.getValue(CHARGE) - 0) / 4.0f * (float)i);
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return RespawnAnchorBlock.getScaledChargeLevel(blockState, 15);
    }

    public static Optional<Vec3> findStandUpPosition(EntityType<?> entityType, CollisionGetter collisionGetter, BlockPos blockPos) {
        Optional<Vec3> optional = RespawnAnchorBlock.findStandUpPosition(entityType, collisionGetter, blockPos, true);
        if (optional.isPresent()) {
            return optional;
        }
        return RespawnAnchorBlock.findStandUpPosition(entityType, collisionGetter, blockPos, false);
    }

    private static Optional<Vec3> findStandUpPosition(EntityType<?> entityType, CollisionGetter collisionGetter, BlockPos blockPos, boolean bl) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Vec3i vec3i : RESPAWN_OFFSETS) {
            mutableBlockPos.set(blockPos).move(vec3i);
            Vec3 vec3 = DismountHelper.findSafeDismountLocation(entityType, collisionGetter, mutableBlockPos, bl);
            if (vec3 == null) continue;
            return Optional.of(vec3);
        }
        return Optional.empty();
    }

    @Override
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }
}

