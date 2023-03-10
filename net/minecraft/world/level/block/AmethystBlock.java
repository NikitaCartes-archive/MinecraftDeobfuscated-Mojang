/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AmethystBlock
extends Block {
    public AmethystBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
        if (!level.isClientSide) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            level.playSound(null, blockPos, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 1.0f, 0.5f + level.random.nextFloat() * 1.2f);
            level.playSound(null, blockPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 0.5f + level.random.nextFloat() * 1.2f);
        }
    }
}

