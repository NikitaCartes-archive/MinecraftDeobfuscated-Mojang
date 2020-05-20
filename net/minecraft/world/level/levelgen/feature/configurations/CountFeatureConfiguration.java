/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class CountFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<CountFeatureConfiguration> CODEC = ((MapCodec)Codec.INT.fieldOf("count")).xmap(CountFeatureConfiguration::new, countFeatureConfiguration -> countFeatureConfiguration.count).codec();
    public final int count;

    public CountFeatureConfiguration(int i) {
        this.count = i;
    }
}

