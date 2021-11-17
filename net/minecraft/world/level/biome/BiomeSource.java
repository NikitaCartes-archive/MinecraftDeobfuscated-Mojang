/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Graph;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

public abstract class BiomeSource
implements BiomeResolver {
    public static final Codec<BiomeSource> CODEC;
    private final List<Biome> possibleBiomes;
    private final List<StepFeatureData> featuresPerStep;

    protected BiomeSource(Stream<Supplier<Biome>> stream) {
        this(stream.map(Supplier::get).distinct().collect(ImmutableList.toImmutableList()));
    }

    protected BiomeSource(List<Biome> list) {
        this.possibleBiomes = list;
        this.featuresPerStep = this.buildFeaturesPerStep(list, true);
    }

    private List<StepFeatureData> buildFeaturesPerStep(List<Biome> list, boolean bl) {
        record FeatureData(int featureIndex, int step, PlacedFeature feature) {
        }
        ArrayList<FeatureData> list2;
        Object2IntOpenHashMap<PlacedFeature> object2IntMap = new Object2IntOpenHashMap<PlacedFeature>();
        MutableInt mutableInt = new MutableInt(0);
        Comparator<FeatureData> comparator = Comparator.comparingInt(FeatureData::step).thenComparingInt(FeatureData::featureIndex);
        TreeMap<FeatureData, Set> map = new TreeMap<FeatureData, Set>(comparator);
        int i = 0;
        for (Biome biome : list) {
            int j;
            list2 = Lists.newArrayList();
            List<List<Supplier<PlacedFeature>>> list3 = biome.getGenerationSettings().features();
            i = Math.max(i, list3.size());
            for (j = 0; j < list3.size(); ++j) {
                for (Supplier supplier : (List)list3.get(j)) {
                    PlacedFeature placedFeature = (PlacedFeature)supplier.get();
                    list2.add(new FeatureData(object2IntMap.computeIfAbsent(placedFeature, object -> mutableInt.getAndIncrement()), j, placedFeature));
                }
            }
            for (j = 0; j < list2.size(); ++j) {
                Set set = map.computeIfAbsent((FeatureData)list2.get(j), arg -> new TreeSet(comparator));
                if (j >= list2.size() - 1) continue;
                set.add((FeatureData)list2.get(j + 1));
            }
        }
        TreeSet<FeatureData> set2 = new TreeSet<FeatureData>(comparator);
        TreeSet<FeatureData> set3 = new TreeSet<FeatureData>(comparator);
        list2 = Lists.newArrayList();
        for (FeatureData lv : map.keySet()) {
            if (!set3.isEmpty()) {
                throw new IllegalStateException("You somehow broke the universe; DFS bork (iteration finished with non-empty in-progress vertex set");
            }
            if (set2.contains(lv) || !Graph.depthFirstSearch(map, set2, set3, list2::add, lv)) continue;
            if (bl) {
                int k;
                ArrayList<Biome> list4 = new ArrayList<Biome>(list);
                do {
                    k = list4.size();
                    ListIterator<Biome> listIterator = list4.listIterator();
                    while (listIterator.hasNext()) {
                        Biome biome2 = (Biome)listIterator.next();
                        listIterator.remove();
                        try {
                            this.buildFeaturesPerStep(list4, false);
                        } catch (IllegalStateException illegalStateException) {
                            continue;
                        }
                        listIterator.add(biome2);
                    }
                } while (k != list4.size());
                throw new IllegalStateException("Feature order cycle found, involved biomes: " + list4);
            }
            throw new IllegalStateException("Feature order cycle found");
        }
        Collections.reverse(list2);
        ImmutableList.Builder builder = ImmutableList.builder();
        for (int j = 0; j < i; ++j) {
            int l = j;
            List<PlacedFeature> list5 = list2.stream().filter(arg -> arg.step() == l).map(FeatureData::feature).collect(Collectors.toList());
            int m = list5.size();
            Object2IntOpenCustomHashMap<PlacedFeature> object2IntMap2 = new Object2IntOpenCustomHashMap<PlacedFeature>(m, Util.identityStrategy());
            for (int n = 0; n < m; ++n) {
                object2IntMap2.put((PlacedFeature)list5.get(n), n);
            }
            builder.add(new StepFeatureData(list5, object2IntMap2));
        }
        return builder.build();
    }

    protected abstract Codec<? extends BiomeSource> codec();

    public abstract BiomeSource withSeed(long var1);

    public List<Biome> possibleBiomes() {
        return this.possibleBiomes;
    }

    public Set<Biome> getBiomesWithin(int i, int j, int k, int l, Climate.Sampler sampler) {
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
                    set.add(this.getNoiseBiome(y, z, aa, sampler));
                }
            }
        }
        return set;
    }

    @Nullable
    public BlockPos findBiomeHorizontal(int i, int j, int k, int l, Predicate<Biome> predicate, Random random, Climate.Sampler sampler) {
        return this.findBiomeHorizontal(i, j, k, l, 1, predicate, random, false, sampler);
    }

    @Nullable
    public BlockPos findBiomeHorizontal(int i, int j, int k, int l, int m, Predicate<Biome> predicate, Random random, boolean bl, Climate.Sampler sampler) {
        int s;
        int n = QuartPos.fromBlock(i);
        int o = QuartPos.fromBlock(k);
        int p = QuartPos.fromBlock(l);
        int q = QuartPos.fromBlock(j);
        BlockPos blockPos = null;
        int r = 0;
        for (int t = s = bl ? 0 : p; t <= p; t += m) {
            int u;
            int n2 = u = SharedConstants.debugGenerateSquareTerrainWithoutNoise ? 0 : -t;
            while (u <= t) {
                boolean bl2 = Math.abs(u) == t;
                for (int v = -t; v <= t; v += m) {
                    int x;
                    int w;
                    if (bl) {
                        boolean bl3;
                        boolean bl4 = bl3 = Math.abs(v) == t;
                        if (!bl3 && !bl2) continue;
                    }
                    if (!predicate.test(this.getNoiseBiome(w = n + v, q, x = o + u, sampler))) continue;
                    if (blockPos == null || random.nextInt(r + 1) == 0) {
                        blockPos = new BlockPos(QuartPos.toBlock(w), j, QuartPos.toBlock(x));
                        if (bl) {
                            return blockPos;
                        }
                    }
                    ++r;
                }
                u += m;
            }
        }
        return blockPos;
    }

    @Override
    public abstract Biome getNoiseBiome(int var1, int var2, int var3, Climate.Sampler var4);

    public void addMultinoiseDebugInfo(List<String> list, BlockPos blockPos, Climate.Sampler sampler) {
    }

    public List<StepFeatureData> featuresPerStep() {
        return this.featuresPerStep;
    }

    static {
        Registry.register(Registry.BIOME_SOURCE, "fixed", FixedBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "multi_noise", MultiNoiseBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "checkerboard", CheckerboardColumnBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "the_end", TheEndBiomeSource.CODEC);
        CODEC = Registry.BIOME_SOURCE.byNameCodec().dispatchStable(BiomeSource::codec, Function.identity());
    }

    public record StepFeatureData(List<PlacedFeature> features, ToIntFunction<PlacedFeature> indexMapping) {
    }
}

