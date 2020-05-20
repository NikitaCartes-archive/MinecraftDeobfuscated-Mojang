/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class FrequencyWithExtraChanceDecoratorConfiguration
implements DecoratorConfiguration {
    public static final Codec<FrequencyWithExtraChanceDecoratorConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("count")).forGetter(frequencyWithExtraChanceDecoratorConfiguration -> frequencyWithExtraChanceDecoratorConfiguration.count), ((MapCodec)Codec.FLOAT.fieldOf("extra_chance")).forGetter(frequencyWithExtraChanceDecoratorConfiguration -> Float.valueOf(frequencyWithExtraChanceDecoratorConfiguration.extraChance)), ((MapCodec)Codec.INT.fieldOf("extra_count")).forGetter(frequencyWithExtraChanceDecoratorConfiguration -> frequencyWithExtraChanceDecoratorConfiguration.extraCount)).apply((Applicative<FrequencyWithExtraChanceDecoratorConfiguration, ?>)instance, FrequencyWithExtraChanceDecoratorConfiguration::new));
    public final int count;
    public final float extraChance;
    public final int extraCount;

    public FrequencyWithExtraChanceDecoratorConfiguration(int i, float f, int j) {
        this.count = i;
        this.extraChance = f;
        this.extraCount = j;
    }
}

