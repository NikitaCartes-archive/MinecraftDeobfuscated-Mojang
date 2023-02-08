/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BaseFireBlock
extends Block {
    private static final int SECONDS_ON_FIRE = 8;
    private final float fireDamage;
    protected static final float AABB_OFFSET = 1.0f;
    protected static final VoxelShape DOWN_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

    public BaseFireBlock(BlockBehaviour.Properties properties, float f) {
        super(properties);
        this.fireDamage = f;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return BaseFireBlock.getState(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
    }

    public static BlockState getState(BlockGetter blockGetter, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.below();
        BlockState blockState = blockGetter.getBlockState(blockPos2);
        if (SoulFireBlock.canSurviveOnBlock(blockState)) {
            return Blocks.SOUL_FIRE.defaultBlockState();
        }
        return ((FireBlock)Blocks.FIRE).getStateForPlacement(blockGetter, blockPos);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return DOWN_AABB;
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        block12: {
            double f;
            double e;
            double d;
            int i;
            block11: {
                BlockPos blockPos2;
                BlockState blockState2;
                if (randomSource.nextInt(24) == 0) {
                    level.playLocalSound((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0f + randomSource.nextFloat(), randomSource.nextFloat() * 0.7f + 0.3f, false);
                }
                if (!this.canBurn(blockState2 = level.getBlockState(blockPos2 = blockPos.below())) && !blockState2.isFaceSturdy(level, blockPos2, Direction.UP)) break block11;
                for (int i2 = 0; i2 < 3; ++i2) {
                    double d2 = (double)blockPos.getX() + randomSource.nextDouble();
                    double e2 = (double)blockPos.getY() + randomSource.nextDouble() * 0.5 + 0.5;
                    double f2 = (double)blockPos.getZ() + randomSource.nextDouble();
                    level.addParticle(ParticleTypes.LARGE_SMOKE, d2, e2, f2, 0.0, 0.0, 0.0);
                }
                break block12;
            }
            if (this.canBurn(level.getBlockState(blockPos.west()))) {
                for (i = 0; i < 2; ++i) {
                    d = (double)blockPos.getX() + randomSource.nextDouble() * (double)0.1f;
                    e = (double)blockPos.getY() + randomSource.nextDouble();
                    f = (double)blockPos.getZ() + randomSource.nextDouble();
                    level.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
                }
            }
            if (this.canBurn(level.getBlockState(blockPos.east()))) {
                for (i = 0; i < 2; ++i) {
                    d = (double)(blockPos.getX() + 1) - randomSource.nextDouble() * (double)0.1f;
                    e = (double)blockPos.getY() + randomSource.nextDouble();
                    f = (double)blockPos.getZ() + randomSource.nextDouble();
                    level.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
                }
            }
            if (this.canBurn(level.getBlockState(blockPos.north()))) {
                for (i = 0; i < 2; ++i) {
                    d = (double)blockPos.getX() + randomSource.nextDouble();
                    e = (double)blockPos.getY() + randomSource.nextDouble();
                    f = (double)blockPos.getZ() + randomSource.nextDouble() * (double)0.1f;
                    level.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
                }
            }
            if (this.canBurn(level.getBlockState(blockPos.south()))) {
                for (i = 0; i < 2; ++i) {
                    d = (double)blockPos.getX() + randomSource.nextDouble();
                    e = (double)blockPos.getY() + randomSource.nextDouble();
                    f = (double)(blockPos.getZ() + 1) - randomSource.nextDouble() * (double)0.1f;
                    level.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
                }
            }
            if (!this.canBurn(level.getBlockState(blockPos.above()))) break block12;
            for (i = 0; i < 2; ++i) {
                d = (double)blockPos.getX() + randomSource.nextDouble();
                e = (double)(blockPos.getY() + 1) - randomSource.nextDouble() * (double)0.1f;
                f = (double)blockPos.getZ() + randomSource.nextDouble();
                level.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
            }
        }
    }

    protected abstract boolean canBurn(BlockState var1);

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (!entity.fireImmune()) {
            entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 1);
            if (entity.getRemainingFireTicks() == 0) {
                entity.setSecondsOnFire(8);
            }
        }
        entity.hurt(level.damageSources().inFire(), this.fireDamage);
        super.entityInside(blockState, level, blockPos, entity);
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        Optional<PortalShape> optional;
        if (blockState2.is(blockState.getBlock())) {
            return;
        }
        if (BaseFireBlock.inPortalDimension(level) && (optional = PortalShape.findEmptyPortalShape(level, blockPos, Direction.Axis.X)).isPresent()) {
            optional.get().createPortalBlocks();
            return;
        }
        if (!blockState.canSurvive(level, blockPos)) {
            level.removeBlock(blockPos, false);
        }
    }

    private static boolean inPortalDimension(Level level) {
        return level.dimension() == Level.OVERWORLD || level.dimension() == Level.NETHER;
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos blockPos, BlockState blockState) {
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if (!level.isClientSide()) {
            level.levelEvent(null, 1009, blockPos, 0);
        }
        super.playerWillDestroy(level, blockPos, blockState, player);
    }

    public static boolean canBePlacedAt(Level level, BlockPos blockPos, Direction direction) {
        BlockState blockState = level.getBlockState(blockPos);
        if (!blockState.isAir()) {
            return false;
        }
        return BaseFireBlock.getState(level, blockPos).canSurvive(level, blockPos) || BaseFireBlock.isPortal(level, blockPos, direction);
    }

    private static boolean isPortal(Level level, BlockPos blockPos, Direction direction) {
        if (!BaseFireBlock.inPortalDimension(level)) {
            return false;
        }
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        boolean bl = false;
        for (Direction direction2 : Direction.values()) {
            if (!level.getBlockState(mutableBlockPos.set(blockPos).move(direction2)).is(Blocks.OBSIDIAN)) continue;
            bl = true;
            break;
        }
        if (!bl) {
            return false;
        }
        Direction.Axis axis = direction.getAxis().isHorizontal() ? direction.getCounterClockWise().getAxis() : Direction.Plane.HORIZONTAL.getRandomAxis(level.random);
        return PortalShape.findEmptyPortalShape(level, blockPos, axis).isPresent();
    }
}

