/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import org.slf4j.Logger;

public class TrapezoidHeight
extends HeightProvider {
    public static final Codec<TrapezoidHeight> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)VerticalAnchor.CODEC.fieldOf("min_inclusive")).forGetter(trapezoidHeight -> trapezoidHeight.minInclusive), ((MapCodec)VerticalAnchor.CODEC.fieldOf("max_inclusive")).forGetter(trapezoidHeight -> trapezoidHeight.maxInclusive), Codec.INT.optionalFieldOf("plateau", 0).forGetter(trapezoidHeight -> trapezoidHeight.plateau)).apply((Applicative<TrapezoidHeight, ?>)instance, TrapezoidHeight::new));
    private static final Logger LOGGER = LogUtils.getLogger();
    private final VerticalAnchor minInclusive;
    private final VerticalAnchor maxInclusive;
    private final int plateau;

    private TrapezoidHeight(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2, int i) {
        this.minInclusive = verticalAnchor;
        this.maxInclusive = verticalAnchor2;
        this.plateau = i;
    }

    public static TrapezoidHeight of(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2, int i) {
        return new TrapezoidHeight(verticalAnchor, verticalAnchor2, i);
    }

    public static TrapezoidHeight of(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2) {
        return TrapezoidHeight.of(verticalAnchor, verticalAnchor2, 0);
    }

    @Override
    public int sample(RandomSource randomSource, WorldGenerationContext worldGenerationContext) {
        int j;
        int i = this.minInclusive.resolveY(worldGenerationContext);
        if (i > (j = this.maxInclusive.resolveY(worldGenerationContext))) {
            LOGGER.warn("Empty height range: {}", (Object)this);
            return i;
        }
        int k = j - i;
        if (this.plateau >= k) {
            return Mth.randomBetweenInclusive(randomSource, i, j);
        }
        int l = (k - this.plateau) / 2;
        int m = k - l;
        return i + Mth.randomBetweenInclusive(randomSource, 0, m) + Mth.randomBetweenInclusive(randomSource, 0, l);
    }

    @Override
    public HeightProviderType<?> getType() {
        return HeightProviderType.TRAPEZOID;
    }

    public String toString() {
        if (this.plateau == 0) {
            return "triangle (" + this.minInclusive + "-" + this.maxInclusive + ")";
        }
        return "trapezoid(" + this.plateau + ") in [" + this.minInclusive + "-" + this.maxInclusive + "]";
    }
}

