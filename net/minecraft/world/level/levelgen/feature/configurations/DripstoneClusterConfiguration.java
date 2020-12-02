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

public class DripstoneClusterConfiguration
implements FeatureConfiguration {
    public static final Codec<DripstoneClusterConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.intRange(1, 512).fieldOf("floor_to_ceiling_search_range")).forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.floorToCeilingSearchRange), ((MapCodec)UniformInt.codec(1, 64, 64).fieldOf("height")).forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.height), ((MapCodec)UniformInt.codec(1, 64, 64).fieldOf("radius")).forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.radius), ((MapCodec)Codec.intRange(0, 64).fieldOf("max_stalagmite_stalactite_height_diff")).forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.maxStalagmiteStalactiteHeightDiff), ((MapCodec)Codec.intRange(1, 64).fieldOf("height_deviation")).forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.heightDeviation), ((MapCodec)UniformInt.codec(0, 64, 64).fieldOf("dripstone_block_layer_thickness")).forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.dripstoneBlockLayerThickness), ((MapCodec)UniformFloat.codec(0.0f, 1.0f, 1.0f).fieldOf("density")).forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.density), ((MapCodec)UniformFloat.codec(0.0f, 1.0f, 1.0f).fieldOf("wetness")).forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.wetness), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("wetness_mean")).forGetter(dripstoneClusterConfiguration -> Float.valueOf(dripstoneClusterConfiguration.wetnessMean)), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("wetness_deviation")).forGetter(dripstoneClusterConfiguration -> Float.valueOf(dripstoneClusterConfiguration.wetnessDeviation)), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("chance_of_dripstone_column_at_max_distance_from_center")).forGetter(dripstoneClusterConfiguration -> Float.valueOf(dripstoneClusterConfiguration.chanceOfDripstoneColumnAtMaxDistanceFromCenter)), ((MapCodec)Codec.intRange(1, 64).fieldOf("max_distance_from_center_affecting_chance_of_dripstone_column")).forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.maxDistanceFromCenterAffectingChanceOfDripstoneColumn), ((MapCodec)Codec.intRange(1, 64).fieldOf("max_distance_from_center_affecting_height_bias")).forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.maxDistanceFromCenterAffectingHeightBias)).apply((Applicative<DripstoneClusterConfiguration, ?>)instance, DripstoneClusterConfiguration::new));
    public final int floorToCeilingSearchRange;
    public final UniformInt height;
    public final UniformInt radius;
    public final int maxStalagmiteStalactiteHeightDiff;
    public final int heightDeviation;
    public final UniformInt dripstoneBlockLayerThickness;
    public final UniformFloat density;
    public final UniformFloat wetness;
    public final float wetnessMean;
    public final float wetnessDeviation;
    public final float chanceOfDripstoneColumnAtMaxDistanceFromCenter;
    public final int maxDistanceFromCenterAffectingChanceOfDripstoneColumn;
    public final int maxDistanceFromCenterAffectingHeightBias;

    public DripstoneClusterConfiguration(int i, UniformInt uniformInt, UniformInt uniformInt2, int j, int k, UniformInt uniformInt3, UniformFloat uniformFloat, UniformFloat uniformFloat2, float f, float g, float h, int l, int m) {
        this.floorToCeilingSearchRange = i;
        this.height = uniformInt;
        this.radius = uniformInt2;
        this.maxStalagmiteStalactiteHeightDiff = j;
        this.heightDeviation = k;
        this.dripstoneBlockLayerThickness = uniformInt3;
        this.density = uniformFloat;
        this.wetness = uniformFloat2;
        this.wetnessMean = f;
        this.wetnessDeviation = g;
        this.chanceOfDripstoneColumnAtMaxDistanceFromCenter = h;
        this.maxDistanceFromCenterAffectingChanceOfDripstoneColumn = l;
        this.maxDistanceFromCenterAffectingHeightBias = m;
    }
}

