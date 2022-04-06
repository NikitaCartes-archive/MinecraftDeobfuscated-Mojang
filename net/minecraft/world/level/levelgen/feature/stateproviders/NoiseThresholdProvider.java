/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseBasedStateProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseThresholdProvider
extends NoiseBasedStateProvider {
    public static final Codec<NoiseThresholdProvider> CODEC = RecordCodecBuilder.create(instance -> NoiseThresholdProvider.noiseCodec(instance).and(instance.group(((MapCodec)Codec.floatRange(-1.0f, 1.0f).fieldOf("threshold")).forGetter(noiseThresholdProvider -> Float.valueOf(noiseThresholdProvider.threshold)), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("high_chance")).forGetter(noiseThresholdProvider -> Float.valueOf(noiseThresholdProvider.highChance)), ((MapCodec)BlockState.CODEC.fieldOf("default_state")).forGetter(noiseThresholdProvider -> noiseThresholdProvider.defaultState), ((MapCodec)Codec.list(BlockState.CODEC).fieldOf("low_states")).forGetter(noiseThresholdProvider -> noiseThresholdProvider.lowStates), ((MapCodec)Codec.list(BlockState.CODEC).fieldOf("high_states")).forGetter(noiseThresholdProvider -> noiseThresholdProvider.highStates))).apply((Applicative<NoiseThresholdProvider, ?>)instance, NoiseThresholdProvider::new));
    private final float threshold;
    private final float highChance;
    private final BlockState defaultState;
    private final List<BlockState> lowStates;
    private final List<BlockState> highStates;

    public NoiseThresholdProvider(long l, NormalNoise.NoiseParameters noiseParameters, float f, float g, float h, BlockState blockState, List<BlockState> list, List<BlockState> list2) {
        super(l, noiseParameters, f);
        this.threshold = g;
        this.highChance = h;
        this.defaultState = blockState;
        this.lowStates = list;
        this.highStates = list2;
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.NOISE_THRESHOLD_PROVIDER;
    }

    @Override
    public BlockState getState(RandomSource randomSource, BlockPos blockPos) {
        double d = this.getNoiseValue(blockPos, this.scale);
        if (d < (double)this.threshold) {
            return Util.getRandom(this.lowStates, randomSource);
        }
        if (randomSource.nextFloat() < this.highChance) {
            return Util.getRandom(this.highStates, randomSource);
        }
        return this.defaultState;
    }
}

