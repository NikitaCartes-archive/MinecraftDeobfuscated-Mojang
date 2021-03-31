package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;

public class LargeDripstoneConfiguration implements FeatureConfiguration {
	public static final Codec<LargeDripstoneConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.intRange(1, 512)
						.fieldOf("floor_to_ceiling_search_range")
						.orElse(30)
						.forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.floorToCeilingSearchRange),
					IntProvider.codec(1, 60).fieldOf("column_radius").forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.columnRadius),
					FloatProvider.codec(0.0F, 20.0F).fieldOf("height_scale").forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.heightScale),
					Codec.floatRange(0.1F, 1.0F)
						.fieldOf("max_column_radius_to_cave_height_ratio")
						.forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.maxColumnRadiusToCaveHeightRatio),
					FloatProvider.codec(0.1F, 10.0F).fieldOf("stalactite_bluntness").forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.stalactiteBluntness),
					FloatProvider.codec(0.1F, 10.0F).fieldOf("stalagmite_bluntness").forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.stalagmiteBluntness),
					FloatProvider.codec(0.0F, 2.0F).fieldOf("wind_speed").forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.windSpeed),
					Codec.intRange(0, 100).fieldOf("min_radius_for_wind").forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.minRadiusForWind),
					Codec.floatRange(0.0F, 5.0F).fieldOf("min_bluntness_for_wind").forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.minBluntnessForWind)
				)
				.apply(instance, LargeDripstoneConfiguration::new)
	);
	public final int floorToCeilingSearchRange;
	public final IntProvider columnRadius;
	public final FloatProvider heightScale;
	public final float maxColumnRadiusToCaveHeightRatio;
	public final FloatProvider stalactiteBluntness;
	public final FloatProvider stalagmiteBluntness;
	public final FloatProvider windSpeed;
	public final int minRadiusForWind;
	public final float minBluntnessForWind;

	public LargeDripstoneConfiguration(
		int i,
		IntProvider intProvider,
		FloatProvider floatProvider,
		float f,
		FloatProvider floatProvider2,
		FloatProvider floatProvider3,
		FloatProvider floatProvider4,
		int j,
		float g
	) {
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
