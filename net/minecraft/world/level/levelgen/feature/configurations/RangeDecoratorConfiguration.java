/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class RangeDecoratorConfiguration
implements DecoratorConfiguration {
    public static final Codec<RangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("bottom_offset")).orElse(0).forGetter(rangeDecoratorConfiguration -> rangeDecoratorConfiguration.bottomOffset), ((MapCodec)Codec.INT.fieldOf("top_offset")).orElse(0).forGetter(rangeDecoratorConfiguration -> rangeDecoratorConfiguration.topOffset), ((MapCodec)Codec.INT.fieldOf("maximum")).orElse(0).forGetter(rangeDecoratorConfiguration -> rangeDecoratorConfiguration.maximum)).apply((Applicative<RangeDecoratorConfiguration, ?>)instance, RangeDecoratorConfiguration::new));
    public final int bottomOffset;
    public final int topOffset;
    public final int maximum;

    public RangeDecoratorConfiguration(int i, int j, int k) {
        this.bottomOffset = i;
        this.topOffset = j;
        this.maximum = k;
    }
}

