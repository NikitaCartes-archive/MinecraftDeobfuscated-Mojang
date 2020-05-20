package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;

public class FixedBiomeSource extends BiomeSource {
	public static final Codec<FixedBiomeSource> CODEC = Registry.BIOME
		.fieldOf("biome")
		.<FixedBiomeSource>xmap(FixedBiomeSource::new, fixedBiomeSource -> fixedBiomeSource.biome)
		.stable()
		.codec();
	private final Biome biome;

	public FixedBiomeSource(Biome biome) {
		super(ImmutableList.of(biome));
		this.biome = biome;
	}

	@Override
	protected Codec<? extends BiomeSource> codec() {
		return CODEC;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public BiomeSource withSeed(long l) {
		return this;
	}

	@Override
	public Biome getNoiseBiome(int i, int j, int k) {
		return this.biome;
	}

	@Nullable
	@Override
	public BlockPos findBiomeHorizontal(int i, int j, int k, int l, int m, List<Biome> list, Random random, boolean bl) {
		if (list.contains(this.biome)) {
			return bl ? new BlockPos(i, j, k) : new BlockPos(i - l + random.nextInt(l * 2 + 1), j, k - l + random.nextInt(l * 2 + 1));
		} else {
			return null;
		}
	}

	@Override
	public Set<Biome> getBiomesWithin(int i, int j, int k, int l) {
		return Sets.<Biome>newHashSet(this.biome);
	}
}
