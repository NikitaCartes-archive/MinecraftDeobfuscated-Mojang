package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.newbiome.layer.Layer;
import net.minecraft.world.level.newbiome.layer.Layers;

public class OverworldBiomeSource extends BiomeSource {
	private final Layer noiseBiomeLayer;
	private static final Set<Biome> POSSIBLE_BIOMES = ImmutableSet.of(
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
	);
	private final boolean legacyBiomeInitLayer;
	private final int biomeSize;

	public OverworldBiomeSource(long l, boolean bl, int i) {
		super(POSSIBLE_BIOMES);
		this.legacyBiomeInitLayer = bl;
		this.biomeSize = i;
		this.noiseBiomeLayer = Layers.getDefaultLayer(l, bl, i, 4);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public BiomeSource withSeed(long l) {
		return new OverworldBiomeSource(l, this.legacyBiomeInitLayer, this.biomeSize);
	}

	@Override
	public Biome getNoiseBiome(int i, int j, int k) {
		return this.noiseBiomeLayer.get(i, k);
	}
}
