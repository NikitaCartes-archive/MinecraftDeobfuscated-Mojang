package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;

public class DripstoneClusterConfiguration implements FeatureConfiguration {
	public static final Codec<DripstoneClusterConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.intRange(1, 512)
						.fieldOf("floor_to_ceiling_search_range")
						.forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.floorToCeilingSearchRange),
					IntProvider.codec(1, 128).fieldOf("height").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.height),
					IntProvider.codec(1, 128).fieldOf("radius").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.radius),
					Codec.intRange(0, 64)
						.fieldOf("max_stalagmite_stalactite_height_diff")
						.forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.maxStalagmiteStalactiteHeightDiff),
					Codec.intRange(1, 64).fieldOf("height_deviation").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.heightDeviation),
					IntProvider.codec(0, 128)
						.fieldOf("dripstone_block_layer_thickness")
						.forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.dripstoneBlockLayerThickness),
					FloatProvider.codec(0.0F, 2.0F).fieldOf("density").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.density),
					FloatProvider.codec(0.0F, 2.0F).fieldOf("wetness").forGetter(dripstoneClusterConfiguration -> dripstoneClusterConfiguration.wetness),
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
	public final IntProvider height;
	public final IntProvider radius;
	public final int maxStalagmiteStalactiteHeightDiff;
	public final int heightDeviation;
	public final IntProvider dripstoneBlockLayerThickness;
	public final FloatProvider density;
	public final FloatProvider wetness;
	public final float chanceOfDripstoneColumnAtMaxDistanceFromCenter;
	public final int maxDistanceFromEdgeAffectingChanceOfDripstoneColumn;
	public final int maxDistanceFromCenterAffectingHeightBias;

	public DripstoneClusterConfiguration(
		int i,
		IntProvider intProvider,
		IntProvider intProvider2,
		int j,
		int k,
		IntProvider intProvider3,
		FloatProvider floatProvider,
		FloatProvider floatProvider2,
		float f,
		int l,
		int m
	) {
		this.floorToCeilingSearchRange = i;
		this.height = intProvider;
		this.radius = intProvider2;
		this.maxStalagmiteStalactiteHeightDiff = j;
		this.heightDeviation = k;
		this.dripstoneBlockLayerThickness = intProvider3;
		this.density = floatProvider;
		this.wetness = floatProvider2;
		this.chanceOfDripstoneColumnAtMaxDistanceFromCenter = f;
		this.maxDistanceFromEdgeAffectingChanceOfDripstoneColumn = l;
		this.maxDistanceFromCenterAffectingHeightBias = m;
	}
}
