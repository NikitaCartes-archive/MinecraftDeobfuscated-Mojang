/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class ColumnFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<ColumnFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)UniformInt.codec(0, 2, 1).fieldOf("reach")).forGetter(columnFeatureConfiguration -> columnFeatureConfiguration.reach), ((MapCodec)UniformInt.codec(1, 5, 5).fieldOf("height")).forGetter(columnFeatureConfiguration -> columnFeatureConfiguration.height)).apply((Applicative<ColumnFeatureConfiguration, ?>)instance, ColumnFeatureConfiguration::new));
    private final UniformInt reach;
    private final UniformInt height;

    public ColumnFeatureConfiguration(UniformInt uniformInt, UniformInt uniformInt2) {
        this.reach = uniformInt;
        this.height = uniformInt2;
    }

    public UniformInt reach() {
        return this.reach;
    }

    public UniformInt height() {
        return this.height;
    }
}

