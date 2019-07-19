package net.minecraft.world.level.biome;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class TheEndBiomeSource extends BiomeSource {
	private final SimplexNoise islandNoise;
	private final WorldgenRandom random;
	private final Biome[] possibleBiomes = new Biome[]{Biomes.THE_END, Biomes.END_HIGHLANDS, Biomes.END_MIDLANDS, Biomes.SMALL_END_ISLANDS, Biomes.END_BARRENS};

	public TheEndBiomeSource(TheEndBiomeSourceSettings theEndBiomeSourceSettings) {
		this.random = new WorldgenRandom(theEndBiomeSourceSettings.getSeed());
		this.random.consumeCount(17292);
		this.islandNoise = new SimplexNoise(this.random);
	}

	@Override
	public Biome getBiome(int i, int j) {
		int k = i >> 4;
		int l = j >> 4;
		if ((long)k * (long)k + (long)l * (long)l <= 4096L) {
			return Biomes.THE_END;
		} else {
			float f = this.getHeightValue(k * 2 + 1, l * 2 + 1);
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
	public Biome[] getBiomeBlock(int i, int j, int k, int l, boolean bl) {
		Biome[] biomes = new Biome[k * l];
		Long2ObjectMap<Biome> long2ObjectMap = new Long2ObjectOpenHashMap<>();

		for (int m = 0; m < k; m++) {
			for (int n = 0; n < l; n++) {
				int o = m + i;
				int p = n + j;
				long q = ChunkPos.asLong(o, p);
				Biome biome = long2ObjectMap.get(q);
				if (biome == null) {
					biome = this.getBiome(o, p);
					long2ObjectMap.put(q, biome);
				}

				biomes[m + n * k] = biome;
			}
		}

		return biomes;
	}

	@Override
	public Set<Biome> getBiomesWithin(int i, int j, int k) {
		int l = i - k >> 2;
		int m = j - k >> 2;
		int n = i + k >> 2;
		int o = j + k >> 2;
		int p = n - l + 1;
		int q = o - m + 1;
		return Sets.<Biome>newHashSet(this.getBiomeBlock(l, m, p, q));
	}

	@Nullable
	@Override
	public BlockPos findBiome(int i, int j, int k, List<Biome> list, Random random) {
		int l = i - k >> 2;
		int m = j - k >> 2;
		int n = i + k >> 2;
		int o = j + k >> 2;
		int p = n - l + 1;
		int q = o - m + 1;
		Biome[] biomes = this.getBiomeBlock(l, m, p, q);
		BlockPos blockPos = null;
		int r = 0;

		for (int s = 0; s < p * q; s++) {
			int t = l + s % p << 2;
			int u = m + s / p << 2;
			if (list.contains(biomes[s])) {
				if (blockPos == null || random.nextInt(r + 1) == 0) {
					blockPos = new BlockPos(t, 0, u);
				}

				r++;
			}
		}

		return blockPos;
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

	@Override
	public boolean canGenerateStructure(StructureFeature<?> structureFeature) {
		return (Boolean)this.supportedStructures.computeIfAbsent(structureFeature, structureFeaturex -> {
			for (Biome biome : this.possibleBiomes) {
				if (biome.isValidStart(structureFeaturex)) {
					return true;
				}
			}

			return false;
		});
	}

	@Override
	public Set<BlockState> getSurfaceBlocks() {
		if (this.surfaceBlocks.isEmpty()) {
			for (Biome biome : this.possibleBiomes) {
				this.surfaceBlocks.add(biome.getSurfaceBuilderConfig().getTopMaterial());
			}
		}

		return this.surfaceBlocks;
	}
}
