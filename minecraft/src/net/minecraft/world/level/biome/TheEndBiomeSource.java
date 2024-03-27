package net.minecraft.world.level.biome;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.levelgen.DensityFunction;

public class TheEndBiomeSource extends BiomeSource {
	public static final MapCodec<TheEndBiomeSource> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					RegistryOps.retrieveElement(Biomes.THE_END),
					RegistryOps.retrieveElement(Biomes.END_HIGHLANDS),
					RegistryOps.retrieveElement(Biomes.END_MIDLANDS),
					RegistryOps.retrieveElement(Biomes.SMALL_END_ISLANDS),
					RegistryOps.retrieveElement(Biomes.END_BARRENS)
				)
				.apply(instance, instance.stable(TheEndBiomeSource::new))
	);
	private final Holder<Biome> end;
	private final Holder<Biome> highlands;
	private final Holder<Biome> midlands;
	private final Holder<Biome> islands;
	private final Holder<Biome> barrens;

	public static TheEndBiomeSource create(HolderGetter<Biome> holderGetter) {
		return new TheEndBiomeSource(
			holderGetter.getOrThrow(Biomes.THE_END),
			holderGetter.getOrThrow(Biomes.END_HIGHLANDS),
			holderGetter.getOrThrow(Biomes.END_MIDLANDS),
			holderGetter.getOrThrow(Biomes.SMALL_END_ISLANDS),
			holderGetter.getOrThrow(Biomes.END_BARRENS)
		);
	}

	private TheEndBiomeSource(Holder<Biome> holder, Holder<Biome> holder2, Holder<Biome> holder3, Holder<Biome> holder4, Holder<Biome> holder5) {
		this.end = holder;
		this.highlands = holder2;
		this.midlands = holder3;
		this.islands = holder4;
		this.barrens = holder5;
	}

	@Override
	protected Stream<Holder<Biome>> collectPossibleBiomes() {
		return Stream.of(this.end, this.highlands, this.midlands, this.islands, this.barrens);
	}

	@Override
	protected MapCodec<? extends BiomeSource> codec() {
		return CODEC;
	}

	@Override
	public Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler sampler) {
		int l = QuartPos.toBlock(i);
		int m = QuartPos.toBlock(j);
		int n = QuartPos.toBlock(k);
		int o = SectionPos.blockToSectionCoord(l);
		int p = SectionPos.blockToSectionCoord(n);
		if ((long)o * (long)o + (long)p * (long)p <= 4096L) {
			return this.end;
		} else {
			int q = (SectionPos.blockToSectionCoord(l) * 2 + 1) * 8;
			int r = (SectionPos.blockToSectionCoord(n) * 2 + 1) * 8;
			double d = sampler.erosion().compute(new DensityFunction.SinglePointContext(q, m, r));
			if (d > 0.25) {
				return this.highlands;
			} else if (d >= -0.0625) {
				return this.midlands;
			} else {
				return d < -0.21875 ? this.islands : this.barrens;
			}
		}
	}
}
