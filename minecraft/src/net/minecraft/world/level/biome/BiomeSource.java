package net.minecraft.world.level.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
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
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Graph;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;

public abstract class BiomeSource implements BiomeResolver {
	public static final Codec<BiomeSource> CODEC = Registry.BIOME_SOURCE.byNameCodec().dispatchStable(BiomeSource::codec, Function.identity());
	private final Set<Holder<Biome>> possibleBiomes;
	private final Supplier<List<BiomeSource.StepFeatureData>> featuresPerStep;

	protected BiomeSource(Stream<Holder<Biome>> stream) {
		this(stream.distinct().toList());
	}

	protected BiomeSource(List<Holder<Biome>> list) {
		this.possibleBiomes = new ObjectLinkedOpenHashSet<>(list);
		this.featuresPerStep = Suppliers.memoize(() -> this.buildFeaturesPerStep(list, true));
	}

	private List<BiomeSource.StepFeatureData> buildFeaturesPerStep(List<Holder<Biome>> list, boolean bl) {
		Object2IntMap<PlacedFeature> object2IntMap = new Object2IntOpenHashMap<>();
		MutableInt mutableInt = new MutableInt(0);

		record FeatureData(int featureIndex, int step, PlacedFeature feature) {
		}

		Comparator<FeatureData> comparator = Comparator.comparingInt(FeatureData::step).thenComparingInt(FeatureData::featureIndex);
		Map<FeatureData, Set<FeatureData>> map = new TreeMap(comparator);
		int i = 0;

		for (Holder<Biome> holder : list) {
			Biome biome = holder.value();
			List<FeatureData> list2 = Lists.<FeatureData>newArrayList();
			List<HolderSet<PlacedFeature>> list3 = biome.getGenerationSettings().features();
			i = Math.max(i, list3.size());

			for (int j = 0; j < list3.size(); j++) {
				for (Holder<PlacedFeature> holder2 : (HolderSet)list3.get(j)) {
					PlacedFeature placedFeature = holder2.value();
					list2.add(
						new FeatureData(
							object2IntMap.computeIfAbsent(placedFeature, (Object2IntFunction<? super PlacedFeature>)(object -> mutableInt.getAndIncrement())), j, placedFeature
						)
					);
				}
			}

			for (int j = 0; j < list2.size(); j++) {
				Set<FeatureData> set = (Set<FeatureData>)map.computeIfAbsent((FeatureData)list2.get(j), arg -> new TreeSet(comparator));
				if (j < list2.size() - 1) {
					set.add((FeatureData)list2.get(j + 1));
				}
			}
		}

		Set<FeatureData> set2 = new TreeSet(comparator);
		Set<FeatureData> set3 = new TreeSet(comparator);
		List<FeatureData> list4 = Lists.<FeatureData>newArrayList();

		for (FeatureData lv : map.keySet()) {
			if (!set3.isEmpty()) {
				throw new IllegalStateException("You somehow broke the universe; DFS bork (iteration finished with non-empty in-progress vertex set");
			}

			if (!set2.contains(lv) && Graph.depthFirstSearch(map, set2, set3, list4::add, lv)) {
				if (!bl) {
					throw new IllegalStateException("Feature order cycle found");
				}

				List<Holder<Biome>> list5 = new ArrayList(list);

				int k;
				do {
					k = list5.size();
					ListIterator<Holder<Biome>> listIterator = list5.listIterator();

					while (listIterator.hasNext()) {
						Holder<Biome> holder3 = (Holder<Biome>)listIterator.next();
						listIterator.remove();

						try {
							this.buildFeaturesPerStep(list5, false);
						} catch (IllegalStateException var18) {
							continue;
						}

						listIterator.add(holder3);
					}
				} while (k != list5.size());

				throw new IllegalStateException("Feature order cycle found, involved biomes: " + list5);
			}
		}

		Collections.reverse(list4);
		Builder<BiomeSource.StepFeatureData> builder = ImmutableList.builder();

		for (int l = 0; l < i; l++) {
			int jx = l;
			List<PlacedFeature> list6 = (List<PlacedFeature>)list4.stream().filter(arg -> arg.step() == j).map(FeatureData::feature).collect(Collectors.toList());
			int m = list6.size();
			Object2IntMap<PlacedFeature> object2IntMap2 = new Object2IntOpenCustomHashMap<>(m, Util.identityStrategy());

			for (int n = 0; n < m; n++) {
				object2IntMap2.put((PlacedFeature)list6.get(n), n);
			}

			builder.add(new BiomeSource.StepFeatureData(list6, object2IntMap2));
		}

		return builder.build();
	}

