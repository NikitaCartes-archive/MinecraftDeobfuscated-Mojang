package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Graph;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public abstract class BiomeSource {
	public static final Codec<BiomeSource> CODEC = Registry.BIOME_SOURCE.dispatchStable(BiomeSource::codec, Function.identity());
	private final List<Biome> possibleBiomes;
	private final ImmutableList<ImmutableList<ConfiguredFeature<?, ?>>> featuresPerStep;

	protected BiomeSource(Stream<Supplier<Biome>> stream) {
		this((List<Biome>)stream.map(Supplier::get).collect(ImmutableList.toImmutableList()));
	}

	protected BiomeSource(List<Biome> list) {
		this.possibleBiomes = list;

		record FeatureData() {
			private final int step;
			private final ConfiguredFeature<?, ?> feature;

			FeatureData(int i, ConfiguredFeature<?, ?> configuredFeature) {
				this.step = i;
				this.feature = configuredFeature;
			}
		}

		Map<FeatureData, Set<FeatureData>> map = Maps.<FeatureData, Set<FeatureData>>newHashMap();
		int i = 0;

		for (Biome biome : list) {
			List<FeatureData> list2 = Lists.<FeatureData>newArrayList();
			List<List<Supplier<ConfiguredFeature<?, ?>>>> list3 = biome.getGenerationSettings().features();
			i = Math.max(i, list3.size());

			for (int j = 0; j < list3.size(); j++) {
				for (Supplier<ConfiguredFeature<?, ?>> supplier : (List)list3.get(j)) {
					list2.add(new FeatureData(j, (ConfiguredFeature<?, ?>)supplier.get()));
				}
			}

			for (int j = 0; j < list2.size(); j++) {
				Set<FeatureData> set = (Set<FeatureData>)map.computeIfAbsent((FeatureData)list2.get(j), arg -> Sets.newHashSet());
				if (j < list2.size() - 1) {
					set.add((FeatureData)list2.get(j + 1));
				}
			}
		}

		Set<FeatureData> set2 = Sets.<FeatureData>newHashSet();
		Set<FeatureData> set3 = Sets.<FeatureData>newHashSet();
		List<FeatureData> list2 = Lists.<FeatureData>newArrayList();

		for (FeatureData lv : map.keySet()) {
			if (!set3.isEmpty()) {
				throw new IllegalStateException("You somehow broke the universe; DFS bork (iteration finished with non-empty in-progress vertex set");
			}

			if (!set2.contains(lv) && Graph.depthFirstSearch(map, set2, set3, list2::add, lv)) {
				Collections.reverse(list2);
				throw new IllegalStateException(
					"Feature order cycle found: " + (String)list2.stream().filter(set3::contains).map(Object::toString).collect(Collectors.joining(", "))
				);
			}
		}

		Collections.reverse(list2);
		Builder<ImmutableList<ConfiguredFeature<?, ?>>> builder = ImmutableList.builder();

		for (int jx = 0; jx < i; jx++) {
			int k = jx;
			builder.add(
				(ImmutableList<ConfiguredFeature<?, ?>>)list2.stream().filter(arg -> arg.step() == k).map(FeatureData::feature).collect(ImmutableList.toImmutableList())
			);
		}

		this.featuresPerStep = builder.build();
	}

	protected abstract Codec<? extends BiomeSource> codec();

	public abstract BiomeSource withSeed(long l);

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
		Set<Biome> set = Sets.<Biome>newHashSet();

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
	public BlockPos findBiomeHorizontal(int i, int j, int k, int l, Predicate<Biome> predicate, Random random, Climate.Sampler sampler) {
		return this.findBiomeHorizontal(i, j, k, l, 1, predicate, random, false, sampler);
	}

	@Nullable
	public BlockPos findBiomeHorizontal(int i, int j, int k, int l, int m, Predicate<Biome> predicate, Random random, boolean bl, Climate.Sampler sampler) {
		int n = QuartPos.fromBlock(i);
		int o = QuartPos.fromBlock(k);
		int p = QuartPos.fromBlock(l);
		int q = QuartPos.fromBlock(j);
		BlockPos blockPos = null;
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
					if (predicate.test(this.getNoiseBiome(w, q, x, sampler))) {
						if (blockPos == null || random.nextInt(r + 1) == 0) {
							blockPos = new BlockPos(QuartPos.toBlock(w), j, QuartPos.toBlock(x));
							if (bl) {
								return blockPos;
							}
						}

						r++;
					}
				}
			}

			t += m;
		}

		return blockPos;
	}

	public abstract Biome getNoiseBiome(int i, int j, int k, Climate.Sampler sampler);

	public void addMultinoiseDebugInfo(List<String> list, BlockPos blockPos, Climate.Sampler sampler) {
	}

	public ImmutableList<ImmutableList<ConfiguredFeature<?, ?>>> featuresPerStep() {
		return this.featuresPerStep;
	}

	static {
		Registry.register(Registry.BIOME_SOURCE, "fixed", FixedBiomeSource.CODEC);
		Registry.register(Registry.BIOME_SOURCE, "multi_noise", MultiNoiseBiomeSource.CODEC);
		Registry.register(Registry.BIOME_SOURCE, "checkerboard", CheckerboardColumnBiomeSource.CODEC);
		Registry.register(Registry.BIOME_SOURCE, "the_end", TheEndBiomeSource.CODEC);
	}
}
