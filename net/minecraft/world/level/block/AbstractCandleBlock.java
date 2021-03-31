/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractCandleBlock
extends Block {
    public static final int LIGHT_PER_CANDLE = 3;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    protected AbstractCandleBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    protected abstract Iterable<Vec3> getParticleOffsets(BlockState var1);

    public static boolean isLit(BlockState blockState) {
        return blockState.hasProperty(LIT) && blockState.is(BlockTags.CANDLES) && blockState.getValue(LIT) != false;
    }

    @Override
    public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
        if (!level.isClientSide && projectile.isOnFire() && this.canBeLit(blockState)) {
            AbstractCandleBlock.setLit(level, blockState, blockHitResult.getBlockPos(), true);
        }
    }

    protected boolean canBeLit(BlockState blockState) {
        return blockState.getValue(LIT) == false;
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if (!blockState.getValue(LIT).booleanValue()) {
            return;
        }
        this.getParticleOffsets(blockState).forEach(vec3 -> AbstractCandleBlock.addParticlesAndSound(level, vec3.add(blockPos.getX(), blockPos.getY(), blockPos.getZ()), random));
    }

    private static void addParticlesAndSound(Level level, Vec3 vec3, Random random) {
        float f = random.nextFloat();
        if (f < 0.3f) {
            level.addParticle(ParticleTypes.SMOKE, vec3.x, vec3.y, vec3.z, 0.0, 0.0, 0.0);
            if (f < 0.17f) {
                level.playLocalSound(vec3.x + 0.5, vec3.y + 0.5, vec3.z + 0.5, SoundEvents.CANDLE_AMBIENT, SoundSource.BLOCKS, 1.0f + random.nextFloat(), random.nextFloat() * 0.7f + 0.3f, false);
            }
        }
        level.addParticle(ParticleTypes.SMALL_FLAME, vec3.x, vec3.y, vec3.z, 0.0, 0.0, 0.0);
    }

    public static void extinguish(@Nullable Player player, BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
        AbstractCandleBlock.setLit(levelAccessor, blockState, blockPos, false);
        if (blockState.getBlock() instanceof AbstractCandleBlock) {
            ((AbstractCandleBlock)blockState.getBlock()).getParticleOffsets(blockState).forEach(vec3 -> levelAccessor.addParticle(ParticleTypes.SMOKE, (double)blockPos.getX() + vec3.x(), (double)blockPos.getY() + vec3.y(), (double)blockPos.getZ() + vec3.z(), 0.0, 0.1f, 0.0));
        }
        levelAccessor.playSound(null, blockPos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
        levelAccessor.gameEvent((Entity)player, GameEvent.BLOCK_CHANGE, blockPos);
    }

    private static void setLit(LevelAccessor levelAccessor, BlockState blockState, BlockPos blockPos, boolean bl) {
        levelAccessor.setBlock(blockPos, (BlockState)blockState.setValue(LIT, bl), 11);
    }
}

