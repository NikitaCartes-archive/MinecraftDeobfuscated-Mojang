/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
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
    protected final Map<StructureFeature<?>, Boolean> supportedStructures = Maps.newHashMap();
    protected final Set<BlockState> surfaceBlocks = Sets.newHashSet();
    protected final List<Biome> possibleBiomes;

    protected BiomeSource(Stream<Supplier<Biome>> stream) {
        this(stream.map(Supplier::get).collect(ImmutableList.toImmutableList()));
    }

    protected BiomeSource(List<Biome> list) {
        this.possibleBiomes = list;
    }

    protected abstract Codec<? extends BiomeSource> codec();

    public abstract BiomeSource withSeed(long var1);

    public List<Biome> possibleBiomes() {
        return this.possibleBiomes;
    }

    public Set<Biome> getBiomesWithin(int i, int j, int k, int l) {
        int m = QuartPos.fromBlock(i - l);
        int n = QuartPos.fromBlock(j - l);
        int o = QuartPos.fromBlock(k - l);
        int p = QuartPos.fromBlock(i + l);
        int q = QuartPos.fromBlock(j + l);
        int r = QuartPos.fromBlock(k + l);
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
    public BlockPos findBiomeHorizontal(int i, int j, int k, int l, Predicate<Biome> predicate, Random random) {
        return this.findBiomeHorizontal(i, j, k, l, 1, predicate, random, false);
    }

    @Nullable
    public BlockPos findBiomeHorizontal(int i, int j, int k, int l, int m, Predicate<Biome> predicate, Random random, boolean bl) {
        int s;
        int n = QuartPos.fromBlock(i);
        int o = QuartPos.fromBlock(k);
        int p = QuartPos.fromBlock(l);
        int q = QuartPos.fromBlock(j);
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
                    if (!predicate.test(this.getNoiseBiome(w = n + v, q, x = o + u))) continue;
                    if (blockPos == null || random.nextInt(r + 1) == 0) {
                        blockPos = new BlockPos(QuartPos.toBlock(w), j, QuartPos.toBlock(x));
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
    }
}

