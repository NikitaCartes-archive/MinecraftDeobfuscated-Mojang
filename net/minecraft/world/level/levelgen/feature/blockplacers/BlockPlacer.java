/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacerType;

public abstract class BlockPlacer {
    public static final Codec<BlockPlacer> CODEC = Registry.BLOCK_PLACER_TYPES.dispatch(BlockPlacer::type, BlockPlacerType::codec);

    public abstract void place(LevelAccessor var1, BlockPos var2, BlockState var3, Random var4);

    protected abstract BlockPlacerType<?> type();
}

