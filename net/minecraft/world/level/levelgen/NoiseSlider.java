/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

public class NoiseSlider {
    public static final Codec<NoiseSlider> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.DOUBLE.fieldOf("target")).forGetter(noiseSlider -> noiseSlider.target), ((MapCodec)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("size")).forGetter(noiseSlider -> noiseSlider.size), ((MapCodec)Codec.INT.fieldOf("offset")).forGetter(noiseSlider -> noiseSlider.offset)).apply((Applicative<NoiseSlider, ?>)instance, NoiseSlider::new));
    private final double target;
    private final int size;
    private final int offset;

    public NoiseSlider(double d, int i, int j) {
        this.target = d;
        this.size = i;
        this.offset = j;
    }

    public double applySlide(double d, int i) {
        if (this.size <= 0) {
            return d;
        }
        double e = (double)(i - this.offset) / (double)this.size;
        return Mth.clampedLerp(this.target, d, e);
    }
}

