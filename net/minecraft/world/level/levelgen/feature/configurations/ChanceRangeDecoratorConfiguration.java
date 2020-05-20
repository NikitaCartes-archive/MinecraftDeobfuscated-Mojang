/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class ChanceRangeDecoratorConfiguration
implements DecoratorConfiguration {
    public static final Codec<ChanceRangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("chance")).forGetter(chanceRangeDecoratorConfiguration -> Float.valueOf(chanceRangeDecoratorConfiguration.chance)), ((MapCodec)Codec.INT.fieldOf("bottom_offset")).withDefault(0).forGetter(chanceRangeDecoratorConfiguration -> chanceRangeDecoratorConfiguration.bottomOffset), ((MapCodec)Codec.INT.fieldOf("top_offset")).withDefault(0).forGetter(chanceRangeDecoratorConfiguration -> chanceRangeDecoratorConfiguration.topOffset), ((MapCodec)Codec.INT.fieldOf("top")).withDefault(0).forGetter(chanceRangeDecoratorConfiguration -> chanceRangeDecoratorConfiguration.top)).apply((Applicative<ChanceRangeDecoratorConfiguration, ?>)instance, ChanceRangeDecoratorConfiguration::new));
    public final float chance;
    public final int bottomOffset;
    public final int topOffset;
    public final int top;

    public ChanceRangeDecoratorConfiguration(float f, int i, int j, int k) {
        this.chance = f;
        this.bottomOffset = i;
        this.topOffset = j;
        this.top = k;
    }
}

