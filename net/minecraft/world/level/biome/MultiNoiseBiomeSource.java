/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.mojang.datafixers.util.Pair;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class MultiNoiseBiomeSource
extends BiomeSource {
    private final NormalNoise temperatureNoise;
    private final NormalNoise humidityNoise;
    private final NormalNoise altitudeNoise;
    private final NormalNoise weirdnessNoise;
    private final List<Pair<Biome.ClimateParameters, Biome>> parameters;
    private final boolean useY;

    public MultiNoiseBiomeSource(MultiNoiseBiomeSourceSettings multiNoiseBiomeSourceSettings) {
        super(multiNoiseBiomeSourceSettings.getParameters().stream().map(Pair::getSecond).collect(Collectors.toSet()));
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
        Biome.ClimateParameters climateParameters = new Biome.ClimateParameters((float)this.temperatureNoise.getValue(i, l, k), (float)this.humidityNoise.getValue(i, l, k), (float)this.altitudeNoise.getValue(i, l, k), (float)this.weirdnessNoise.getValue(i, l, k), 0.0f);
        return this.parameters.stream().min(Comparator.comparing(pair -> Float.valueOf(((Biome.ClimateParameters)pair.getFirst()).fitness(climateParameters)))).map(Pair::getSecond).orElse(Biomes.THE_VOID);
    }
}

