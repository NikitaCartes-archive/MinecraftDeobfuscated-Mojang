package net.minecraft.world.level.biome;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.newbiome.layer.Layer;
import net.minecraft.world.level.newbiome.layer.Layers;
import net.minecraft.world.level.storage.LevelData;

public class OverworldBiomeSource extends BiomeSource {
	private final Layer noiseBiomeLayer;
	private final Layer blockBiomeLayer;
	private final Biome[] possibleBiomes = new Biome[]{
		Biomes.OCEAN,
		Biomes.PLAINS,
		Biomes.DESERT,
		Biomes.MOUNTAINS,
		Biomes.FOREST,
		Biomes.TAIGA,
		Biomes.SWAMP,
		Biomes.RIVER,
		Biomes.FROZEN_OCEAN,
		Biomes.FROZEN_RIVER,
		Biomes.SNOWY_TUNDRA,
		Biomes.SNOWY_MOUNTAINS,
		Biomes.MUSHROOM_FIELDS,
		Biomes.MUSHROOM_FIELD_SHORE,
		Biomes.BEACH,
		Biomes.DESERT_HILLS,
		Biomes.WOODED_HILLS,
		Biomes.TAIGA_HILLS,
		Biomes.MOUNTAIN_EDGE,
		Biomes.JUNGLE,
		Biomes.JUNGLE_HILLS,
		Biomes.JUNGLE_EDGE,
		Biomes.DEEP_OCEAN,
		Biomes.STONE_SHORE,
		Biomes.SNOWY_BEACH,
		Biomes.BIRCH_FOREST,
		Biomes.BIRCH_FOREST_HILLS,
		Biomes.DARK_FOREST,
		Biomes.SNOWY_TAIGA,
		Biomes.SNOWY_TAIGA_HILLS,
		Biomes.GIANT_TREE_TAIGA,
		Biomes.GIANT_TREE_TAIGA_HILLS,
		Biomes.WOODED_MOUNTAINS,
		Biomes.SAVANNA,
		Biomes.SAVANNA_PLATEAU,
		Biomes.BADLANDS,
		Biomes.WOODED_BADLANDS_PLATEAU,
		Biomes.BADLANDS_PLATEAU,
		Biomes.WARM_OCEAN,
		Biomes.LUKEWARM_OCEAN,
		Biomes.COLD_OCEAN,
		Biomes.DEEP_WARM_OCEAN,
		Biomes.DEEP_LUKEWARM_OCEAN,
		Biomes.DEEP_COLD_OCEAN,
		Biomes.DEEP_FROZEN_OCEAN,
		Biomes.SUNFLOWER_PLAINS,
		Biomes.DESERT_LAKES,
		Biomes.GRAVELLY_MOUNTAINS,
		Biomes.FLOWER_FOREST,
		Biomes.TAIGA_MOUNTAINS,
		Biomes.SWAMP_HILLS,
		Biomes.ICE_SPIKES,
		Biomes.MODIFIED_JUNGLE,
		Biomes.MODIFIED_JUNGLE_EDGE,
		Biomes.TALL_BIRCH_FOREST,
		Biomes.TALL_BIRCH_HILLS,
		Biomes.DARK_FOREST_HILLS,
		Biomes.SNOWY_TAIGA_MOUNTAINS,
		Biomes.GIANT_SPRUCE_TAIGA,
		Biomes.GIANT_SPRUCE_TAIGA_HILLS,
		Biomes.MODIFIED_GRAVELLY_MOUNTAINS,
		Biomes.SHATTERED_SAVANNA,
		Biomes.SHATTERED_SAVANNA_PLATEAU,
		Biomes.ERODED_BADLANDS,
		Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU,
		Biomes.MODIFIED_BADLANDS_PLATEAU
	};

	public OverworldBiomeSource(OverworldBiomeSourceSettings overworldBiomeSourceSettings) {
		LevelData levelData = overworldBiomeSourceSettings.getLevelData();
		OverworldGeneratorSettings overworldGeneratorSettings = overworldBiomeSourceSettings.getGeneratorSettings();
		Layer[] layers = Layers.getDefaultLayers(levelData.getSeed(), levelData.getGeneratorType(), overworldGeneratorSettings);
		this.noiseBiomeLayer = layers[0];
		this.blockBiomeLayer = layers[1];
	}

	@Override
	public Biome getBiome(int i, int j) {
		return this.blockBiomeLayer.get(i, j);
	}

	@Override
	public Biome getNoiseBiome(int i, int j) {
		return this.noiseBiomeLayer.get(i, j);
	}

	@Override
	public Biome[] getBiomeBlock(int i, int j, int k, int l, boolean bl) {
		return this.blockBiomeLayer.getArea(i, j, k, l);
	}

	@Override
	public Set<Biome> getBiomesWithin(int i, int j, int k) {
		int l = i - k >> 2;
		int m = j - k >> 2;
		int n = i + k >> 2;
		int o = j + k >> 2;
		int p = n - l + 1;
		int q = o - m + 1;
		Set<Biome> set = Sets.<Biome>newHashSet();
		Collections.addAll(set, this.noiseBiomeLayer.getArea(l, m, p, q));
		return set;
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
		Biome[] biomes = this.noiseBiomeLayer.getArea(l, m, p, q);
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
