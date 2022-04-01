package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class TheEndBiomeSource extends BiomeSource {
	public static final Codec<TheEndBiomeSource> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(theEndBiomeSource -> null),
					Codec.LONG.fieldOf("seed").stable().forGetter(theEndBiomeSource -> theEndBiomeSource.seed)
				)
				.apply(instance, instance.stable(TheEndBiomeSource::new))
	);
	private static final float ISLAND_THRESHOLD = -0.9F;
	public static final int ISLAND_CHUNK_DISTANCE = 64;
	private static final long ISLAND_CHUNK_DISTANCE_SQR = 4096L;
	private final SimplexNoise islandNoise;
	private final long seed;
	private final Holder<Biome> end;
	private final Holder<Biome> highlands;
	private final Holder<Biome> midlands;
	private final Holder<Biome> islands;
	private final Holder<Biome> barrens;

	public TheEndBiomeSource(Registry<Biome> registry, long l) {
		this(
			l,
			registry.getOrCreateHolder(Biomes.THE_END),
			registry.getOrCreateHolder(Biomes.END_HIGHLANDS),
			registry.getOrCreateHolder(Biomes.END_MIDLANDS),
			registry.getOrCreateHolder(Biomes.SMALL_END_ISLANDS),
			registry.getOrCreateHolder(Biomes.END_BARRENS)
		);
	}

	private TheEndBiomeSource(long l, Holder<Biome> holder, Holder<Biome> holder2, Holder<Biome> holder3, Holder<Biome> holder4, Holder<Biome> holder5) {
		super(ImmutableList.of(holder, holder2, holder3, holder4, holder5));
		this.seed = l;
		this.end = holder;
		this.highlands = holder2;
		this.midlands = holder3;
		this.islands = holder4;
		this.barrens = holder5;
		WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(l));
		worldgenRandom.consumeCount(17292);
		this.islandNoise = new SimplexNoise(worldgenRandom);
	}

	@Override
	protected Codec<? extends BiomeSource> codec() {
		return CODEC;
	}

	@Override
	public BiomeSource withSeed(long l) {
		return new TheEndBiomeSource(l, this.end, this.highlands, this.midlands, this.islands, this.barrens);
	}

	@Override
	public Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler sampler) {
		int l = i >> 2;
		int m = k >> 2;
		if ((long)l * (long)l + (long)m * (long)m <= 4096L) {
			return this.end;
		} else {
			float f = getHeightValue(this.islandNoise, l * 2 + 1, m * 2 + 1);
			if (f > 40.0F) {
				return this.highlands;
			} else if (f >= 0.0F) {
				return this.midlands;
			} else {
				return f < -20.0F ? this.islands : this.barrens;
			}
		}
	}

	public boolean stable(long l) {
		return this.seed == l;
	}

	public static float getHeightValue(SimplexNoise simplexNoise, int i, int j) {
		int k = i / 2;
		int l = j / 2;
		int m = i % 2;
		int n = j % 2;
		float f = 100.0F - Mth.sqrt((float)(i * i + j * j)) * 8.0F;
		f = Mth.clamp(f, -100.0F, 80.0F);

		for (int o = -12; o <= 12; o++) {
			for (int p = -12; p <= 12; p++) {
				long q = (long)(k + o);
				long r = (long)(l + p);
				if (q * q + r * r > 4096L && simplexNoise.getValue((double)q, (double)r) < -0.9F) {
					float g = (Mth.abs((float)q) * 3439.0F + Mth.abs((float)r) * 147.0F) % 13.0F + 9.0F;
					float h = (float)(m - o * 2);
					float s = (float)(n - p * 2);
					float t = 100.0F - Mth.sqrt(h * h + s * s) * g;
					t = Mth.clamp(t, -100.0F, 80.0F);
					f = Math.max(f, t);
				}
			}
		}

		return f;
	}
}
