package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class CarverConfiguration extends ProbabilityFeatureConfiguration {
	public static final Codec<CarverConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(carverConfiguration -> carverConfiguration.probability),
					CarverDebugSettings.CODEC.optionalFieldOf("debug_settings", CarverDebugSettings.DEFAULT).forGetter(CarverConfiguration::getDebugSettings)
				)
				.apply(instance, CarverConfiguration::new)
	);
	private final CarverDebugSettings debugSettings;

	public CarverConfiguration(float f, CarverDebugSettings carverDebugSettings) {
		super(f);
		this.debugSettings = carverDebugSettings;
	}

	public CarverConfiguration(float f) {
		this(f, CarverDebugSettings.DEFAULT);
	}

	public CarverDebugSettings getDebugSettings() {
		return this.debugSettings;
	}
}
