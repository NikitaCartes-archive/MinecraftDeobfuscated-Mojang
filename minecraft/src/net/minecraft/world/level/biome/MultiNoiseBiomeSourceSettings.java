package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.stream.IntStream;

public class MultiNoiseBiomeSourceSettings implements BiomeSourceSettings {
	private final long seed;
	private ImmutableList<Integer> temperatureOctaves = (ImmutableList<Integer>)IntStream.rangeClosed(-7, -6).boxed().collect(ImmutableList.toImmutableList());
	private ImmutableList<Integer> humidityOctaves = (ImmutableList<Integer>)IntStream.rangeClosed(-7, -6).boxed().collect(ImmutableList.toImmutableList());
	private ImmutableList<Integer> altitudeOctaves = (ImmutableList<Integer>)IntStream.rangeClosed(-7, -6).boxed().collect(ImmutableList.toImmutableList());
	private ImmutableList<Integer> weirdnessOctaves = (ImmutableList<Integer>)IntStream.rangeClosed(-7, -6).boxed().collect(ImmutableList.toImmutableList());
	private boolean useY;
	private List<Pair<Biome.ClimateParameters, Biome>> parameters = ImmutableList.of();

	public MultiNoiseBiomeSourceSettings(long l) {
		this.seed = l;
	}

	public MultiNoiseBiomeSourceSettings setBiomes(List<Biome> list) {
		return this.setParameters(
			(List<Pair<Biome.ClimateParameters, Biome>>)list.stream()
				.flatMap(biome -> biome.optimalParameters().map(climateParameters -> Pair.of(climateParameters, biome)))
				.collect(ImmutableList.toImmutableList())
		);
	}

	public MultiNoiseBiomeSourceSettings setParameters(List<Pair<Biome.ClimateParameters, Biome>> list) {
		this.parameters = list;
		return this;
	}

	public List<Pair<Biome.ClimateParameters, Biome>> getParameters() {
		return this.parameters;
	}

	public long getSeed() {
		return this.seed;
	}

	public ImmutableList<Integer> getTemperatureOctaves() {
		return this.temperatureOctaves;
	}

	public ImmutableList<Integer> getHumidityOctaves() {
		return this.humidityOctaves;
	}

	public ImmutableList<Integer> getAltitudeOctaves() {
		return this.altitudeOctaves;
	}

	public ImmutableList<Integer> getWeirdnessOctaves() {
		return this.weirdnessOctaves;
	}

	public boolean useY() {
		return this.useY;
	}
}
