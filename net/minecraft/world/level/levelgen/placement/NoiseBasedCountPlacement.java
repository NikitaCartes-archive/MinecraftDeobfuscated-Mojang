/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;

public class NoiseBasedCountPlacement
extends RepeatingPlacement {
    public static final Codec<NoiseBasedCountPlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("noise_to_count_ratio")).forGetter(noiseBasedCountPlacement -> noiseBasedCountPlacement.noiseToCountRatio), ((MapCodec)Codec.DOUBLE.fieldOf("noise_factor")).forGetter(noiseBasedCountPlacement -> noiseBasedCountPlacement.noiseFactor), ((MapCodec)Codec.DOUBLE.fieldOf("noise_offset")).orElse(0.0).forGetter(noiseBasedCountPlacement -> noiseBasedCountPlacement.noiseOffset)).apply((Applicative<NoiseBasedCountPlacement, ?>)instance, NoiseBasedCountPlacement::new));
    private final int noiseToCountRatio;
    private final double noiseFactor;
    private final double noiseOffset;

    private NoiseBasedCountPlacement(int i, double d, double e) {
        this.noiseToCountRatio = i;
        this.noiseFactor = d;
        this.noiseOffset = e;
    }

    public static NoiseBasedCountPlacement of(int i, double d, double e) {
        return new NoiseBasedCountPlacement(i, d, e);
    }

    @Override
    protected int count(RandomSource randomSource, BlockPos blockPos) {
        double d = Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() / this.noiseFactor, (double)blockPos.getZ() / this.noiseFactor, false);
        return (int)Math.ceil((d + this.noiseOffset) * (double)this.noiseToCountRatio);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.NOISE_BASED_COUNT;
    }
}

