package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;

public record NoiseRouterWithOnlyNoises(
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
	public static final Codec<NoiseRouterWithOnlyNoises> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					field("barrier", NoiseRouterWithOnlyNoises::barrierNoise),
					field("fluid_level_floodedness", NoiseRouterWithOnlyNoises::fluidLevelFloodednessNoise),
					field("fluid_level_spread", NoiseRouterWithOnlyNoises::fluidLevelSpreadNoise),
					field("lava", NoiseRouterWithOnlyNoises::lavaNoise),
					field("temperature", NoiseRouterWithOnlyNoises::temperature),
					field("vegetation", NoiseRouterWithOnlyNoises::vegetation),
					field("continents", NoiseRouterWithOnlyNoises::continents),
					field("erosion", NoiseRouterWithOnlyNoises::erosion),
					field("depth", NoiseRouterWithOnlyNoises::depth),
					field("ridges", NoiseRouterWithOnlyNoises::ridges),
					field("initial_density_without_jaggedness", NoiseRouterWithOnlyNoises::initialDensityWithoutJaggedness),
					field("final_density", NoiseRouterWithOnlyNoises::finalDensity),
					field("vein_toggle", NoiseRouterWithOnlyNoises::veinToggle),
					field("vein_ridged", NoiseRouterWithOnlyNoises::veinRidged),
					field("vein_gap", NoiseRouterWithOnlyNoises::veinGap)
				)
				.apply(instance, NoiseRouterWithOnlyNoises::new)
	);

	private static RecordCodecBuilder<NoiseRouterWithOnlyNoises, DensityFunction> field(
		String string, Function<NoiseRouterWithOnlyNoises, DensityFunction> function
	) {
		return DensityFunction.HOLDER_HELPER_CODEC.fieldOf(string).forGetter(function);
	}

	public NoiseRouterWithOnlyNoises mapAll(DensityFunction.Visitor visitor) {
		return new NoiseRouterWithOnlyNoises(
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