	protected abstract Codec<? extends BiomeSource> codec();

	public abstract BiomeSource withSeed(long l);

	public Set<Holder<Biome>> possibleBiomes() {
		return this.possibleBiomes;
	}

	public Set<Holder<Biome>> getBiomesWithin(int i, int j, int k, int l, Climate.Sampler sampler) {
		int m = QuartPos.fromBlock(i - l);
		int n = QuartPos.fromBlock(j - l);
		int o = QuartPos.fromBlock(k - l);
		int p = QuartPos.fromBlock(i + l);
		int q = QuartPos.fromBlock(j + l);
		int r = QuartPos.fromBlock(k + l);
		int s = p - m + 1;
		int t = q - n + 1;
		int u = r - o + 1;
		Set<Holder<Biome>> set = Sets.<Holder<Biome>>newHashSet();

		for (int v = 0; v < u; v++) {
			for (int w = 0; w < s; w++) {
				for (int x = 0; x < t; x++) {
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
	public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(
		int i, int j, int k, int l, Predicate<Holder<Biome>> predicate, Random random, Climate.Sampler sampler
	) {
		return this.findBiomeHorizontal(i, j, k, l, 1, predicate, random, false, sampler);
	}

	@Nullable
	public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(
		int i, int j, int k, int l, int m, Predicate<Holder<Biome>> predicate, Random random, boolean bl, Climate.Sampler sampler
	) {
		int n = QuartPos.fromBlock(i);
		int o = QuartPos.fromBlock(k);
		int p = QuartPos.fromBlock(l);
		int q = QuartPos.fromBlock(j);
		Pair<BlockPos, Holder<Biome>> pair = null;
		int r = 0;
		int s = bl ? 0 : p;
		int t = s;

		while (t <= p) {
			for (int u = SharedConstants.debugGenerateSquareTerrainWithoutNoise ? 0 : -t; u <= t; u += m) {
				boolean bl2 = Math.abs(u) == t;

				for (int v = -t; v <= t; v += m) {
					if (bl) {
						boolean bl3 = Math.abs(v) == t;
						if (!bl3 && !bl2) {
							continue;
						}
					}

					int w = n + v;
					int x = o + u;
					Holder<Biome> holder = this.getNoiseBiome(w, q, x, sampler);
					if (predicate.test(holder)) {
						if (pair == null || random.nextInt(r + 1) == 0) {
							BlockPos blockPos = new BlockPos(QuartPos.toBlock(w), j, QuartPos.toBlock(x));
							if (bl) {
								return Pair.of(blockPos, holder);
							}

							pair = Pair.of(blockPos, holder);
						}

						r++;
					}
				}
			}

			t += m;
		}

		return pair;
	}

	@Override
	public abstract Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler sampler);

	public void addDebugInfo(List<String> list, BlockPos blockPos, Climate.Sampler sampler) {
	}

	public List<BiomeSource.StepFeatureData> featuresPerStep() {
		return (List<BiomeSource.StepFeatureData>)this.featuresPerStep.get();
	}

	static {
		Registry.register(Registry.BIOME_SOURCE, "fixed", FixedBiomeSource.CODEC);
		Registry.register(Registry.BIOME_SOURCE, "multi_noise", MultiNoiseBiomeSource.CODEC);
		Registry.register(Registry.BIOME_SOURCE, "checkerboard", CheckerboardColumnBiomeSource.CODEC);
		Registry.register(Registry.BIOME_SOURCE, "the_end", TheEndBiomeSource.CODEC);
	}

	public static record StepFeatureData(List<PlacedFeature> features, ToIntFunction<PlacedFeature> indexMapping) {
	}
}
