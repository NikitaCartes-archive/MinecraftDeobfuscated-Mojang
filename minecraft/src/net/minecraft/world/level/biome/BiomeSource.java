package net.minecraft.world.level.biome;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;

public abstract class BiomeSource implements BiomeResolver {
	public static final Codec<BiomeSource> CODEC = Registry.BIOME_SOURCE.byNameCodec().dispatchStable(BiomeSource::codec, Function.identity());
	private final Set<Holder<Biome>> possibleBiomes;

	protected BiomeSource(Stream<Holder<Biome>> stream) {
		this(stream.distinct().toList());
	}

	protected BiomeSource(List<Holder<Biome>> list) {
		this.possibleBiomes = new ObjectLinkedOpenHashSet<>(list);
	}

	protected abstract Codec<? extends BiomeSource> codec();

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
		int i, int j, int k, int l, Predicate<Holder<Biome>> predicate, RandomSource randomSource, Climate.Sampler sampler
	) {
		return this.findBiomeHorizontal(i, j, k, l, 1, predicate, randomSource, false, sampler);
	}

	@Nullable
	public Pair<BlockPos, Holder<Biome>> findClosestBiome3d(
		BlockPos blockPos, int i, int j, int k, Predicate<Holder<Biome>> predicate, Climate.Sampler sampler, LevelReader levelReader
	) {
		Set<Holder<Biome>> set = (Set<Holder<Biome>>)this.possibleBiomes().stream().filter(predicate).collect(Collectors.toUnmodifiableSet());
		if (set.isEmpty()) {
			return null;
		} else {
			int l = Math.floorDiv(i, j);
			int[] is = Mth.outFromOrigin(blockPos.getY(), levelReader.getMinBuildHeight() + 1, levelReader.getMaxBuildHeight(), k).toArray();

			for (BlockPos.MutableBlockPos mutableBlockPos : BlockPos.spiralAround(BlockPos.ZERO, l, Direction.EAST, Direction.SOUTH)) {
				int m = blockPos.getX() + mutableBlockPos.getX() * j;
				int n = blockPos.getZ() + mutableBlockPos.getZ() * j;
				int o = QuartPos.fromBlock(m);
				int p = QuartPos.fromBlock(n);

				for (int q : is) {
					int r = QuartPos.fromBlock(q);
					Holder<Biome> holder = this.getNoiseBiome(o, r, p, sampler);
					if (set.contains(holder)) {
						return Pair.of(new BlockPos(m, q, n), holder);
					}
				}
			}

			return null;
		}
	}

	@Nullable
	public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(
		int i, int j, int k, int l, int m, Predicate<Holder<Biome>> predicate, RandomSource randomSource, boolean bl, Climate.Sampler sampler
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
						if (pair == null || randomSource.nextInt(r + 1) == 0) {
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
}
