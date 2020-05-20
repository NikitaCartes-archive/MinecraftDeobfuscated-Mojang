/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacer;
import net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacerType;

public class SimpleBlockPlacer
extends BlockPlacer {
    public static final Codec<SimpleBlockPlacer> CODEC = Codec.unit(() -> INSTANCE);
    public static final SimpleBlockPlacer INSTANCE = new SimpleBlockPlacer();

    @Override
    protected BlockPlacerType<?> type() {
        return BlockPlacerType.SIMPLE_BLOCK_PLACER;
    }

    @Override
    public void place(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Random random) {
        levelAccessor.setBlock(blockPos, blockState, 2);
    }
}

