package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.UniformFloat;
import net.minecraft.util.UniformInt;

public class LargeDripstoneConfiguration implements FeatureConfiguration {
	public static final Codec<LargeDripstoneConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.intRange(1, 512)
						.fieldOf("floor_to_ceiling_search_range")
						.orElse(30)
						.forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.floorToCeilingSearchRange),
					UniformInt.codec(1, 30, 30).fieldOf("column_radius").forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.columnRadius),
					UniformFloat.codec(0.0F, 10.0F, 10.0F).fieldOf("height_scale").forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.heightScale),
					Codec.floatRange(0.1F, 1.0F)
						.fieldOf("max_column_radius_to_cave_height_ratio")
						.forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.maxColumnRadiusToCaveHeightRatio),
					UniformFloat.codec(0.1F, 5.0F, 5.0F)
						.fieldOf("stalactite_bluntness")
						.forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.stalactiteBluntness),
					UniformFloat.codec(0.1F, 5.0F, 5.0F)
						.fieldOf("stalagmite_bluntness")
						.forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.stalagmiteBluntness),
					UniformFloat.codec(0.0F, 1.0F, 1.0F).fieldOf("wind_speed").forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.windSpeed),
					Codec.intRange(0, 100).fieldOf("min_radius_for_wind").forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.minRadiusForWind),
					Codec.floatRange(0.0F, 5.0F).fieldOf("min_bluntness_for_wind").forGetter(largeDripstoneConfiguration -> largeDripstoneConfiguration.minBluntnessForWind)
				)
				.apply(instance, LargeDripstoneConfiguration::new)
	);
	public final int floorToCeilingSearchRange;
	public final UniformInt columnRadius;
	public final UniformFloat heightScale;
	public final float maxColumnRadiusToCaveHeightRatio;
	public final UniformFloat stalactiteBluntness;
	public final UniformFloat stalagmiteBluntness;
	public final UniformFloat windSpeed;
	public final int minRadiusForWind;
	public final float minBluntnessForWind;

	public LargeDripstoneConfiguration(
		int i,
		UniformInt uniformInt,
		UniformFloat uniformFloat,
		float f,
		UniformFloat uniformFloat2,
		UniformFloat uniformFloat3,
		UniformFloat uniformFloat4,
		int j,
		float g
	) {
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
