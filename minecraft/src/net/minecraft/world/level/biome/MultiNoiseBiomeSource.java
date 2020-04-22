package net.minecraft.world.level.biome;

import com.mojang.datafixers.util.Pair;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class MultiNoiseBiomeSource extends BiomeSource {
	private final NormalNoise temperatureNoise;
	private final NormalNoise humidityNoise;
	private final NormalNoise altitudeNoise;
	private final NormalNoise weirdnessNoise;
	private final List<Pair<Biome.ClimateParameters, Biome>> parameters;
	private final boolean useY;

	public MultiNoiseBiomeSource(MultiNoiseBiomeSourceSettings multiNoiseBiomeSourceSettings) {
		super((Set<Biome>)multiNoiseBiomeSourceSettings.getParameters().stream().map(Pair::getSecond).collect(Collectors.toSet()));
		long l = multiNoiseBiomeSourceSettings.getSeed();
		this.temperatureNoise = new NormalNoise(new WorldgenRandom(l), multiNoiseBiomeSourceSettings.getTemperatureOctaves());
		this.humidityNoise = new NormalNoise(new WorldgenRandom(l + 1L), multiNoiseBiomeSourceSettings.getHumidityOctaves());
		this.altitudeNoise = new NormalNoise(new WorldgenRandom(l + 2L), multiNoiseBiomeSourceSettings.getAltitudeOctaves());
		this.weirdnessNoise = new NormalNoise(new WorldgenRandom(l + 3L), multiNoiseBiomeSourceSettings.getWeirdnessOctaves());
		this.parameters = multiNoiseBiomeSourceSettings.getParameters();
		this.useY = multiNoiseBiomeSourceSettings.useY();
	}

	@Override
	public Biome getNoiseBiome(int i, int j, int k) {
		int l = this.useY ? j : 0;
		Biome.ClimateParameters climateParameters = new Biome.ClimateParameters(
			(float)this.temperatureNoise.getValue((double)i, (double)l, (double)k),
			(float)this.humidityNoise.getValue((double)i, (double)l, (double)k),
			(float)this.altitudeNoise.getValue((double)i, (double)l, (double)k),
			(float)this.weirdnessNoise.getValue((double)i, (double)l, (double)k),
			0.0F
		);
		return (Biome)this.parameters
			.stream()
			.min(Comparator.comparing(pair -> ((Biome.ClimateParameters)pair.getFirst()).fitness(climateParameters)))
			.map(Pair::getSecond)
			.orElse(Biomes.THE_VOID);
	}
}
