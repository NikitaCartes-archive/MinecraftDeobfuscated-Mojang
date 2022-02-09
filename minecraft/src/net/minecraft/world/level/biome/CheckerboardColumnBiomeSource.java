package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;

public class CheckerboardColumnBiomeSource extends BiomeSource {
	public static final Codec<CheckerboardColumnBiomeSource> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Biome.LIST_CODEC.fieldOf("biomes").forGetter(checkerboardColumnBiomeSource -> checkerboardColumnBiomeSource.allowedBiomes),
					Codec.intRange(0, 62).fieldOf("scale").orElse(2).forGetter(checkerboardColumnBiomeSource -> checkerboardColumnBiomeSource.size)
				)
				.apply(instance, CheckerboardColumnBiomeSource::new)
	);
	private final HolderSet<Biome> allowedBiomes;
	private final int bitShift;
	private final int size;

	public CheckerboardColumnBiomeSource(HolderSet<Biome> holderSet, int i) {
		super(holderSet.stream());
		this.allowedBiomes = holderSet;
		this.bitShift = i + 2;
		this.size = i;
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
	public Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler sampler) {
		return this.allowedBiomes.get(Math.floorMod((i >> this.bitShift) + (k >> this.bitShift), this.allowedBiomes.size()));
	}
}
