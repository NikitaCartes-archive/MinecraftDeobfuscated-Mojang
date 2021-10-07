/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class TheEndBiomeSource
extends BiomeSource {
    public static final Codec<TheEndBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(theEndBiomeSource -> theEndBiomeSource.biomes), ((MapCodec)Codec.LONG.fieldOf("seed")).stable().forGetter(theEndBiomeSource -> theEndBiomeSource.seed)).apply((Applicative<TheEndBiomeSource, ?>)instance, instance.stable(TheEndBiomeSource::new)));
    private static final float ISLAND_THRESHOLD = -0.9f;
    public static final int ISLAND_CHUNK_DISTANCE = 64;
    private static final long ISLAND_CHUNK_DISTANCE_SQR = 4096L;
    private final SimplexNoise islandNoise;
    private final Registry<Biome> biomes;
    private final long seed;
    private final Biome end;
    private final Biome highlands;
    private final Biome midlands;
    private final Biome islands;
    private final Biome barrens;

    public TheEndBiomeSource(Registry<Biome> registry, long l) {
        this(registry, l, registry.getOrThrow(Biomes.THE_END), registry.getOrThrow(Biomes.END_HIGHLANDS), registry.getOrThrow(Biomes.END_MIDLANDS), registry.getOrThrow(Biomes.SMALL_END_ISLANDS), registry.getOrThrow(Biomes.END_BARRENS));
    }

    private TheEndBiomeSource(Registry<Biome> registry, long l, Biome biome, Biome biome2, Biome biome3, Biome biome4, Biome biome5) {
        super(ImmutableList.of(biome, biome2, biome3, biome4, biome5));
        this.biomes = registry;
        this.seed = l;
        this.end = biome;
        this.highlands = biome2;
        this.midlands = biome3;
        this.islands = biome4;
        this.barrens = biome5;
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(l));
        worldgenRandom.consumeCount(17292);
        this.islandNoise = new SimplexNoise(worldgenRandom);
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public BiomeSource withSeed(long l) {
        return new TheEndBiomeSource(this.biomes, l, this.end, this.highlands, this.midlands, this.islands, this.barrens);
    }

    @Override
    public Biome getNoiseBiome(int i, int j, int k, Climate.Sampler sampler) {
        int l = i >> 2;
        int m = k >> 2;
        if ((long)l * (long)l + (long)m * (long)m <= 4096L) {
            return this.end;
        }
        float f = TheEndBiomeSource.getHeightValue(this.islandNoise, l * 2 + 1, m * 2 + 1);
        if (f > 40.0f) {
            return this.highlands;
        }
        if (f >= 0.0f) {
            return this.midlands;
        }
        if (f < -20.0f) {
            return this.islands;
        }
        return this.barrens;
    }

    public boolean stable(long l) {
        return this.seed == l;
    }

    public static float getHeightValue(SimplexNoise simplexNoise, int i, int j) {
        int k = i / 2;
        int l = j / 2;
        int m = i % 2;
        int n = j % 2;
        float f = 100.0f - Mth.sqrt(i * i + j * j) * 8.0f;
        f = Mth.clamp(f, -100.0f, 80.0f);
        for (int o = -12; o <= 12; ++o) {
            for (int p = -12; p <= 12; ++p) {
                long q = k + o;
                long r = l + p;
                if (q * q + r * r <= 4096L || !(simplexNoise.getValue(q, r) < (double)-0.9f)) continue;
                float g = (Mth.abs(q) * 3439.0f + Mth.abs(r) * 147.0f) % 13.0f + 9.0f;
                float h = m - o * 2;
                float s = n - p * 2;
                float t = 100.0f - Mth.sqrt(h * h + s * s) * g;
                t = Mth.clamp(t, -100.0f, 80.0f);
                f = Math.max(f, t);
            }
        }
        return f;
    }
}

