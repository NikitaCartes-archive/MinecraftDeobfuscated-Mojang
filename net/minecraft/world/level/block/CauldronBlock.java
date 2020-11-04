/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CauldronBlock
extends AbstractCauldronBlock {
    public CauldronBlock(BlockBehaviour.Properties properties) {
        super(properties, CauldronInteraction.EMPTY);
    }

    protected static boolean shouldHandleRain(Level level, BlockPos blockPos) {
        if (level.random.nextInt(20) != 1) {
            return false;
        }
        return level.getBiome(blockPos).getTemperature(blockPos) >= 0.15f;
    }

    @Override
    public void handleRain(BlockState blockState, Level level, BlockPos blockPos) {
        if (!CauldronBlock.shouldHandleRain(level, blockPos)) {
            return;
        }
        level.setBlockAndUpdate(blockPos, Blocks.WATER_CAULDRON.defaultBlockState());
    }
}

