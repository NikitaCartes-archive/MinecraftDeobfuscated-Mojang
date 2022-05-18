package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.levelgen.DensityFunction;

public class TheEndBiomeSource extends BiomeSource {
	public static final Codec<TheEndBiomeSource> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(theEndBiomeSource -> null))
				.apply(instance, instance.stable(TheEndBiomeSource::new))
	);
	private final Holder<Biome> end;
	private final Holder<Biome> highlands;
	private final Holder<Biome> midlands;
	private final Holder<Biome> islands;
	private final Holder<Biome> barrens;

	public TheEndBiomeSource(Registry<Biome> registry) {
		this(
			registry.getOrCreateHolderOrThrow(Biomes.THE_END),
			registry.getOrCreateHolderOrThrow(Biomes.END_HIGHLANDS),
			registry.getOrCreateHolderOrThrow(Biomes.END_MIDLANDS),
			registry.getOrCreateHolderOrThrow(Biomes.SMALL_END_ISLANDS),
			registry.getOrCreateHolderOrThrow(Biomes.END_BARRENS)
		);
	}

	private TheEndBiomeSource(Holder<Biome> holder, Holder<Biome> holder2, Holder<Biome> holder3, Holder<Biome> holder4, Holder<Biome> holder5) {
		super(ImmutableList.of(holder, holder2, holder3, holder4, holder5));
		this.end = holder;
		this.highlands = holder2;
		this.midlands = holder3;
		this.islands = holder4;
		this.barrens = holder5;
	}

	@Override
	protected Codec<? extends BiomeSource> codec() {
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
