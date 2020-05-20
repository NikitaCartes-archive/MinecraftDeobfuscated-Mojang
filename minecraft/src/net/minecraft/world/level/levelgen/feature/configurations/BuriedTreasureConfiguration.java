package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;

public class BuriedTreasureConfiguration implements FeatureConfiguration {
	public static final Codec<BuriedTreasureConfiguration> CODEC = Codec.FLOAT
		.xmap(BuriedTreasureConfiguration::new, buriedTreasureConfiguration -> buriedTreasureConfiguration.probability);
	public final float probability;

	public BuriedTreasureConfiguration(float f) {
		this.probability = f;
	}
}
