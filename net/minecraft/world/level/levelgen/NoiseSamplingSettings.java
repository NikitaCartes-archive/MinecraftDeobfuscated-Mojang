/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record NoiseSamplingSettings(double xzScale, double yScale, double xzFactor, double yFactor) {
    private static final Codec<Double> SCALE_RANGE = Codec.doubleRange(0.001, 1000.0);
    public static final Codec<NoiseSamplingSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)SCALE_RANGE.fieldOf("xz_scale")).forGetter(NoiseSamplingSettings::xzScale), ((MapCodec)SCALE_RANGE.fieldOf("y_scale")).forGetter(NoiseSamplingSettings::yScale), ((MapCodec)SCALE_RANGE.fieldOf("xz_factor")).forGetter(NoiseSamplingSettings::xzFactor), ((MapCodec)SCALE_RANGE.fieldOf("y_factor")).forGetter(NoiseSamplingSettings::yFactor)).apply((Applicative<NoiseSamplingSettings, ?>)instance, NoiseSamplingSettings::new));
}

