/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.TheEndBiomeSourceSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.Nullable;

public class TheEndBiomeSource
extends BiomeSource {
    private final SimplexNoise islandNoise;
    private final WorldgenRandom random;
    private final Biome[] possibleBiomes = new Biome[]{Biomes.THE_END, Biomes.END_HIGHLANDS, Biomes.END_MIDLANDS, Biomes.SMALL_END_ISLANDS, Biomes.END_BARRENS};

    public TheEndBiomeSource(TheEndBiomeSourceSettings theEndBiomeSourceSettings) {
        this.random = new WorldgenRandom(theEndBiomeSourceSettings.getSeed());
        this.random.consumeCount(17292);
        this.islandNoise = new SimplexNoise(this.random);
    }

    @Override
    public Biome getBiome(int i, int j) {
        int k = i >> 4;
        int l = j >> 4;
        if ((long)k * (long)k + (long)l * (long)l <= 4096L) {
            return Biomes.THE_END;
        }
        float f = this.getHeightValue(k * 2 + 1, l * 2 + 1);
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

    @Override
    public Biome[] getBiomeBlock(int i, int j, int k, int l, boolean bl) {
        Biome[] biomes = new Biome[k * l];
        Long2ObjectOpenHashMap<Biome> long2ObjectMap = new Long2ObjectOpenHashMap<Biome>();
        for (int m = 0; m < k; ++m) {
            for (int n = 0; n < l; ++n) {
                int o = m + i;
                int p = n + j;
                long q = ChunkPos.asLong(o, p);
                Biome biome = (Biome)long2ObjectMap.get(q);
                if (biome == null) {
                    biome = this.getBiome(o, p);
                    long2ObjectMap.put(q, biome);
                }
                biomes[m + n * k] = biome;
            }
        }
        return biomes;
    }

    @Override
    public Set<Biome> getBiomesWithin(int i, int j, int k) {
        int l = i - k >> 2;
        int m = j - k >> 2;
        int n = i + k >> 2;
        int o = j + k >> 2;
        int p = n - l + 1;
        int q = o - m + 1;
        return Sets.newHashSet(this.getBiomeBlock(l, m, p, q));
    }

    @Override
    @Nullable
    public BlockPos findBiome(int i, int j, int k, List<Biome> list, Random random) {
        int l = i - k >> 2;
        int m = j - k >> 2;
        int n = i + k >> 2;
        int o = j + k >> 2;
        int p = n - l + 1;
        int q = o - m + 1;
        Biome[] biomes = this.getBiomeBlock(l, m, p, q);
        BlockPos blockPos = null;
        int r = 0;
        for (int s = 0; s < p * q; ++s) {
            int t = l + s % p << 2;
            int u = m + s / p << 2;
            if (!list.contains(biomes[s])) continue;
            if (blockPos == null || random.nextInt(r + 1) == 0) {
                blockPos = new BlockPos(t, 0, u);
            }
            ++r;
        }
        return blockPos;
    }

    @Override
    public float getHeightValue(int i, int j) {
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
                if (q * q + r * r <= 4096L || !(this.islandNoise.getValue(q, r) < (double)-0.9f)) continue;
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

    @Override
    public boolean canGenerateStructure(StructureFeature<?> structureFeature2) {
        return this.supportedStructures.computeIfAbsent(structureFeature2, structureFeature -> {
            for (Biome biome : this.possibleBiomes) {
                if (!biome.isValidStart(structureFeature)) continue;
                return true;
            }
            return false;
        });
    }

    @Override
    public Set<BlockState> getSurfaceBlocks() {
        if (this.surfaceBlocks.isEmpty()) {
            for (Biome biome : this.possibleBiomes) {
                this.surfaceBlocks.add(biome.getSurfaceBuilderConfig().getTopMaterial());
            }
        }
        return this.surfaceBlocks;
    }
}

