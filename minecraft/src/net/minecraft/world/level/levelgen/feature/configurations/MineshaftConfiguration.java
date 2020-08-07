package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;

public class MineshaftConfiguration implements FeatureConfiguration {
	public static final Codec<MineshaftConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(mineshaftConfiguration -> mineshaftConfiguration.probability),
					MineshaftFeature.Type.CODEC.fieldOf("type").forGetter(mineshaftConfiguration -> mineshaftConfiguration.type)
				)
				.apply(instance, MineshaftConfiguration::new)
	);
	public final float probability;
	public final MineshaftFeature.Type type;

	public MineshaftConfiguration(float f, MineshaftFeature.Type type) {
		this.probability = f;
		this.type = type;
	}
}
