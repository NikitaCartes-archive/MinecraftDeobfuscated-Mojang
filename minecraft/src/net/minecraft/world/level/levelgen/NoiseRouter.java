package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;

public record NoiseRouter(
	DensityFunction barrierNoise,
	DensityFunction fluidLevelFloodednessNoise,
	DensityFunction fluidLevelSpreadNoise,
	DensityFunction lavaNoise,
	DensityFunction temperature,
	DensityFunction vegetation,
	DensityFunction continents,
	DensityFunction erosion,
	DensityFunction depth,
	DensityFunction ridges,
	DensityFunction initialDensityWithoutJaggedness,
	DensityFunction finalDensity,
	DensityFunction veinToggle,
	DensityFunction veinRidged,
	DensityFunction veinGap
) {
	public static final Codec<NoiseRouter> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					field("barrier", NoiseRouter::barrierNoise),
					field("fluid_level_floodedness", NoiseRouter::fluidLevelFloodednessNoise),
					field("fluid_level_spread", NoiseRouter::fluidLevelSpreadNoise),
					field("lava", NoiseRouter::lavaNoise),
					field("temperature", NoiseRouter::temperature),
					field("vegetation", NoiseRouter::vegetation),
					field("continents", NoiseRouter::continents),
					field("erosion", NoiseRouter::erosion),
					field("depth", NoiseRouter::depth),
					field("ridges", NoiseRouter::ridges),
					field("initial_density_without_jaggedness", NoiseRouter::initialDensityWithoutJaggedness),
					field("final_density", NoiseRouter::finalDensity),
					field("vein_toggle", NoiseRouter::veinToggle),
					field("vein_ridged", NoiseRouter::veinRidged),
					field("vein_gap", NoiseRouter::veinGap)
				)
				.apply(instance, NoiseRouter::new)
	);

	private static RecordCodecBuilder<NoiseRouter, DensityFunction> field(String string, Function<NoiseRouter, DensityFunction> function) {
		return DensityFunction.HOLDER_HELPER_CODEC.fieldOf(string).forGetter(function);
	}

	public NoiseRouter mapAll(DensityFunction.Visitor visitor) {
		return new NoiseRouter(
			this.barrierNoise.mapAll(visitor),
			this.fluidLevelFloodednessNoise.mapAll(visitor),
			this.fluidLevelSpreadNoise.mapAll(visitor),
			this.lavaNoise.mapAll(visitor),
			this.temperature.mapAll(visitor),
			this.vegetation.mapAll(visitor),
			this.continents.mapAll(visitor),
			this.erosion.mapAll(visitor),
			this.depth.mapAll(visitor),
			this.ridges.mapAll(visitor),
			this.initialDensityWithoutJaggedness.mapAll(visitor),
			this.finalDensity.mapAll(visitor),
			this.veinToggle.mapAll(visitor),
			this.veinRidged.mapAll(visitor),
			this.veinGap.mapAll(visitor)
		);
	}
}
