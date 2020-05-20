/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class MultiNoiseBiomeSource
extends BiomeSource {
    public static final MapCodec<MultiNoiseBiomeSource> DIRECT_CODEC = RecordCodecBuilder.mapCodec(instance2 -> instance2.group(((MapCodec)Codec.LONG.fieldOf("seed")).forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.seed), ((MapCodec)RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Biome.ClimateParameters.CODEC.fieldOf("parameters")).forGetter(Pair::getFirst), ((MapCodec)Registry.BIOME.fieldOf("biome")).forGetter(Pair::getSecond)).apply((Applicative<Pair, ?>)instance, Pair::of)).listOf().fieldOf("biomes")).forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.parameters)).apply((Applicative<MultiNoiseBiomeSource, ?>)instance2, MultiNoiseBiomeSource::new));
    public static final Codec<MultiNoiseBiomeSource> CODEC = Codec.mapEither(Preset.CODEC, DIRECT_CODEC).xmap(either -> either.map(pair -> ((Preset)pair.getFirst()).biomeSource((Long)pair.getSecond()), Function.identity()), multiNoiseBiomeSource -> multiNoiseBiomeSource.preset.map(preset -> Either.left(Pair.of(preset, multiNoiseBiomeSource.seed))).orElseGet(() -> Either.right(multiNoiseBiomeSource))).codec();
    private final NormalNoise temperatureNoise;
    private final NormalNoise humidityNoise;
    private final NormalNoise altitudeNoise;
    private final NormalNoise weirdnessNoise;
    private final List<Pair<Biome.ClimateParameters, Biome>> parameters;
    private final boolean useY;
    private final long seed;
    private final Optional<Preset> preset;

    private MultiNoiseBiomeSource(long l, List<Pair<Biome.ClimateParameters, Biome>> list) {
        this(l, list, Optional.empty());
    }

    public MultiNoiseBiomeSource(long l, List<Pair<Biome.ClimateParameters, Biome>> list, Optional<Preset> optional) {
        super(list.stream().map(Pair::getSecond).collect(Collectors.toList()));
        this.seed = l;
        this.preset = optional;
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

    private static MultiNoiseBiomeSource defaultNether(long l) {
        ImmutableList<Biome> immutableList = ImmutableList.of(Biomes.NETHER_WASTES, Biomes.SOUL_SAND_VALLEY, Biomes.CRIMSON_FOREST, Biomes.WARPED_FOREST, Biomes.BASALT_DELTAS);
        return new MultiNoiseBiomeSource(l, immutableList.stream().flatMap(biome -> biome.optimalParameters().map(climateParameters -> Pair.of(climateParameters, biome))).collect(ImmutableList.toImmutableList()), Optional.of(Preset.NETHER));
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public BiomeSource withSeed(long l) {
        return new MultiNoiseBiomeSource(l, this.parameters, this.preset);
    }

    @Override
    public Biome getNoiseBiome(int i, int j, int k) {
        int l = this.useY ? j : 0;
        Biome.ClimateParameters climateParameters = new Biome.ClimateParameters((float)this.temperatureNoise.getValue(i, l, k), (float)this.humidityNoise.getValue(i, l, k), (float)this.altitudeNoise.getValue(i, l, k), (float)this.weirdnessNoise.getValue(i, l, k), 0.0f);
        return this.parameters.stream().min(Comparator.comparing(pair -> Float.valueOf(((Biome.ClimateParameters)pair.getFirst()).fitness(climateParameters)))).map(Pair::getSecond).orElse(Biomes.THE_VOID);
    }

    public boolean stable(long l) {
        return this.seed == l && Objects.equals(this.preset, Optional.of(Preset.NETHER));
    }

    public static class Preset {
        private static final Map<ResourceLocation, Preset> BY_NAME = Maps.newHashMap();
        public static final MapCodec<Pair<Preset, Long>> CODEC = Codec.mapPair(ResourceLocation.CODEC.flatXmap(resourceLocation -> Optional.ofNullable(BY_NAME.get(resourceLocation)).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown preset: " + resourceLocation)), preset -> DataResult.success(preset.name)).fieldOf("preset"), Codec.LONG.fieldOf("seed")).stable();
        public static final Preset NETHER = new Preset(new ResourceLocation("nether"), l -> MultiNoiseBiomeSource.method_28465(l));
        private final ResourceLocation name;
        private final LongFunction<MultiNoiseBiomeSource> biomeSource;

        public Preset(ResourceLocation resourceLocation, LongFunction<MultiNoiseBiomeSource> longFunction) {
            this.name = resourceLocation;
            this.biomeSource = longFunction;
            BY_NAME.put(resourceLocation, this);
        }

        public MultiNoiseBiomeSource biomeSource(long l) {
            return this.biomeSource.apply(l);
        }
    }
}

