/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WetSpongeBlock
extends Block {
    protected WetSpongeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (level.dimensionType().ultraWarm()) {
            level.setBlock(blockPos, Blocks.SPONGE.defaultBlockState(), 3);
            level.levelEvent(2009, blockPos, 0);
            level.playSound(null, blockPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, (1.0f + level.getRandom().nextFloat() * 0.2f) * 0.7f);
        }
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        Direction direction = Direction.getRandom(randomSource);
        if (direction == Direction.UP) {
            return;
        }
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState2 = level.getBlockState(blockPos2);
        if (blockState.canOcclude() && blockState2.isFaceSturdy(level, blockPos2, direction.getOpposite())) {
            return;
        }
        double d = blockPos.getX();
        double e = blockPos.getY();
        double f = blockPos.getZ();
        if (direction == Direction.DOWN) {
            e -= 0.05;
            d += randomSource.nextDouble();
            f += randomSource.nextDouble();
        } else {
            e += randomSource.nextDouble() * 0.8;
            if (direction.getAxis() == Direction.Axis.X) {
                f += randomSource.nextDouble();
                d = direction == Direction.EAST ? (d += 1.1) : (d += 0.05);
            } else {
                d += randomSource.nextDouble();
                f = direction == Direction.SOUTH ? (f += 1.1) : (f += 0.05);
            }
        }
        level.addParticle(ParticleTypes.DRIPPING_WATER, d, e, f, 0.0, 0.0, 0.0);
    }
}

