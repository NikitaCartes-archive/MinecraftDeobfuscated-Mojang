/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class FrequencyChanceDecoratorConfiguration
implements DecoratorConfiguration {
    public static final Codec<FrequencyChanceDecoratorConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("count")).forGetter(frequencyChanceDecoratorConfiguration -> frequencyChanceDecoratorConfiguration.count), ((MapCodec)Codec.FLOAT.fieldOf("chance")).forGetter(frequencyChanceDecoratorConfiguration -> Float.valueOf(frequencyChanceDecoratorConfiguration.chance))).apply((Applicative<FrequencyChanceDecoratorConfiguration, ?>)instance, FrequencyChanceDecoratorConfiguration::new));
    public final int count;
    public final float chance;

    public FrequencyChanceDecoratorConfiguration(int i, float f) {
        this.count = i;
        this.chance = f;
    }
}

