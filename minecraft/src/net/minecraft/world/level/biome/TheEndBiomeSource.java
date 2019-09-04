package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class TheEndBiomeSource extends BiomeSource {
	private final SimplexNoise islandNoise;
	private final WorldgenRandom random;
	private static final Set<Biome> POSSIBLE_BIOMES = ImmutableSet.of(
		Biomes.THE_END, Biomes.END_HIGHLANDS, Biomes.END_MIDLANDS, Biomes.SMALL_END_ISLANDS, Biomes.END_BARRENS
	);

	public TheEndBiomeSource(TheEndBiomeSourceSettings theEndBiomeSourceSettings) {
		super(POSSIBLE_BIOMES);
		this.random = new WorldgenRandom(theEndBiomeSourceSettings.getSeed());
		this.random.consumeCount(17292);
		this.islandNoise = new SimplexNoise(this.random);
	}

	@Override
	public Biome getNoiseBiome(int i, int j, int k) {
		int l = i >> 2;
		int m = k >> 2;
		if ((long)l * (long)l + (long)m * (long)m <= 4096L) {
			return Biomes.THE_END;
		} else {
			float f = this.getHeightValue(l * 2 + 1, m * 2 + 1);
			if (f > 40.0F) {
				return Biomes.END_HIGHLANDS;
			} else if (f >= 0.0F) {
				return Biomes.END_MIDLANDS;
			} else {
				return f < -20.0F ? Biomes.SMALL_END_ISLANDS : Biomes.END_BARRENS;
			}
		}
	}

	@Override
	public float getHeightValue(int i, int j) {
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
				if (q * q + r * r > 4096L && this.islandNoise.getValue((double)q, (double)r) < -0.9F) {
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
