/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class LargeDripstoneConfiguration
implements FeatureConfiguration {
    public static final Codec<LargeDripstoneConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.intRange(1, 512).fieldOf("floor_to_ceiling_search_range")).orElse(30).forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.floorToCeilingSearchRange), ((MapCodec)IntProvider.codec(1, 60).fieldOf("column_radius")).forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.columnRadius), ((MapCodec)FloatProvider.codec(0.0f, 20.0f).fieldOf("height_scale")).forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.heightScale), ((MapCodec)Codec.floatRange(0.1f, 1.0f).fieldOf("max_column_radius_to_cave_height_ratio")).forGetter(largeDripstoneConfiguration -> Float.valueOf(largeDripstoneConfiguration.maxColumnRadiusToCaveHeightRatio)), ((MapCodec)FloatProvider.codec(0.1f, 10.0f).fieldOf("stalactite_bluntness")).forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.stalactiteBluntness), ((MapCodec)FloatProvider.codec(0.1f, 10.0f).fieldOf("stalagmite_bluntness")).forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.stalagmiteBluntness), ((MapCodec)FloatProvider.codec(0.0f, 2.0f).fieldOf("wind_speed")).forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.windSpeed), ((MapCodec)Codec.intRange(0, 100).fieldOf("min_radius_for_wind")).forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.minRadiusForWind), ((MapCodec)Codec.floatRange(0.0f, 5.0f).fieldOf("min_bluntness_for_wind")).forGetter(largeDripstoneConfiguration -> Float.valueOf(largeDripstoneConfiguration.minBluntnessForWind))).apply((Applicative<LargeDripstoneConfiguration, ?>)instance, LargeDripstoneConfiguration::new));
    public final int floorToCeilingSearchRange;
    public final IntProvider columnRadius;
    public final FloatProvider heightScale;
    public final float maxColumnRadiusToCaveHeightRatio;
    public final FloatProvider stalactiteBluntness;
    public final FloatProvider stalagmiteBluntness;
    public final FloatProvider windSpeed;
    public final int minRadiusForWind;
    public final float minBluntnessForWind;

    public LargeDripstoneConfiguration(int i, IntProvider intProvider, FloatProvider floatProvider, float f, FloatProvider floatProvider2, FloatProvider floatProvider3, FloatProvider floatProvider4, int j, float g) {
        this.floorToCeilingSearchRange = i;
        this.columnRadius = intProvider;
        this.heightScale = floatProvider;
        this.maxColumnRadiusToCaveHeightRatio = f;
        this.stalactiteBluntness = floatProvider2;
        this.stalagmiteBluntness = floatProvider3;
        this.windSpeed = floatProvider4;
        this.minRadiusForWind = j;
        this.minBluntnessForWind = g;
    }
}

