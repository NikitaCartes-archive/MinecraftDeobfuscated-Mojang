/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.carver;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CaveCarverConfiguration
extends CarverConfiguration {
    public static final Codec<CaveCarverConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(CarverConfiguration.CODEC.forGetter(caveCarverConfiguration -> caveCarverConfiguration), ((MapCodec)FloatProvider.CODEC.fieldOf("horizontal_radius_multiplier")).forGetter(caveCarverConfiguration -> caveCarverConfiguration.horizontalRadiusMultiplier), ((MapCodec)FloatProvider.CODEC.fieldOf("vertical_radius_multiplier")).forGetter(caveCarverConfiguration -> caveCarverConfiguration.verticalRadiusMultiplier), ((MapCodec)FloatProvider.codec(-1.0f, 1.0f).fieldOf("floor_level")).forGetter(caveCarverConfiguration -> caveCarverConfiguration.floorLevel)).apply((Applicative<CaveCarverConfiguration, ?>)instance, CaveCarverConfiguration::new));
    public final FloatProvider horizontalRadiusMultiplier;
    public final FloatProvider verticalRadiusMultiplier;
    final FloatProvider floorLevel;

    public CaveCarverConfiguration(float f, HeightProvider heightProvider, FloatProvider floatProvider, VerticalAnchor verticalAnchor, boolean bl, CarverDebugSettings carverDebugSettings, FloatProvider floatProvider2, FloatProvider floatProvider3, FloatProvider floatProvider4) {
        super(f, heightProvider, floatProvider, verticalAnchor, bl, carverDebugSettings);
        this.horizontalRadiusMultiplier = floatProvider2;
        this.verticalRadiusMultiplier = floatProvider3;
        this.floorLevel = floatProvider4;
    }

    public CaveCarverConfiguration(float f, HeightProvider heightProvider, FloatProvider floatProvider, VerticalAnchor verticalAnchor, boolean bl, FloatProvider floatProvider2, FloatProvider floatProvider3, FloatProvider floatProvider4) {
        this(f, heightProvider, floatProvider, verticalAnchor, bl, CarverDebugSettings.DEFAULT, floatProvider2, floatProvider3, floatProvider4);
    }

    public CaveCarverConfiguration(CarverConfiguration carverConfiguration, FloatProvider floatProvider, FloatProvider floatProvider2, FloatProvider floatProvider3) {
        this(carverConfiguration.probability, carverConfiguration.y, carverConfiguration.yScale, carverConfiguration.lavaLevel, carverConfiguration.aquifersEnabled, carverConfiguration.debugSettings, floatProvider, floatProvider2, floatProvider3);
    }
}

