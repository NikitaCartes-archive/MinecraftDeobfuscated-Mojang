/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class MineshaftConfiguration
implements FeatureConfiguration {
    public static final Codec<MineshaftConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("probability")).forGetter(mineshaftConfiguration -> Float.valueOf(mineshaftConfiguration.probability)), ((MapCodec)MineshaftFeature.Type.CODEC.fieldOf("type")).forGetter(mineshaftConfiguration -> mineshaftConfiguration.type)).apply((Applicative<MineshaftConfiguration, ?>)instance, MineshaftConfiguration::new));
    public final float probability;
    public final MineshaftFeature.Type type;

    public MineshaftConfiguration(float f, MineshaftFeature.Type type) {
        this.probability = f;
        this.type = type;
    }
}

