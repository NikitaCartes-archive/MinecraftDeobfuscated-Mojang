/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.behavior.WeightedList;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public class WeightedStateProvider
extends BlockStateProvider {
    public static final Codec<WeightedStateProvider> CODEC = ((MapCodec)WeightedList.codec(BlockState.CODEC).comapFlatMap(WeightedStateProvider::create, weightedStateProvider -> weightedStateProvider.weightedList).fieldOf("entries")).codec();
    private final WeightedList<BlockState> weightedList;

    private static DataResult<WeightedStateProvider> create(WeightedList<BlockState> weightedList) {
        if (weightedList.isEmpty()) {
            return DataResult.error("WeightedStateProvider with no states");
        }
        return DataResult.success(new WeightedStateProvider(weightedList));
    }

    private WeightedStateProvider(WeightedList<BlockState> weightedList) {
        this.weightedList = weightedList;
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.WEIGHTED_STATE_PROVIDER;
    }

    public WeightedStateProvider() {
        this(new WeightedList<BlockState>());
    }

    public WeightedStateProvider add(BlockState blockState, int i) {
        this.weightedList.add(blockState, i);
        return this;
    }

    @Override
    public BlockState getState(Random random, BlockPos blockPos) {
        return this.weightedList.getOne(random);
    }
}

