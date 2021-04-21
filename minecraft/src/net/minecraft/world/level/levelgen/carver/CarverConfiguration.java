package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CarverConfiguration extends ProbabilityFeatureConfiguration {
	public static final MapCodec<CarverConfiguration> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(carverConfiguration -> carverConfiguration.probability),
					HeightProvider.CODEC.fieldOf("y").forGetter(carverConfiguration -> carverConfiguration.y),
					FloatProvider.CODEC.fieldOf("yScale").forGetter(carverConfiguration -> carverConfiguration.yScale),
					VerticalAnchor.CODEC.fieldOf("lava_level").forGetter(carverConfiguration -> carverConfiguration.lavaLevel),
					Codec.BOOL.fieldOf("aquifers_enabled").forGetter(carverConfiguration -> carverConfiguration.aquifersEnabled),
					CarverDebugSettings.CODEC
						.optionalFieldOf("debug_settings", CarverDebugSettings.DEFAULT)
						.forGetter(carverConfiguration -> carverConfiguration.debugSettings)
				)
				.apply(instance, CarverConfiguration::new)
	);
	public final HeightProvider y;
	public final FloatProvider yScale;
	public final VerticalAnchor lavaLevel;
	public final boolean aquifersEnabled;
	public final CarverDebugSettings debugSettings;

	public CarverConfiguration(
		float f, HeightProvider heightProvider, FloatProvider floatProvider, VerticalAnchor verticalAnchor, boolean bl, CarverDebugSettings carverDebugSettings
	) {
		super(f);
		this.y = heightProvider;
		this.yScale = floatProvider;
		this.lavaLevel = verticalAnchor;
		this.aquifersEnabled = bl;
		this.debugSettings = carverDebugSettings;
	}
}
