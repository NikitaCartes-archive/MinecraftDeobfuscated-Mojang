/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public abstract class BlockStateProvider {
    public static final Codec<BlockStateProvider> CODEC = Registry.BLOCKSTATE_PROVIDER_TYPES.dispatch(BlockStateProvider::type, BlockStateProviderType::codec);

    protected abstract BlockStateProviderType<?> type();

    public abstract BlockState getState(Random var1, BlockPos var2);
}

