package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.LevelReader;

public class FixedBiomeSource extends BiomeSource implements BiomeManager.NoiseBiomeSource {
	public static final Codec<FixedBiomeSource> CODEC = Biome.CODEC
		.fieldOf("biome")
		.<FixedBiomeSource>xmap(FixedBiomeSource::new, fixedBiomeSource -> fixedBiomeSource.biome)
		.stable()
		.codec();
	private final Holder<Biome> biome;

	public FixedBiomeSource(Holder<Biome> holder) {
		super(ImmutableList.of(holder));
		this.biome = holder;
	}

	@Override
	protected Codec<? extends BiomeSource> codec() {
		return CODEC;
	}

	@Override
	public Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler sampler) {
		return this.biome;
	}

	@Override
	public Holder<Biome> getNoiseBiome(int i, int j, int k) {
		return this.biome;
	}

	@Nullable
	@Override
	public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(
		int i, int j, int k, int l, int m, Predicate<Holder<Biome>> predicate, Random random, boolean bl, Climate.Sampler sampler
	) {
		if (predicate.test(this.biome)) {
			return bl
				? Pair.of(new BlockPos(i, j, k), this.biome)
				: Pair.of(new BlockPos(i - l + random.nextInt(l * 2 + 1), j, k - l + random.nextInt(l * 2 + 1)), this.biome);
		} else {
			return null;
		}
	}

	@Nullable
	@Override
	public Pair<BlockPos, Holder<Biome>> findClosestBiome3d(
		BlockPos blockPos, int i, int j, int k, Predicate<Holder<Biome>> predicate, Climate.Sampler sampler, LevelReader levelReader
	) {
		return predicate.test(this.biome) ? Pair.of(blockPos, this.biome) : null;
	}

	@Override
	public Set<Holder<Biome>> getBiomesWithin(int i, int j, int k, int l, Climate.Sampler sampler) {
		return Sets.<Holder<Biome>>newHashSet(Set.of(this.biome));
	}
}
