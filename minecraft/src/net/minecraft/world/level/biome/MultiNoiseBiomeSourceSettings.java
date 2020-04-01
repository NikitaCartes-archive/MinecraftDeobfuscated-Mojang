package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class MultiNoiseBiomeSourceSettings implements BiomeSourceSettings {
	private final long seed;
	private ImmutableList<Integer> temperatureOctaves = (ImmutableList<Integer>)IntStream.rangeClosed(-8, -1).boxed().collect(ImmutableList.toImmutableList());
	private ImmutableList<Integer> humidityOctaves = (ImmutableList<Integer>)IntStream.rangeClosed(-8, -1).boxed().collect(ImmutableList.toImmutableList());
	private ImmutableList<Integer> altitudeOctaves = (ImmutableList<Integer>)IntStream.rangeClosed(-9, -1).boxed().collect(ImmutableList.toImmutableList());
	private ImmutableList<Integer> weirdnessOctaves = (ImmutableList<Integer>)IntStream.rangeClosed(-8, -1).boxed().collect(ImmutableList.toImmutableList());
	private Map<Biome, List<Biome.ClimateParameters>> biomes = ImmutableMap.of();

	public MultiNoiseBiomeSourceSettings(long l) {
		this.seed = l;
	}

	public MultiNoiseBiomeSourceSettings setBiomes(Map<Biome, List<Biome.ClimateParameters>> map) {
		this.biomes = map;
		return this;
	}

	public Map<Biome, List<Biome.ClimateParameters>> getBiomes() {
		return this.biomes;
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
}
