/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
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

    public static MultiNoiseBiomeSource of(long l, List<Biome> list) {
        return new MultiNoiseBiomeSource(l, list.stream().flatMap(biome -> biome.optimalParameters().map(climateParameters -> Pair.of(climateParameters, biome))).collect(ImmutableList.toImmutableList()));
    }

    public MultiNoiseBiomeSource(long l, List<Pair<Biome.ClimateParameters, Biome>> list) {
        super(list.stream().map(Pair::getSecond).collect(Collectors.toSet()));
        IntStream intStream = IntStream.rangeClosed(-7, -6);
        IntStream intStream2 = IntStream.rangeClosed(-7, -6);
        IntStream intStream3 = IntStream.rangeClosed(-7, -6);
        IntStream intStream4 = IntStream.rangeClosed(-7, -6);
        this.temperatureNoise = new NormalNoise(new WorldgenRandom(l), intStream);
        this.humidityNoise = new NormalNoise(new WorldgenRandom(l + 1L), intStream2);
        this.altitudeNoise = new NormalNoise(new WorldgenRandom(l + 2L), intStream3);
        this.weirdnessNoise = new NormalNoise(new WorldgenRandom(l + 3L), intStream4);
        this.parameters = list;
        this.useY = false;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public BiomeSource withSeed(long l) {
        return new MultiNoiseBiomeSource(l, this.parameters);
    }

    @Override
    public Biome getNoiseBiome(int i, int j, int k) {
        int l = this.useY ? j : 0;
        Biome.ClimateParameters climateParameters = new Biome.ClimateParameters((float)this.temperatureNoise.getValue(i, l, k), (float)this.humidityNoise.getValue(i, l, k), (float)this.altitudeNoise.getValue(i, l, k), (float)this.weirdnessNoise.getValue(i, l, k), 0.0f);
        return this.parameters.stream().min(Comparator.comparing(pair -> Float.valueOf(((Biome.ClimateParameters)pair.getFirst()).fitness(climateParameters)))).map(Pair::getSecond).orElse(Biomes.THE_VOID);
    }
}

