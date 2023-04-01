package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.RegistryOps;

public class TheMoonBiomeSource extends BiomeSource {
	public static final Codec<TheMoonBiomeSource> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(RegistryOps.retrieveElement(Biomes.THE_MOON)).apply(instance, instance.stable(TheMoonBiomeSource::new))
	);
	private final Holder<Biome> moon;

	public static TheMoonBiomeSource create(HolderGetter<Biome> holderGetter) {
		return new TheMoonBiomeSource(holderGetter.getOrThrow(Biomes.THE_MOON));
	}

	private TheMoonBiomeSource(Holder<Biome> holder) {
		this.moon = holder;
	}

	@Override
	protected Stream<Holder<Biome>> collectPossibleBiomes() {
		return Stream.of(this.moon);
	}

	@Override
	protected Codec<? extends BiomeSource> codec() {
		return CODEC;
	}

	@Override
	public Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler sampler) {
		return this.moon;
	}
}
