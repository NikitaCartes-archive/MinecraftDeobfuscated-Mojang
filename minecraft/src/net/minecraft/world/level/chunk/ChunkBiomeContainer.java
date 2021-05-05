package net.minecraft.world.level.chunk;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.IdMap;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkBiomeContainer implements BiomeManager.NoiseBiomeSource {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int WIDTH_BITS = Mth.ceillog2(16) - 2;
	private static final int HORIZONTAL_MASK = (1 << WIDTH_BITS) - 1;
	public static final int MAX_SIZE = 1 << WIDTH_BITS + WIDTH_BITS + DimensionType.BITS_FOR_Y - 2;
	private final IdMap<Biome> biomeRegistry;
	private final Biome[] biomes;
	private final int quartMinY;
	private final int quartHeight;

	protected ChunkBiomeContainer(IdMap<Biome> idMap, LevelHeightAccessor levelHeightAccessor, Biome[] biomes) {
		this.biomeRegistry = idMap;
		this.biomes = biomes;
		this.quartMinY = QuartPos.fromBlock(levelHeightAccessor.getMinBuildHeight());
		this.quartHeight = QuartPos.fromBlock(levelHeightAccessor.getHeight()) - 1;
	}

	public ChunkBiomeContainer(IdMap<Biome> idMap, LevelHeightAccessor levelHeightAccessor, int[] is) {
		this(idMap, levelHeightAccessor, new Biome[is.length]);
		int i = -1;

		for (int j = 0; j < this.biomes.length; j++) {
			int k = is[j];
			Biome biome = idMap.byId(k);
			if (biome == null) {
				if (i == -1) {
					i = j;
				}

				this.biomes[j] = idMap.byId(0);
			} else {
				this.biomes[j] = biome;
			}
		}

		if (i != -1) {
			LOGGER.warn("Invalid biome data received, starting from {}: {}", i, Arrays.toString(is));
		}
	}

	public ChunkBiomeContainer(IdMap<Biome> idMap, LevelHeightAccessor levelHeightAccessor, ChunkPos chunkPos, BiomeSource biomeSource) {
		this(idMap, levelHeightAccessor, chunkPos, biomeSource, null);
	}

	public ChunkBiomeContainer(IdMap<Biome> idMap, LevelHeightAccessor levelHeightAccessor, ChunkPos chunkPos, BiomeSource biomeSource, @Nullable int[] is) {
		this(idMap, levelHeightAccessor, new Biome[(1 << WIDTH_BITS + WIDTH_BITS) * ceilDiv(levelHeightAccessor.getHeight(), 4)]);
		int i = QuartPos.fromBlock(chunkPos.getMinBlockX());
		int j = this.quartMinY;
		int k = QuartPos.fromBlock(chunkPos.getMinBlockZ());
		if (is != null) {
			for (int l = 0; l < is.length; l++) {
				this.biomes[l] = idMap.byId(is[l]);
				if (this.biomes[l] == null) {
					this.biomes[l] = biomeForIndex(biomeSource, i, j, k, l);
				}
			}
		} else {
			for (int lx = 0; lx < this.biomes.length; lx++) {
				this.biomes[lx] = biomeForIndex(biomeSource, i, j, k, lx);
			}
		}
	}

	private static int ceilDiv(int i, int j) {
		return (i + j - 1) / j;
	}

	private static Biome biomeForIndex(BiomeSource biomeSource, int i, int j, int k, int l) {
		int m = l & HORIZONTAL_MASK;
		int n = l >> WIDTH_BITS + WIDTH_BITS;
		int o = l >> WIDTH_BITS & HORIZONTAL_MASK;
		return biomeSource.getNoiseBiome(i + m, j + n, k + o);
	}

	public int[] writeBiomes() {
		int[] is = new int[this.biomes.length];

		for (int i = 0; i < this.biomes.length; i++) {
			is[i] = this.biomeRegistry.getId(this.biomes[i]);
		}

		return is;
	}

	@Override
	public Biome getNoiseBiome(int i, int j, int k) {
		int l = i & HORIZONTAL_MASK;
		int m = Mth.clamp(j - this.quartMinY, 0, this.quartHeight);
		int n = k & HORIZONTAL_MASK;
		return this.biomes[m << WIDTH_BITS + WIDTH_BITS | n << WIDTH_BITS | l];
	}
}
