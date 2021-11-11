/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;

public class NoiseThresholdCountPlacement
extends RepeatingPlacement {
    public static final Codec<NoiseThresholdCountPlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.DOUBLE.fieldOf("noise_level")).forGetter(noiseThresholdCountPlacement -> noiseThresholdCountPlacement.noiseLevel), ((MapCodec)Codec.INT.fieldOf("below_noise")).forGetter(noiseThresholdCountPlacement -> noiseThresholdCountPlacement.belowNoise), ((MapCodec)Codec.INT.fieldOf("above_noise")).forGetter(noiseThresholdCountPlacement -> noiseThresholdCountPlacement.aboveNoise)).apply((Applicative<NoiseThresholdCountPlacement, ?>)instance, NoiseThresholdCountPlacement::new));
    private final double noiseLevel;
    private final int belowNoise;
    private final int aboveNoise;

    private NoiseThresholdCountPlacement(double d, int i, int j) {
        this.noiseLevel = d;
        this.belowNoise = i;
        this.aboveNoise = j;
    }

    public static NoiseThresholdCountPlacement of(double d, int i, int j) {
        return new NoiseThresholdCountPlacement(d, i, j);
    }

    @Override
    protected int count(Random random, BlockPos blockPos) {
        double d = Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() / 200.0, (double)blockPos.getZ() / 200.0, false);
        return d < this.noiseLevel ? this.belowNoise : this.aboveNoise;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.NOISE_THRESHOLD_COUNT;
    }
}

