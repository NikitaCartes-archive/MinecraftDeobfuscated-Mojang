/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class TheEndBiomeSource
extends BiomeSource {
    public static final Codec<TheEndBiomeSource> CODEC = ((MapCodec)Codec.LONG.fieldOf("seed")).xmap(TheEndBiomeSource::new, theEndBiomeSource -> theEndBiomeSource.seed).stable().codec();
    private final SimplexNoise islandNoise;
    private static final List<Biome> POSSIBLE_BIOMES = ImmutableList.of(Biomes.THE_END, Biomes.END_HIGHLANDS, Biomes.END_MIDLANDS, Biomes.SMALL_END_ISLANDS, Biomes.END_BARRENS);
    private final long seed;

    public TheEndBiomeSource(long l) {
        super(POSSIBLE_BIOMES);
        this.seed = l;
        WorldgenRandom worldgenRandom = new WorldgenRandom(l);
        worldgenRandom.consumeCount(17292);
        this.islandNoise = new SimplexNoise(worldgenRandom);
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public BiomeSource withSeed(long l) {
        return new TheEndBiomeSource(l);
    }

    @Override
    public Biome getNoiseBiome(int i, int j, int k) {
        int l = i >> 2;
        int m = k >> 2;
        if ((long)l * (long)l + (long)m * (long)m <= 4096L) {
            return Biomes.THE_END;
        }
        float f = TheEndBiomeSource.getHeightValue(this.islandNoise, l * 2 + 1, m * 2 + 1);
        if (f > 40.0f) {
            return Biomes.END_HIGHLANDS;
        }
        if (f >= 0.0f) {
            return Biomes.END_MIDLANDS;
        }
        if (f < -20.0f) {
            return Biomes.SMALL_END_ISLANDS;
        }
        return Biomes.END_BARRENS;
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

