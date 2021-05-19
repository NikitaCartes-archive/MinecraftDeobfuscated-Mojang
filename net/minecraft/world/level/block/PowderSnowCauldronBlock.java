/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class PowderSnowCauldronBlock
extends LayeredCauldronBlock {
    public PowderSnowCauldronBlock(BlockBehaviour.Properties properties, Predicate<Biome.Precipitation> predicate, Map<Item, CauldronInteraction> map) {
        super(properties, predicate, map);
    }

    @Override
    protected void handleEntityOnFireInside(BlockState blockState, Level level, BlockPos blockPos) {
        PowderSnowCauldronBlock.lowerFillLevel((BlockState)Blocks.WATER_CAULDRON.defaultBlockState().setValue(LEVEL, blockState.getValue(LEVEL)), level, blockPos);
    }
}

