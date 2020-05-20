/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class FrequencyDecoratorConfiguration
implements DecoratorConfiguration {
    public static final Codec<FrequencyDecoratorConfiguration> CODEC = ((MapCodec)Codec.INT.fieldOf("count")).xmap(FrequencyDecoratorConfiguration::new, frequencyDecoratorConfiguration -> frequencyDecoratorConfiguration.count).codec();
    public final int count;

    public FrequencyDecoratorConfiguration(int i) {
        this.count = i;
    }
}

