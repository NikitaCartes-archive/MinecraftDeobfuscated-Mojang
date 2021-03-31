package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Supplier;

public class CheckerboardColumnBiomeSource extends BiomeSource {
	public static final Codec<CheckerboardColumnBiomeSource> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Biome.LIST_CODEC.fieldOf("biomes").forGetter(checkerboardColumnBiomeSource -> checkerboardColumnBiomeSource.allowedBiomes),
					Codec.intRange(0, 62).fieldOf("scale").orElse(2).forGetter(checkerboardColumnBiomeSource -> checkerboardColumnBiomeSource.size)
				)
				.apply(instance, CheckerboardColumnBiomeSource::new)
	);
	private final List<Supplier<Biome>> allowedBiomes;
	private final int bitShift;
	private final int size;

	public CheckerboardColumnBiomeSource(List<Supplier<Biome>> list, int i) {
		super(list.stream());
		this.allowedBiomes = list;
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
	public Biome getNoiseBiome(int i, int j, int k) {
		return (Biome)((Supplier)this.allowedBiomes.get(Math.floorMod((i >> this.bitShift) + (k >> this.bitShift), this.allowedBiomes.size()))).get();
	}
}
