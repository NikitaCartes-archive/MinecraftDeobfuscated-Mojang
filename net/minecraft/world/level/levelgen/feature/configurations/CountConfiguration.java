/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class CountConfiguration
implements FeatureConfiguration {
    public static final Codec<CountConfiguration> CODEC = ((MapCodec)IntProvider.codec(0, 256).fieldOf("count")).xmap(CountConfiguration::new, CountConfiguration::count).codec();
    private final IntProvider count;

    public CountConfiguration(int i) {
        this.count = ConstantInt.of(i);
    }

    public CountConfiguration(IntProvider intProvider) {
        this.count = intProvider;
    }

    public IntProvider count() {
        return this.count;
    }
}

