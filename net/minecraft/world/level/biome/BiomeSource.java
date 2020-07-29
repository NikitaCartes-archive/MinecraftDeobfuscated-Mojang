/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import org.jetbrains.annotations.Nullable;

public abstract class BiomeSource
implements BiomeManager.NoiseBiomeSource {
    public static final Codec<BiomeSource> CODEC;
    private static final List<Biome> PLAYER_SPAWN_BIOMES;
    protected final Map<StructureFeature<?>, Boolean> supportedStructures = Maps.newHashMap();
    protected final Set<BlockState> surfaceBlocks = Sets.newHashSet();
    protected final List<Biome> possibleBiomes;

    protected BiomeSource(List<Biome> list) {
        this.possibleBiomes = list;
    }

    protected abstract Codec<? extends BiomeSource> codec();

    @Environment(value=EnvType.CLIENT)
    public abstract BiomeSource withSeed(long var1);

    public List<Biome> getPlayerSpawnBiomes() {
        return PLAYER_SPAWN_BIOMES;
    }

    public List<Biome> possibleBiomes() {
        return this.possibleBiomes;
    }

    public Set<Biome> getBiomesWithin(int i, int j, int k, int l) {
        int m = i - l >> 2;
        int n = j - l >> 2;
        int o = k - l >> 2;
        int p = i + l >> 2;
        int q = j + l >> 2;
        int r = k + l >> 2;
        int s = p - m + 1;
        int t = q - n + 1;
        int u = r - o + 1;
        HashSet<Biome> set = Sets.newHashSet();
        for (int v = 0; v < u; ++v) {
            for (int w = 0; w < s; ++w) {
                for (int x = 0; x < t; ++x) {
                    int y = m + w;
                    int z = n + x;
                    int aa = o + v;
                    set.add(this.getNoiseBiome(y, z, aa));
                }
            }
        }
        return set;
    }

    @Nullable
    public BlockPos findBiomeHorizontal(int i, int j, int k, int l, List<Biome> list, Random random) {
        return this.findBiomeHorizontal(i, j, k, l, 1, list, random, false);
    }

    @Nullable
    public BlockPos findBiomeHorizontal(int i, int j, int k, int l, int m, List<Biome> list, Random random, boolean bl) {
        int s;
        int n = i >> 2;
        int o = k >> 2;
        int p = l >> 2;
        int q = j >> 2;
        BlockPos blockPos = null;
        int r = 0;
        for (int t = s = bl ? 0 : p; t <= p; t += m) {
            for (int u = -t; u <= t; u += m) {
                boolean bl2 = Math.abs(u) == t;
                for (int v = -t; v <= t; v += m) {
                    int x;
                    int w;
                    if (bl) {
                        boolean bl3;
                        boolean bl4 = bl3 = Math.abs(v) == t;
                        if (!bl3 && !bl2) continue;
                    }
                    if (!list.contains(this.getNoiseBiome(w = n + v, q, x = o + u))) continue;
                    if (blockPos == null || random.nextInt(r + 1) == 0) {
                        blockPos = new BlockPos(w << 2, j, x << 2);
                        if (bl) {
                            return blockPos;
                        }
                    }
                    ++r;
                }
            }
        }
        return blockPos;
    }

    public boolean canGenerateStructure(StructureFeature<?> structureFeature2) {
        return this.supportedStructures.computeIfAbsent(structureFeature2, structureFeature -> this.possibleBiomes.stream().anyMatch(biome -> biome.getGenerationSettings().isValidStart((StructureFeature<?>)structureFeature)));
    }

    public Set<BlockState> getSurfaceBlocks() {
        if (this.surfaceBlocks.isEmpty()) {
            for (Biome biome : this.possibleBiomes) {
                this.surfaceBlocks.add(biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial());
            }
        }
        return this.surfaceBlocks;
    }

    static {
        Registry.register(Registry.BIOME_SOURCE, "fixed", FixedBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "multi_noise", MultiNoiseBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "checkerboard", CheckerboardColumnBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "vanilla_layered", OverworldBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "the_end", TheEndBiomeSource.CODEC);
        CODEC = Registry.BIOME_SOURCE.dispatchStable(BiomeSource::codec, Function.identity());
        PLAYER_SPAWN_BIOMES = Lists.newArrayList(Biomes.FOREST, Biomes.PLAINS, Biomes.TAIGA, Biomes.TAIGA_HILLS, Biomes.WOODED_HILLS, Biomes.JUNGLE, Biomes.JUNGLE_HILLS);
    }
}

