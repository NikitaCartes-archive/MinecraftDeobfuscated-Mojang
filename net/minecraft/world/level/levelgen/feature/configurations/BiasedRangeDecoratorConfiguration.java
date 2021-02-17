/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class BiasedRangeDecoratorConfiguration
implements DecoratorConfiguration {
    public static final Codec<BiasedRangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)VerticalAnchor.CODEC.fieldOf("bottom_inclusive")).forGetter(BiasedRangeDecoratorConfiguration::bottomInclusive), ((MapCodec)VerticalAnchor.CODEC.fieldOf("top_inclusive")).forGetter(BiasedRangeDecoratorConfiguration::topInclusive), ((MapCodec)Codec.INT.fieldOf("cutoff")).forGetter(BiasedRangeDecoratorConfiguration::cutoff)).apply((Applicative<BiasedRangeDecoratorConfiguration, ?>)instance, BiasedRangeDecoratorConfiguration::new));
    private final VerticalAnchor bottomInclusive;
    private final VerticalAnchor topInclusive;
    private final int cutoff;

    public BiasedRangeDecoratorConfiguration(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2, int i) {
        this.bottomInclusive = verticalAnchor;
        this.cutoff = i;
        this.topInclusive = verticalAnchor2;
    }

    public VerticalAnchor bottomInclusive() {
        return this.bottomInclusive;
    }

    public int cutoff() {
        return this.cutoff;
    }

    public VerticalAnchor topInclusive() {
        return this.topInclusive;
    }
}

