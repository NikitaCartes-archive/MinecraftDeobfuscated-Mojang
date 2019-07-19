/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.BlockPileFeature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;

public class SnowBlockPileFeature
extends BlockPileFeature {
    public SnowBlockPileFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
        super(function);
    }

    @Override
    protected BlockState getBlockState(LevelAccessor levelAccessor) {
        return Blocks.SNOW_BLOCK.defaultBlockState();
    }
}

