package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class WaterDepthThresholdConfiguration implements DecoratorConfiguration {
	public static final Codec<WaterDepthThresholdConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(Codec.INT.fieldOf("max_water_depth").forGetter(waterDepthThresholdConfiguration -> waterDepthThresholdConfiguration.maxWaterDepth))
				.apply(instance, WaterDepthThresholdConfiguration::new)
	);
	public final int maxWaterDepth;

	public WaterDepthThresholdConfiguration(int i) {
		this.maxWaterDepth = i;
	}
}
