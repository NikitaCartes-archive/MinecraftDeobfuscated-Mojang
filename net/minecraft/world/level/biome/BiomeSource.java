/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import org.jetbrains.annotations.Nullable;

public abstract class BiomeSource
implements BiomeManager.NoiseBiomeSource {
    private static final List<Biome> PLAYER_SPAWN_BIOMES = Lists.newArrayList(Biomes.FOREST, Biomes.PLAINS, Biomes.TAIGA, Biomes.TAIGA_HILLS, Biomes.WOODED_HILLS, Biomes.JUNGLE, Biomes.JUNGLE_HILLS);
    protected final Map<StructureFeature<?>, Boolean> supportedStructures = Maps.newHashMap();
    protected final Set<BlockState> surfaceBlocks = Sets.newHashSet();
    protected final Set<Biome> possibleBiomes;

    protected BiomeSource(Set<Biome> set) {
        this.possibleBiomes = set;
    }

    public List<Biome> getPlayerSpawnBiomes() {
        return PLAYER_SPAWN_BIOMES;
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
        int m = i - l >> 2;
        int n = k - l >> 2;
        int o = i + l >> 2;
        int p = k + l >> 2;
        int q = o - m + 1;
        int r = p - n + 1;
        int s = j >> 2;
        BlockPos blockPos = null;
        int t = 0;
        for (int u = 0; u < r; ++u) {
            for (int v = 0; v < q; ++v) {
                int w = m + v;
                int x = n + u;
                if (!list.contains(this.getNoiseBiome(w, s, x))) continue;
                if (blockPos == null || random.nextInt(t + 1) == 0) {
                    blockPos = new BlockPos(w << 2, j, x << 2);
                }
                ++t;
            }
        }
        return blockPos;
    }

    public float getHeightValue(int i, int j) {
        return 0.0f;
    }

    public boolean canGenerateStructure(StructureFeature<?> structureFeature2) {
        return this.supportedStructures.computeIfAbsent(structureFeature2, structureFeature -> this.possibleBiomes.stream().anyMatch(biome -> biome.isValidStart(structureFeature)));
    }

    public Set<BlockState> getSurfaceBlocks() {
        if (this.surfaceBlocks.isEmpty()) {
            for (Biome biome : this.possibleBiomes) {
                this.surfaceBlocks.add(biome.getSurfaceBuilderConfig().getTopMaterial());
            }
        }
        return this.surfaceBlocks;
    }
}

