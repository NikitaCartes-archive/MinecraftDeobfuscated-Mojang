/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class WaterDepthThresholdConfiguration
implements DecoratorConfiguration {
    public static final Codec<WaterDepthThresholdConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("max_water_depth")).forGetter(waterDepthThresholdConfiguration -> waterDepthThresholdConfiguration.maxWaterDepth)).apply((Applicative<WaterDepthThresholdConfiguration, ?>)instance, WaterDepthThresholdConfiguration::new));
    public final int maxWaterDepth;

    public WaterDepthThresholdConfiguration(int i) {
        this.maxWaterDepth = i;
    }
}

