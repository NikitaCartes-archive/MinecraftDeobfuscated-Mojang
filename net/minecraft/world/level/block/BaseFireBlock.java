/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BaseFireBlock
extends Block {
    private final float fireDamage;

    public BaseFireBlock(Block.Properties properties, float f) {
        super(properties);
        this.fireDamage = f;
    }

    public static BlockState getState(BlockGetter blockGetter, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.below();
        BlockState blockState = blockGetter.getBlockState(blockPos2);
        if (blockState.getBlock() == Blocks.SOUL_SOIL) {
            return Blocks.SOUL_FIRE.defaultBlockState();
        }
        return ((FireBlock)Blocks.FIRE).getStateForPlacement(blockGetter, blockPos);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.empty();
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        block12: {
            double f;
            double e;
            double d;
            int i;
            block11: {
                BlockPos blockPos2;
                BlockState blockState2;
                if (random.nextInt(24) == 0) {
                    level.playLocalSound((float)blockPos.getX() + 0.5f, (float)blockPos.getY() + 0.5f, (float)blockPos.getZ() + 0.5f, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0f + random.nextFloat(), random.nextFloat() * 0.7f + 0.3f, false);
                }
                if (!this.canBurn(blockState2 = level.getBlockState(blockPos2 = blockPos.below())) && !blockState2.isFaceSturdy(level, blockPos2, Direction.UP)) break block11;
                for (int i2 = 0; i2 < 3; ++i2) {
                    double d2 = (double)blockPos.getX() + random.nextDouble();
                    double e2 = (double)blockPos.getY() + random.nextDouble() * 0.5 + 0.5;
                    double f2 = (double)blockPos.getZ() + random.nextDouble();
                    level.addParticle(ParticleTypes.LARGE_SMOKE, d2, e2, f2, 0.0, 0.0, 0.0);
                }
                break block12;
            }
            if (this.canBurn(level.getBlockState(blockPos.west()))) {
                for (i = 0; i < 2; ++i) {
                    d = (double)blockPos.getX() + random.nextDouble() * (double)0.1f;
                    e = (double)blockPos.getY() + random.nextDouble();
                    f = (double)blockPos.getZ() + random.nextDouble();
                    level.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
                }
            }
            if (this.canBurn(level.getBlockState(blockPos.east()))) {
                for (i = 0; i < 2; ++i) {
                    d = (double)(blockPos.getX() + 1) - random.nextDouble() * (double)0.1f;
                    e = (double)blockPos.getY() + random.nextDouble();
                    f = (double)blockPos.getZ() + random.nextDouble();
                    level.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
                }
            }
            if (this.canBurn(level.getBlockState(blockPos.north()))) {
                for (i = 0; i < 2; ++i) {
                    d = (double)blockPos.getX() + random.nextDouble();
                    e = (double)blockPos.getY() + random.nextDouble();
                    f = (double)blockPos.getZ() + random.nextDouble() * (double)0.1f;
                    level.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
                }
            }
            if (this.canBurn(level.getBlockState(blockPos.south()))) {
                for (i = 0; i < 2; ++i) {
                    d = (double)blockPos.getX() + random.nextDouble();
                    e = (double)blockPos.getY() + random.nextDouble();
                    f = (double)(blockPos.getZ() + 1) - random.nextDouble() * (double)0.1f;
                    level.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
                }
            }
            if (!this.canBurn(level.getBlockState(blockPos.above()))) break block12;
            for (i = 0; i < 2; ++i) {
                d = (double)blockPos.getX() + random.nextDouble();
                e = (double)(blockPos.getY() + 1) - random.nextDouble() * (double)0.1f;
                f = (double)blockPos.getZ() + random.nextDouble();
                level.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
            }
        }
    }

    protected abstract boolean canBurn(BlockState var1);

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (!(entity.fireImmune() || entity instanceof LivingEntity && EnchantmentHelper.hasFrostWalker((LivingEntity)entity) || entity.isInWaterOrRain())) {
            entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 1);
            if (entity.getRemainingFireTicks() == 0) {
                entity.setSecondsOnFire(8);
            }
            entity.hurt(DamageSource.IN_FIRE, this.fireDamage);
        }
        super.entityInside(blockState, level, blockPos, entity);
    }
}

