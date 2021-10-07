/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class DualNoiseProvider
extends NoiseProvider {
    public static final Codec<DualNoiseProvider> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)InclusiveRange.codec(Codec.INT, 1, 64).fieldOf("variety")).forGetter(dualNoiseProvider -> dualNoiseProvider.variety), ((MapCodec)NormalNoise.NoiseParameters.CODEC.fieldOf("slow_noise")).forGetter(dualNoiseProvider -> dualNoiseProvider.slowNoiseParameters), ((MapCodec)ExtraCodecs.POSITIVE_FLOAT.fieldOf("slow_scale")).forGetter(dualNoiseProvider -> Float.valueOf(dualNoiseProvider.slowScale))).and(DualNoiseProvider.noiseProviderCodec(instance)).apply(instance, DualNoiseProvider::new));
    private final InclusiveRange<Integer> variety;
    private final NormalNoise.NoiseParameters slowNoiseParameters;
    private final float slowScale;
    private final NormalNoise slowNoise;

    public DualNoiseProvider(InclusiveRange<Integer> inclusiveRange, NormalNoise.NoiseParameters noiseParameters, float f, long l, NormalNoise.NoiseParameters noiseParameters2, float g, List<BlockState> list) {
        super(l, noiseParameters2, g, list);
        this.variety = inclusiveRange;
        this.slowNoiseParameters = noiseParameters;
        this.slowScale = f;
        this.slowNoise = NormalNoise.create(new WorldgenRandom(new LegacyRandomSource(l)), noiseParameters);
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.DUAL_NOISE_PROVIDER;
    }

    @Override
    public BlockState getState(Random random, BlockPos blockPos) {
        double d = this.getSlowNoiseValue(blockPos);
        int i = (int)Mth.clampedMap(d, -1.0, 1.0, (double)((Integer)this.variety.minInclusive()).intValue(), (double)((Integer)this.variety.maxInclusive() + 1));
        ArrayList<BlockState> list = Lists.newArrayListWithCapacity(i);
        for (int j = 0; j < i; ++j) {
            list.add(this.getRandomState(this.states, this.getSlowNoiseValue(blockPos.offset(j * 54545, 0, j * 34234))));
        }
        return this.getRandomState(list, blockPos, this.scale);
    }

    protected double getSlowNoiseValue(BlockPos blockPos) {
        return this.slowNoise.getValue((float)blockPos.getX() * this.slowScale, (float)blockPos.getY() * this.slowScale, (float)blockPos.getZ() * this.slowScale);
    }
}

