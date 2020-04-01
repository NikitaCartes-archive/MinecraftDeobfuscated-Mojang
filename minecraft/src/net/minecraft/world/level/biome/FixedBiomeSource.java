package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;

public class FixedBiomeSource extends BiomeSource {
	private final Biome biome;

	public FixedBiomeSource(FixedBiomeSourceSettings fixedBiomeSourceSettings) {
		super(ImmutableSet.of(fixedBiomeSourceSettings.getBiome()));
		this.biome = fixedBiomeSourceSettings.getBiome();
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

	@Override
	public BiomeSourceType<?, ?> getType() {
		return BiomeSourceType.FIXED;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("biome"), dynamicOps.createString(Registry.BIOME.getKey(this.biome).toString())))
		);
	}
}
