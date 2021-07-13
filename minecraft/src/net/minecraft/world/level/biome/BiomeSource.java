package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableSet.Builder;
import com.mojang.datafixers.util.Pair;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

public abstract class BiomeSource implements BiomeManager.NoiseBiomeSource {
	public static final Codec<BiomeSource> CODEC = Registry.BIOME_SOURCE.dispatchStable(BiomeSource::codec, Function.identity());
	private final ImmutableSet<StructureFeature<?>> supportedStructures;
	private final ImmutableSet<BlockState> surfaceBlocks;
	private final List<Biome> possibleBiomes;
	private final ImmutableList<List<ConfiguredFeature<?, ?>>> features;

	protected BiomeSource(Stream<Supplier<Biome>> stream) {
		this((List<Biome>)stream.map(Supplier::get).collect(ImmutableList.toImmutableList()));
	}

	protected BiomeSource(List<Biome> list) {
		this.possibleBiomes = list;
		Builder<StructureFeature<?>> builder = ImmutableSet.builder();

		for (StructureFeature<?> structureFeature : Registry.STRUCTURE_FEATURE) {
			if (list.stream().anyMatch(biome -> biome.getGenerationSettings().isValidStart(structureFeature))) {
				builder.add(structureFeature);
			}
		}

		this.supportedStructures = builder.build();
		Builder<BlockState> builder2 = ImmutableSet.builder();

		for (Biome biome : list) {
			builder2.add(biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial());
		}

		this.surfaceBlocks = builder2.build();
		Map<Pair<Integer, ConfiguredFeature<?, ?>>, Set<Pair<Integer, ConfiguredFeature<?, ?>>>> map = Maps.<Pair<Integer, ConfiguredFeature<?, ?>>, Set<Pair<Integer, ConfiguredFeature<?, ?>>>>newHashMap();
		int i = 0;

		for (Biome biome2 : list) {
			List<Pair<Integer, ConfiguredFeature<?, ?>>> list2 = Lists.<Pair<Integer, ConfiguredFeature<?, ?>>>newArrayList();
			List<List<Supplier<ConfiguredFeature<?, ?>>>> list3 = biome2.getGenerationSettings().features();
			i = Math.max(i, list3.size());

			for (int j = 0; j < list3.size(); j++) {
				for (Supplier<ConfiguredFeature<?, ?>> supplier : (List)list3.get(j)) {
					ConfiguredFeature<?, ?> configuredFeature = (ConfiguredFeature<?, ?>)supplier.get();
					list2.add(Pair.of(j, configuredFeature));
				}
			}

			for (int j = 0; j < list2.size() - 1; j++) {
				((Set)map.computeIfAbsent((Pair)list2.get(j), pair -> Sets.newHashSet())).add((Pair)list2.get(j + 1));
			}
		}

		Set<Pair<Integer, ConfiguredFeature<?, ?>>> set = Sets.<Pair<Integer, ConfiguredFeature<?, ?>>>newHashSet();
		Set<Pair<Integer, ConfiguredFeature<?, ?>>> set2 = Sets.<Pair<Integer, ConfiguredFeature<?, ?>>>newHashSet();
		List<Pair<Integer, ConfiguredFeature<?, ?>>> list2 = Lists.<Pair<Integer, ConfiguredFeature<?, ?>>>newArrayList();

		for (Pair<Integer, ConfiguredFeature<?, ?>> pair : map.keySet()) {
			if (!set2.isEmpty()) {
				throw new IllegalStateException("DFS bork");
			}

			if (!set.contains(pair) && dfs(map, set, set2, list2, pair)) {
				throw new IllegalStateException("Feature order cycle found: " + (String)set2.stream().map(Object::toString).collect(Collectors.joining(", ")));
			}
		}

		Collections.reverse(list2);
		com.google.common.collect.ImmutableList.Builder<List<ConfiguredFeature<?, ?>>> builder3 = ImmutableList.builder();

		for (int j = 0; j < i; j++) {
			int k = j;
			builder3.add((List<ConfiguredFeature<?, ?>>)list2.stream().filter(pair -> (Integer)pair.getFirst() == k).map(Pair::getSecond).collect(Collectors.toList()));
		}

		this.features = builder3.build();
	}

	private static <T> boolean dfs(Map<T, Set<T>> map, Set<T> set, Set<T> set2, List<T> list, T object) {
		if (set.contains(object)) {
			return false;
		} else if (set2.contains(object)) {
			return true;
		} else {
			set2.add(object);

			for (T object2 : (Set)map.getOrDefault(object, ImmutableSet.of())) {
				if (dfs(map, set, set2, list, object2)) {
					return true;
				}
			}

			set2.remove(object);
			set.add(object);
			list.add(object);
			return false;
		}
	}

	protected abstract Codec<? extends BiomeSource> codec();

	public abstract BiomeSource withSeed(long l);

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
		Set<Biome> set = Sets.<Biome>newHashSet();

		for (int v = 0; v < u; v++) {
			for (int w = 0; w < s; w++) {
				for (int x = 0; x < t; x++) {
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
		int n = QuartPos.fromBlock(i);
		int o = QuartPos.fromBlock(k);
		int p = QuartPos.fromBlock(l);
		int q = QuartPos.fromBlock(j);
		BlockPos blockPos = null;
		int r = 0;
		int s = bl ? 0 : p;
		int t = s;

		while (t <= p) {
			for (int u = SharedConstants.DEBUG_GENERATE_SQUARE_TERRAIN_WITHOUT_NOISE ? 0 : -t; u <= t; u += m) {
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
					if (predicate.test(this.getNoiseBiome(w, q, x))) {
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

	public boolean canGenerateStructure(StructureFeature<?> structureFeature) {
		return this.supportedStructures.contains(structureFeature);
	}

	public boolean hasSurfaceBlock(BlockState blockState) {
		return this.surfaceBlocks.contains(blockState);
	}

	public double[] getOffsetAndFactor(int i, int j) {
		double d = 0.03;
		double e = 342.8571468713332;
		return new double[]{0.03, 342.8571468713332};
	}

	public List<List<ConfiguredFeature<?, ?>>> features() {
		return this.features;
	}

	public void addMultinoiseDebugInfo(List<String> list, BlockPos blockPos) {
	}

	static {
		Registry.register(Registry.BIOME_SOURCE, "fixed", FixedBiomeSource.CODEC);
		Registry.register(Registry.BIOME_SOURCE, "multi_noise", MultiNoiseBiomeSource.CODEC);
		Registry.register(Registry.BIOME_SOURCE, "checkerboard", CheckerboardColumnBiomeSource.CODEC);
		Registry.register(Registry.BIOME_SOURCE, "the_end", TheEndBiomeSource.CODEC);
	}
}
