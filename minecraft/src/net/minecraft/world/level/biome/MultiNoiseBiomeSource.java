package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class MultiNoiseBiomeSource extends BiomeSource {
	private final PerlinNoise temperatureNoise;
	private final PerlinNoise humidityNoise;
	private final PerlinNoise altitudeNoise;
	private final PerlinNoise weirdnessNoise;
	private final Map<Biome, List<Biome.ClimateParameters>> biomePoints;

	public MultiNoiseBiomeSource(MultiNoiseBiomeSourceSettings multiNoiseBiomeSourceSettings) {
		super(multiNoiseBiomeSourceSettings.getBiomes().keySet());
		long l = multiNoiseBiomeSourceSettings.getSeed();
		this.temperatureNoise = new PerlinNoise(new WorldgenRandom(l), multiNoiseBiomeSourceSettings.getTemperatureOctaves());
		this.humidityNoise = new PerlinNoise(new WorldgenRandom(l + 1L), multiNoiseBiomeSourceSettings.getHumidityOctaves());
		this.altitudeNoise = new PerlinNoise(new WorldgenRandom(l + 2L), multiNoiseBiomeSourceSettings.getAltitudeOctaves());
		this.weirdnessNoise = new PerlinNoise(new WorldgenRandom(l + 3L), multiNoiseBiomeSourceSettings.getWeirdnessOctaves());
		this.biomePoints = multiNoiseBiomeSourceSettings.getBiomes();
	}

	private float getFitness(Biome biome, Biome.ClimateParameters climateParameters) {
		return (Float)((List)this.biomePoints.get(biome))
			.stream()
			.map(climateParameters2 -> climateParameters2.fitness(climateParameters))
			.min(Float::compare)
			.orElse(Float.POSITIVE_INFINITY);
	}

	@Override
	public Biome getNoiseBiome(int i, int j, int k) {
		double d = 1.0181268882175227;
		double e = 1.0;
		double f = (double)i * 1.0181268882175227;
		double g = (double)k * 1.0181268882175227;
		double h = (double)i * 1.0;
		double l = (double)k * 1.0;
		Biome.ClimateParameters climateParameters = new Biome.ClimateParameters(
			(float)((this.temperatureNoise.getValue(f, 0.0, g) + this.temperatureNoise.getValue(h, 0.0, l)) * 0.5),
			(float)((this.humidityNoise.getValue(f, 0.0, g) + this.humidityNoise.getValue(h, 0.0, l)) * 0.5),
			(float)((this.altitudeNoise.getValue(f, 0.0, g) + this.altitudeNoise.getValue(h, 0.0, l)) * 0.5),
			(float)((this.weirdnessNoise.getValue(f, 0.0, g) + this.weirdnessNoise.getValue(h, 0.0, l)) * 0.5),
			1.0F
		);
		return (Biome)this.possibleBiomes.stream().min(Comparator.comparing(biome -> this.getFitness(biome, climateParameters))).orElse(Biomes.THE_END);
	}

	@Override
	public BiomeSourceType<?, ?> getType() {
		return BiomeSourceType.MULTI_NOISE;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		T object = dynamicOps.createMap(
			(Map<T, T>)this.biomePoints
				.entrySet()
				.stream()
				.collect(
					ImmutableMap.toImmutableMap(
						entry -> dynamicOps.createString(Registry.BIOME.getKey((Biome)entry.getKey()).toString()),
						entry -> dynamicOps.createList(((List)entry.getValue()).stream().map(climateParameters -> climateParameters.serialize(dynamicOps).getValue()))
					)
				)
		);
		T object2 = dynamicOps.createMap(
			ImmutableMap.<T, T>builder()
				.put(dynamicOps.createString("temperature"), dynamicOps.createList(this.temperatureNoise.getOctaves().stream().map(dynamicOps::createInt)))
				.put(dynamicOps.createString("humidity"), dynamicOps.createList(this.humidityNoise.getOctaves().stream().map(dynamicOps::createInt)))
				.put(dynamicOps.createString("altitude"), dynamicOps.createList(this.altitudeNoise.getOctaves().stream().map(dynamicOps::createInt)))
				.put(dynamicOps.createString("weirdness"), dynamicOps.createList(this.weirdnessNoise.getOctaves().stream().map(dynamicOps::createInt)))
				.build()
		);
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("biomes"), object, dynamicOps.createString("noises"), object2)));
	}
}
