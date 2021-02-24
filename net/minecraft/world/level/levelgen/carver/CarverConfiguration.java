/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.carver;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class CarverConfiguration
extends ProbabilityFeatureConfiguration {
    public static final Codec<CarverConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("probability")).forGetter(carverConfiguration -> Float.valueOf(carverConfiguration.probability)), CarverDebugSettings.CODEC.optionalFieldOf("debug_settings", CarverDebugSettings.DEFAULT).forGetter(CarverConfiguration::getDebugSettings)).apply((Applicative<CarverConfiguration, ?>)instance, CarverConfiguration::new));
    private final CarverDebugSettings debugSettings;

    public CarverConfiguration(float f, CarverDebugSettings carverDebugSettings) {
        super(f);
        this.debugSettings = carverDebugSettings;
    }

    public CarverConfiguration(float f) {
        this(f, CarverDebugSettings.DEFAULT);
    }

    public CarverDebugSettings getDebugSettings() {
        return this.debugSettings;
    }
}

