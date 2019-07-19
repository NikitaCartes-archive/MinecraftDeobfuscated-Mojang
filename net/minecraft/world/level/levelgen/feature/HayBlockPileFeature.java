/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.BlockPileFeature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;

public class HayBlockPileFeature
extends BlockPileFeature {
    public HayBlockPileFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
        super(function);
    }

    @Override
    protected BlockState getBlockState(LevelAccessor levelAccessor) {
        Direction.Axis axis = Direction.Axis.getRandomAxis(levelAccessor.getRandom());
        return (BlockState)Blocks.HAY_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }
}

