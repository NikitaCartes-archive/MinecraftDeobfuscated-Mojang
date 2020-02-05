package net.minecraft.world.level.biome;

import java.util.Comparator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class MultiNoiseBiomeSource extends BiomeSource {
	private final PerlinNoise temperatureNoise;
	private final PerlinNoise humidityNoise;
	private final PerlinNoise altitudeNoise;
	private final PerlinNoise weirdnessNoise;

	public MultiNoiseBiomeSource(MultiNoiseBiomeSourceSettings multiNoiseBiomeSourceSettings) {
		super(multiNoiseBiomeSourceSettings.getBiomes());
		long l = multiNoiseBiomeSourceSettings.getSeed();
		this.temperatureNoise = new PerlinNoise(new WorldgenRandom(l), multiNoiseBiomeSourceSettings.getTemperatureOctaves());
		this.humidityNoise = new PerlinNoise(new WorldgenRandom(l + 1L), multiNoiseBiomeSourceSettings.getHumidityOctaves());
		this.altitudeNoise = new PerlinNoise(new WorldgenRandom(l + 2L), multiNoiseBiomeSourceSettings.getAltitudeOctaves());
		this.weirdnessNoise = new PerlinNoise(new WorldgenRandom(l + 3L), multiNoiseBiomeSourceSettings.getWeirdnessOctaves());
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
		return (Biome)this.possibleBiomes.stream().min(Comparator.comparing(biome -> biome.getFitness(climateParameters))).orElse(Biomes.THE_END);
	}
}
