package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

public class FixedBiomeSource extends BiomeSource {
	public static final Codec<FixedBiomeSource> CODEC = Biome.CODEC
		.fieldOf("biome")
		.<FixedBiomeSource>xmap(FixedBiomeSource::new, fixedBiomeSource -> fixedBiomeSource.biome)
		.stable()
		.codec();
	private final Supplier<Biome> biome;

	public FixedBiomeSource(Biome biome) {
		this(() -> biome);
	}

	public FixedBiomeSource(Supplier<Biome> supplier) {
		super(ImmutableList.of((Biome)supplier.get()));
		this.biome = supplier;
	}

	@Override
	protected Codec<? extends BiomeSource> codec() {
		return CODEC;
	}

	@Override
	public BiomeSource withSeed(long l) {
		return this;
	}

	@Override
	public Biome getNoiseBiome(int i, int j, int k) {
		return (Biome)this.biome.get();
	}

	@Nullable
	@Override
	public BlockPos findBiomeHorizontal(int i, int j, int k, int l, int m, Predicate<Biome> predicate, Random random, boolean bl) {
		if (predicate.test((Biome)this.biome.get())) {
			return bl ? new BlockPos(i, j, k) : new BlockPos(i - l + random.nextInt(l * 2 + 1), j, k - l + random.nextInt(l * 2 + 1));
		} else {
			return null;
		}
	}

	@Override
	public Set<Biome> getBiomesWithin(int i, int j, int k, int l) {
		return Sets.<Biome>newHashSet((Biome)this.biome.get());
	}
}
