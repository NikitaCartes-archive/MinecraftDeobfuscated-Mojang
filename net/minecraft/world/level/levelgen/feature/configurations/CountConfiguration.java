/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class CountConfiguration
implements DecoratorConfiguration,
FeatureConfiguration {
    public static final Codec<CountConfiguration> CODEC = ((MapCodec)UniformInt.codec(-10, 128, 128).fieldOf("count")).xmap(CountConfiguration::new, CountConfiguration::count).codec();
    private final UniformInt count;

    public CountConfiguration(int i) {
        this.count = UniformInt.fixed(i);
    }

    public CountConfiguration(UniformInt uniformInt) {
        this.count = uniformInt;
    }

    public UniformInt count() {
        return this.count;
    }
}

