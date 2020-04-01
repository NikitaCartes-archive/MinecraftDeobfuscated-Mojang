package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Objects;
import net.minecraft.core.Registry;

public class CheckerboardColumnBiomeSource extends BiomeSource {
	private final Biome[] allowedBiomes;
	private final int bitShift;

	public CheckerboardColumnBiomeSource(CheckerboardBiomeSourceSettings checkerboardBiomeSourceSettings) {
		super(ImmutableSet.copyOf(checkerboardBiomeSourceSettings.getAllowedBiomes()));
		this.allowedBiomes = checkerboardBiomeSourceSettings.getAllowedBiomes();
		this.bitShift = checkerboardBiomeSourceSettings.getSize() + 2;
	}

	@Override
	public Biome getNoiseBiome(int i, int j, int k) {
		return this.allowedBiomes[Math.abs(((i >> this.bitShift) + (k >> this.bitShift)) % this.allowedBiomes.length)];
	}

	@Override
	public BiomeSourceType<?, ?> getType() {
		return BiomeSourceType.CHECKERBOARD;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		T object = dynamicOps.createList(this.possibleBiomes.stream().map(Registry.BIOME::getKey).map(Objects::toString).map(dynamicOps::createString));
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("biomes"), object, dynamicOps.createString("bitShift"), dynamicOps.createInt(this.bitShift)))
		);
	}
}
