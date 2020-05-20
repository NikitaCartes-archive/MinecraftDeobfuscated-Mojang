/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public class SimpleStateProvider
extends BlockStateProvider {
    public static final Codec<SimpleStateProvider> CODEC = ((MapCodec)BlockState.CODEC.fieldOf("state")).xmap(SimpleStateProvider::new, simpleStateProvider -> simpleStateProvider.state).codec();
    private final BlockState state;

    public SimpleStateProvider(BlockState blockState) {
        this.state = blockState;
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.SIMPLE_STATE_PROVIDER;
    }

    @Override
    public BlockState getState(Random random, BlockPos blockPos) {
        return this.state;
    }
}

