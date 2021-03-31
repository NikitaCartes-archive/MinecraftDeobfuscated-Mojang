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

public class CanyonCarverConfiguration
extends CarverConfiguration {
    public static final Codec<CanyonCarverConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(CarverConfiguration.CODEC.forGetter(canyonCarverConfiguration -> canyonCarverConfiguration), ((MapCodec)FloatProvider.CODEC.fieldOf("vertical_rotation")).forGetter(canyonCarverConfiguration -> canyonCarverConfiguration.verticalRotation), ((MapCodec)CanyonShapeConfiguration.CODEC.fieldOf("shape")).forGetter(canyonCarverConfiguration -> canyonCarverConfiguration.shape)).apply((Applicative<CanyonCarverConfiguration, ?>)instance, CanyonCarverConfiguration::new));
    public final FloatProvider verticalRotation;
    public final CanyonShapeConfiguration shape;

    public CanyonCarverConfiguration(float f, HeightProvider heightProvider, FloatProvider floatProvider, VerticalAnchor verticalAnchor, CarverDebugSettings carverDebugSettings, FloatProvider floatProvider2, CanyonShapeConfiguration canyonShapeConfiguration) {
        super(f, heightProvider, floatProvider, verticalAnchor, carverDebugSettings);
        this.verticalRotation = floatProvider2;
        this.shape = canyonShapeConfiguration;
    }

    public CanyonCarverConfiguration(CarverConfiguration carverConfiguration, FloatProvider floatProvider, CanyonShapeConfiguration canyonShapeConfiguration) {
        this(carverConfiguration.probability, carverConfiguration.y, carverConfiguration.yScale, carverConfiguration.lavaLevel, carverConfiguration.debugSettings, floatProvider, canyonShapeConfiguration);
    }

    public static class CanyonShapeConfiguration {
        public static final Codec<CanyonShapeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)FloatProvider.CODEC.fieldOf("distance_factor")).forGetter(canyonShapeConfiguration -> canyonShapeConfiguration.distanceFactor), ((MapCodec)FloatProvider.CODEC.fieldOf("thickness")).forGetter(canyonShapeConfiguration -> canyonShapeConfiguration.thickness), ((MapCodec)Codec.intRange(0, Integer.MAX_VALUE).fieldOf("width_smoothness")).forGetter(canyonShapeConfiguration -> canyonShapeConfiguration.widthSmoothness), ((MapCodec)FloatProvider.CODEC.fieldOf("horizontal_radius_factor")).forGetter(canyonShapeConfiguration -> canyonShapeConfiguration.horizontalRadiusFactor), ((MapCodec)Codec.FLOAT.fieldOf("vertical_radius_default_factor")).forGetter(canyonShapeConfiguration -> Float.valueOf(canyonShapeConfiguration.verticalRadiusDefaultFactor)), ((MapCodec)Codec.FLOAT.fieldOf("vertical_radius_center_factor")).forGetter(canyonShapeConfiguration -> Float.valueOf(canyonShapeConfiguration.verticalRadiusCenterFactor))).apply((Applicative<CanyonShapeConfiguration, ?>)instance, CanyonShapeConfiguration::new));
        public final FloatProvider distanceFactor;
        public final FloatProvider thickness;
        public final int widthSmoothness;
        public final FloatProvider horizontalRadiusFactor;
        public final float verticalRadiusDefaultFactor;
        public final float verticalRadiusCenterFactor;

        public CanyonShapeConfiguration(FloatProvider floatProvider, FloatProvider floatProvider2, int i, FloatProvider floatProvider3, float f, float g) {
            this.widthSmoothness = i;
            this.horizontalRadiusFactor = floatProvider3;
            this.verticalRadiusDefaultFactor = f;
            this.verticalRadiusCenterFactor = g;
            this.distanceFactor = floatProvider;
            this.thickness = floatProvider2;
        }
    }
}

