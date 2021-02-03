package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.UniformFloat;
import net.minecraft.util.UniformInt;

public class DripstoneClusterConfiguration implements FeatureConfiguration {
	public static final Codec<DripstoneClusterConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.intRange(1, 512)
						.fieldOf("floor_to_ceiling_search_range")
						.forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.floorToCeilingSearchRange),
					UniformInt.codec(1, 64, 64).fieldOf("height").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.height),
					UniformInt.codec(1, 64, 64).fieldOf("radius").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.radius),
					Codec.intRange(0, 64)
						.fieldOf("max_stalagmite_stalactite_height_diff")
						.forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.maxStalagmiteStalactiteHeightDiff),
					Codec.intRange(1, 64).fieldOf("height_deviation").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.heightDeviation),
					UniformInt.codec(0, 64, 64)
						.fieldOf("dripstone_block_layer_thickness")
						.forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.dripstoneBlockLayerThickness),
					UniformFloat.codec(0.0F, 1.0F, 1.0F).fieldOf("density").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.density),
					UniformFloat.codec(0.0F, 1.0F, 1.0F).fieldOf("wetness").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.wetness),
					Codec.floatRange(0.0F, 1.0F).fieldOf("wetness_mean").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.wetnessMean),
					Codec.floatRange(0.0F, 1.0F).fieldOf("wetness_deviation").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.wetnessDeviation),
					Codec.floatRange(0.0F, 1.0F)
						.fieldOf("chance_of_dripstone_column_at_max_distance_from_center")
						.forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.chanceOfDripstoneColumnAtMaxDistanceFromCenter),
					Codec.intRange(1, 64)
						.fieldOf("max_distance_from_edge_affecting_chance_of_dripstone_column")
						.forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.maxDistanceFromEdgeAffectingChanceOfDripstoneColumn),
					Codec.intRange(1, 64)
						.fieldOf("max_distance_from_center_affecting_height_bias")
						.forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.maxDistanceFromCenterAffectingHeightBias)
				)
				.apply(instance, DripstoneClusterConfiguration::new)
	);
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
	public final int maxDistanceFromEdgeAffectingChanceOfDripstoneColumn;
	public final int maxDistanceFromCenterAffectingHeightBias;

	public DripstoneClusterConfiguration(
		int i,
		UniformInt uniformInt,
		UniformInt uniformInt2,
		int j,
		int k,
		UniformInt uniformInt3,
		UniformFloat uniformFloat,
		UniformFloat uniformFloat2,
		float f,
		float g,
		float h,
		int l,
		int m
	) {
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
		this.maxDistanceFromEdgeAffectingChanceOfDripstoneColumn = l;
		this.maxDistanceFromCenterAffectingHeightBias = m;
	}
}
