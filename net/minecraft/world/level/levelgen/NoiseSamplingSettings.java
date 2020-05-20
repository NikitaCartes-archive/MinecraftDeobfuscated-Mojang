/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class NoiseSamplingSettings {
    public static final Codec<NoiseSamplingSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.DOUBLE.fieldOf("xz_scale")).forGetter(NoiseSamplingSettings::xzScale), ((MapCodec)Codec.DOUBLE.fieldOf("y_scale")).forGetter(NoiseSamplingSettings::yScale), ((MapCodec)Codec.DOUBLE.fieldOf("xz_factor")).forGetter(NoiseSamplingSettings::xzFactor), ((MapCodec)Codec.DOUBLE.fieldOf("y_factor")).forGetter(NoiseSamplingSettings::yFactor)).apply((Applicative<NoiseSamplingSettings, ?>)instance, NoiseSamplingSettings::new));
    private final double xzScale;
    private final double yScale;
    private final double xzFactor;
    private final double yFactor;

    public NoiseSamplingSettings(double d, double e, double f, double g) {
        this.xzScale = d;
        this.yScale = e;
        this.xzFactor = f;
        this.yFactor = g;
    }

    public double xzScale() {
        return this.xzScale;
    }

    public double yScale() {
        return this.yScale;
    }

    public double xzFactor() {
        return this.xzFactor;
    }

    public double yFactor() {
        return this.yFactor;
    }
}

