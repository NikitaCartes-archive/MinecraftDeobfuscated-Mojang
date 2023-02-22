package net.minecraft.world.level.biome;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.levelgen.NoiseRouterData;

public class MultiNoiseBiomeSource extends BiomeSource {
	private static final MapCodec<Holder<Biome>> ENTRY_CODEC = Biome.CODEC.fieldOf("biome");
	public static final MapCodec<Climate.ParameterList<Holder<Biome>>> DIRECT_CODEC = Climate.ParameterList.codec(ENTRY_CODEC).fieldOf("biomes");
	private static final MapCodec<Holder<MultiNoiseBiomeSourceParameterList>> PRESET_CODEC = MultiNoiseBiomeSourceParameterList.CODEC
		.fieldOf("preset")
		.withLifecycle(Lifecycle.stable());
	public static final Codec<MultiNoiseBiomeSource> CODEC = Codec.mapEither(DIRECT_CODEC, PRESET_CODEC)
		.<MultiNoiseBiomeSource>xmap(MultiNoiseBiomeSource::new, multiNoiseBiomeSource -> multiNoiseBiomeSource.parameters)
		.codec();
	private final Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> parameters;

	private MultiNoiseBiomeSource(Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> either) {
		this.parameters = either;
	}

	public static MultiNoiseBiomeSource createFromList(Climate.ParameterList<Holder<Biome>> parameterList) {
		return new MultiNoiseBiomeSource(Either.left(parameterList));
	}

	public static MultiNoiseBiomeSource createFromPreset(Holder<MultiNoiseBiomeSourceParameterList> holder) {
		return new MultiNoiseBiomeSource(Either.right(holder));
	}

	private Climate.ParameterList<Holder<Biome>> parameters() {
		return this.parameters.map(parameterList -> parameterList, holder -> ((MultiNoiseBiomeSourceParameterList)holder.value()).parameters());
	}

	@Override
	protected Stream<Holder<Biome>> collectPossibleBiomes() {
		return this.parameters().values().stream().map(Pair::getSecond);
	}

	@Override
	protected Codec<? extends BiomeSource> codec() {
		return CODEC;
	}

	public boolean stable(ResourceKey<MultiNoiseBiomeSourceParameterList> resourceKey) {
		Optional<Holder<MultiNoiseBiomeSourceParameterList>> optional = this.parameters.right();
		return optional.isPresent() && ((Holder)optional.get()).is(resourceKey);
	}

	@Override
	public Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler sampler) {
		return this.getNoiseBiome(sampler.sample(i, j, k));
	}

	@VisibleForDebug
	public Holder<Biome> getNoiseBiome(Climate.TargetPoint targetPoint) {
		return this.parameters().findValue(targetPoint);
	}

	@Override
	public void addDebugInfo(List<String> list, BlockPos blockPos, Climate.Sampler sampler) {
		int i = QuartPos.fromBlock(blockPos.getX());
		int j = QuartPos.fromBlock(blockPos.getY());
		int k = QuartPos.fromBlock(blockPos.getZ());
		Climate.TargetPoint targetPoint = sampler.sample(i, j, k);
		float f = Climate.unquantizeCoord(targetPoint.continentalness());
		float g = Climate.unquantizeCoord(targetPoint.erosion());
		float h = Climate.unquantizeCoord(targetPoint.temperature());
		float l = Climate.unquantizeCoord(targetPoint.humidity());
		float m = Climate.unquantizeCoord(targetPoint.weirdness());
		double d = (double)NoiseRouterData.peaksAndValleys(m);
		OverworldBiomeBuilder overworldBiomeBuilder = new OverworldBiomeBuilder();
		list.add(
			"Biome builder PV: "
				+ OverworldBiomeBuilder.getDebugStringForPeaksAndValleys(d)
				+ " C: "
				+ overworldBiomeBuilder.getDebugStringForContinentalness((double)f)
				+ " E: "
				+ overworldBiomeBuilder.getDebugStringForErosion((double)g)
				+ " T: "
				+ overworldBiomeBuilder.getDebugStringForTemperature((double)h)
				+ " H: "
				+ overworldBiomeBuilder.getDebugStringForHumidity((double)l)
		);
	}
}
