/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.UniformFloat;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class LargeDripstoneConfiguration
implements FeatureConfiguration {
    public static final Codec<LargeDripstoneConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.intRange(1, 512).fieldOf("floor_to_ceiling_search_range")).orElse(30).forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.floorToCeilingSearchRange), ((MapCodec)UniformInt.codec(1, 30, 30).fieldOf("column_radius")).forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.columnRadius), ((MapCodec)UniformFloat.codec(0.0f, 10.0f, 10.0f).fieldOf("height_scale")).forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.heightScale), ((MapCodec)Codec.floatRange(0.1f, 1.0f).fieldOf("max_column_radius_to_cave_height_ratio")).forGetter(largeDripstoneConfiguration -> Float.valueOf(largeDripstoneConfiguration.maxColumnRadiusToCaveHeightRatio)), ((MapCodec)UniformFloat.codec(0.1f, 5.0f, 5.0f).fieldOf("stalactite_bluntness")).forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.stalactiteBluntness), ((MapCodec)UniformFloat.codec(0.1f, 5.0f, 5.0f).fieldOf("stalagmite_bluntness")).forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.stalagmiteBluntness), ((MapCodec)UniformFloat.codec(0.0f, 1.0f, 1.0f).fieldOf("wind_speed")).forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.windSpeed), ((MapCodec)Codec.intRange(0, 100).fieldOf("min_radius_for_wind")).forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.minRadiusForWind), ((MapCodec)Codec.floatRange(0.0f, 5.0f).fieldOf("min_bluntness_for_wind")).forGetter(largeDripstoneConfiguration -> Float.valueOf(largeDripstoneConfiguration.minBluntnessForWind))).apply((Applicative<LargeDripstoneConfiguration, ?>)instance, LargeDripstoneConfiguration::new));
    public final int floorToCeilingSearchRange;
    public final UniformInt columnRadius;
    public final UniformFloat heightScale;
    public final float maxColumnRadiusToCaveHeightRatio;
    public final UniformFloat stalactiteBluntness;
    public final UniformFloat stalagmiteBluntness;
    public final UniformFloat windSpeed;
    public final int minRadiusForWind;
    public final float minBluntnessForWind;

    public LargeDripstoneConfiguration(int i, UniformInt uniformInt, UniformFloat uniformFloat, float f, UniformFloat uniformFloat2, UniformFloat uniformFloat3, UniformFloat uniformFloat4, int j, float g) {
        this.floorToCeilingSearchRange = i;
        this.columnRadius = uniformInt;
        this.heightScale = uniformFloat;
        this.maxColumnRadiusToCaveHeightRatio = f;
        this.stalactiteBluntness = uniformFloat2;
        this.stalagmiteBluntness = uniformFloat3;
        this.windSpeed = uniformFloat4;
        this.minRadiusForWind = j;
        this.minBluntnessForWind = g;
    }
}

