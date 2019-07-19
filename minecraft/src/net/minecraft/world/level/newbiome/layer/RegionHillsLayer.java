package net.minecraft.world.level.newbiome.layer;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2;
import net.minecraft.world.level.newbiome.layer.traits.DimensionOffset1Transformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum RegionHillsLayer implements AreaTransformer2, DimensionOffset1Transformer {
	INSTANCE;

	private static final Logger LOGGER = LogManager.getLogger();
	private static final int BIRCH_FOREST = Registry.BIOME.getId(Biomes.BIRCH_FOREST);
	private static final int BIRCH_FOREST_HILLS = Registry.BIOME.getId(Biomes.BIRCH_FOREST_HILLS);
	private static final int DESERT = Registry.BIOME.getId(Biomes.DESERT);
	private static final int DESERT_HILLS = Registry.BIOME.getId(Biomes.DESERT_HILLS);
	private static final int MOUNTAINS = Registry.BIOME.getId(Biomes.MOUNTAINS);
	private static final int WOODED_MOUNTAINS = Registry.BIOME.getId(Biomes.WOODED_MOUNTAINS);
	private static final int FOREST = Registry.BIOME.getId(Biomes.FOREST);
	private static final int WOODED_HILLS = Registry.BIOME.getId(Biomes.WOODED_HILLS);
	private static final int SNOWY_TUNDRA = Registry.BIOME.getId(Biomes.SNOWY_TUNDRA);
	private static final int SNOWY_MOUNTAIN = Registry.BIOME.getId(Biomes.SNOWY_MOUNTAINS);
	private static final int JUNGLE = Registry.BIOME.getId(Biomes.JUNGLE);
	private static final int JUNGLE_HILLS = Registry.BIOME.getId(Biomes.JUNGLE_HILLS);
	private static final int BAMBOO_JUNGLE = Registry.BIOME.getId(Biomes.BAMBOO_JUNGLE);
	private static final int BAMBOO_JUNGLE_HILLS = Registry.BIOME.getId(Biomes.BAMBOO_JUNGLE_HILLS);
	private static final int BADLANDS = Registry.BIOME.getId(Biomes.BADLANDS);
	private static final int WOODED_BADLANDS_PLATEAU = Registry.BIOME.getId(Biomes.WOODED_BADLANDS_PLATEAU);
	private static final int PLAINS = Registry.BIOME.getId(Biomes.PLAINS);
	private static final int GIANT_TREE_TAIGA = Registry.BIOME.getId(Biomes.GIANT_TREE_TAIGA);
	private static final int GIANT_TREE_TAIGA_HILLS = Registry.BIOME.getId(Biomes.GIANT_TREE_TAIGA_HILLS);
	private static final int DARK_FOREST = Registry.BIOME.getId(Biomes.DARK_FOREST);
	private static final int SAVANNA = Registry.BIOME.getId(Biomes.SAVANNA);
	private static final int SAVANNA_PLATEAU = Registry.BIOME.getId(Biomes.SAVANNA_PLATEAU);
	private static final int TAIGA = Registry.BIOME.getId(Biomes.TAIGA);
	private static final int SNOWY_TAIGA = Registry.BIOME.getId(Biomes.SNOWY_TAIGA);
	private static final int SNOWY_TAIGA_HILLS = Registry.BIOME.getId(Biomes.SNOWY_TAIGA_HILLS);
	private static final int TAIGA_HILLS = Registry.BIOME.getId(Biomes.TAIGA_HILLS);

	@Override
	public int applyPixel(Context context, Area area, Area area2, int i, int j) {
		int k = area.get(this.getParentX(i + 1), this.getParentY(j + 1));
		int l = area2.get(this.getParentX(i + 1), this.getParentY(j + 1));
		if (k > 255) {
			LOGGER.debug("old! {}", k);
		}

		int m = (l - 2) % 29;
		if (!Layers.isShallowOcean(k) && l >= 2 && m == 1) {
			Biome biome = Registry.BIOME.byId(k);
			if (biome == null || !biome.isMutated()) {
				Biome biome2 = Biome.getMutatedVariant(biome);
				return biome2 == null ? k : Registry.BIOME.getId(biome2);
			}
		}

		if (context.nextRandom(3) == 0 || m == 0) {
			int n = k;
			if (k == DESERT) {
				n = DESERT_HILLS;
			} else if (k == FOREST) {
				n = WOODED_HILLS;
			} else if (k == BIRCH_FOREST) {
				n = BIRCH_FOREST_HILLS;
			} else if (k == DARK_FOREST) {
				n = PLAINS;
			} else if (k == TAIGA) {
				n = TAIGA_HILLS;
			} else if (k == GIANT_TREE_TAIGA) {
				n = GIANT_TREE_TAIGA_HILLS;
			} else if (k == SNOWY_TAIGA) {
				n = SNOWY_TAIGA_HILLS;
			} else if (k == PLAINS) {
				n = context.nextRandom(3) == 0 ? WOODED_HILLS : FOREST;
			} else if (k == SNOWY_TUNDRA) {
				n = SNOWY_MOUNTAIN;
			} else if (k == JUNGLE) {
				n = JUNGLE_HILLS;
			} else if (k == BAMBOO_JUNGLE) {
				n = BAMBOO_JUNGLE_HILLS;
			} else if (k == Layers.OCEAN) {
				n = Layers.DEEP_OCEAN;
			} else if (k == Layers.LUKEWARM_OCEAN) {
				n = Layers.DEEP_LUKEWARM_OCEAN;
			} else if (k == Layers.COLD_OCEAN) {
				n = Layers.DEEP_COLD_OCEAN;
			} else if (k == Layers.FROZEN_OCEAN) {
				n = Layers.DEEP_FROZEN_OCEAN;
			} else if (k == MOUNTAINS) {
				n = WOODED_MOUNTAINS;
			} else if (k == SAVANNA) {
				n = SAVANNA_PLATEAU;
			} else if (Layers.isSame(k, WOODED_BADLANDS_PLATEAU)) {
				n = BADLANDS;
			} else if ((k == Layers.DEEP_OCEAN || k == Layers.DEEP_LUKEWARM_OCEAN || k == Layers.DEEP_COLD_OCEAN || k == Layers.DEEP_FROZEN_OCEAN)
				&& context.nextRandom(3) == 0) {
				n = context.nextRandom(2) == 0 ? PLAINS : FOREST;
			}

			if (m == 0 && n != k) {
				Biome biome2 = Biome.getMutatedVariant(Registry.BIOME.byId(n));
				n = biome2 == null ? k : Registry.BIOME.getId(biome2);
			}

			if (n != k) {
				int o = 0;
				if (Layers.isSame(area.get(this.getParentX(i + 1), this.getParentY(j + 0)), k)) {
					o++;
				}

				if (Layers.isSame(area.get(this.getParentX(i + 2), this.getParentY(j + 1)), k)) {
					o++;
				}

				if (Layers.isSame(area.get(this.getParentX(i + 0), this.getParentY(j + 1)), k)) {
					o++;
				}

				if (Layers.isSame(area.get(this.getParentX(i + 1), this.getParentY(j + 2)), k)) {
					o++;
				}

				if (o >= 3) {
					return n;
				}
			}
		}

		return k;
	}
}
