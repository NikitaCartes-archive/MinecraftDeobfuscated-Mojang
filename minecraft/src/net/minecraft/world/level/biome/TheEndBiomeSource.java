package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class TheEndBiomeSource extends BiomeSource {
	public static final Codec<TheEndBiomeSource> CODEC = Codec.LONG
		.fieldOf("seed")
		.<TheEndBiomeSource>xmap(TheEndBiomeSource::new, theEndBiomeSource -> theEndBiomeSource.seed)
		.stable()
		.codec();
	private final SimplexNoise islandNoise;
	private static final List<Biome> POSSIBLE_BIOMES = ImmutableList.of(
		Biomes.THE_END, Biomes.END_HIGHLANDS, Biomes.END_MIDLANDS, Biomes.SMALL_END_ISLANDS, Biomes.END_BARRENS
	);
	private final long seed;

	public TheEndBiomeSource(long l) {
		super(POSSIBLE_BIOMES);
		this.seed = l;
		WorldgenRandom worldgenRandom = new WorldgenRandom(l);
		worldgenRandom.consumeCount(17292);
		this.islandNoise = new SimplexNoise(worldgenRandom);
	}

	@Override
	protected Codec<? extends BiomeSource> codec() {
		return CODEC;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public BiomeSource withSeed(long l) {
		return new TheEndBiomeSource(l);
	}

	@Override
	public Biome getNoiseBiome(int i, int j, int k) {
		int l = i >> 2;
		int m = k >> 2;
		if ((long)l * (long)l + (long)m * (long)m <= 4096L) {
			return Biomes.THE_END;
		} else {
			float f = getHeightValue(this.islandNoise, l * 2 + 1, m * 2 + 1);
			if (f > 40.0F) {
				return Biomes.END_HIGHLANDS;
			} else if (f >= 0.0F) {
				return Biomes.END_MIDLANDS;
			} else {
				return f < -20.0F ? Biomes.SMALL_END_ISLANDS : Biomes.END_BARRENS;
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
