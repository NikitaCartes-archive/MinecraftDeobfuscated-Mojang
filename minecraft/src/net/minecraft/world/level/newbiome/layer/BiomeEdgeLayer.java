package net.minecraft.world.level.newbiome.layer;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum BiomeEdgeLayer implements CastleTransformer {
	INSTANCE;

	private static final int DESERT = BuiltinRegistries.BIOME.getId(Biomes.DESERT);
	private static final int MOUNTAINS = BuiltinRegistries.BIOME.getId(Biomes.MOUNTAINS);
	private static final int WOODED_MOUNTAINS = BuiltinRegistries.BIOME.getId(Biomes.WOODED_MOUNTAINS);
	private static final int SNOWY_TUNDRA = BuiltinRegistries.BIOME.getId(Biomes.SNOWY_TUNDRA);
	private static final int JUNGLE = BuiltinRegistries.BIOME.getId(Biomes.JUNGLE);
	private static final int BAMBOO_JUNGLE = BuiltinRegistries.BIOME.getId(Biomes.BAMBOO_JUNGLE);
	private static final int JUNGLE_EDGE = BuiltinRegistries.BIOME.getId(Biomes.JUNGLE_EDGE);
	private static final int BADLANDS = BuiltinRegistries.BIOME.getId(Biomes.BADLANDS);
	private static final int BADLANDS_PLATEAU = BuiltinRegistries.BIOME.getId(Biomes.BADLANDS_PLATEAU);
	private static final int WOODED_BADLANDS_PLATEAU = BuiltinRegistries.BIOME.getId(Biomes.WOODED_BADLANDS_PLATEAU);
	private static final int PLAINS = BuiltinRegistries.BIOME.getId(Biomes.PLAINS);
	private static final int GIANT_TREE_TAIGA = BuiltinRegistries.BIOME.getId(Biomes.GIANT_TREE_TAIGA);
	private static final int MOUNTAIN_EDGE = BuiltinRegistries.BIOME.getId(Biomes.MOUNTAIN_EDGE);
	private static final int SWAMP = BuiltinRegistries.BIOME.getId(Biomes.SWAMP);
	private static final int TAIGA = BuiltinRegistries.BIOME.getId(Biomes.TAIGA);
	private static final int SNOWY_TAIGA = BuiltinRegistries.BIOME.getId(Biomes.SNOWY_TAIGA);

	@Override
	public int apply(Context context, int i, int j, int k, int l, int m) {
		int[] is = new int[1];
		if (!this.checkEdge(is, i, j, k, l, m, MOUNTAINS, MOUNTAIN_EDGE)
			&& !this.checkEdgeStrict(is, i, j, k, l, m, WOODED_BADLANDS_PLATEAU, BADLANDS)
			&& !this.checkEdgeStrict(is, i, j, k, l, m, BADLANDS_PLATEAU, BADLANDS)
			&& !this.checkEdgeStrict(is, i, j, k, l, m, GIANT_TREE_TAIGA, TAIGA)) {
			if (m != DESERT || i != SNOWY_TUNDRA && j != SNOWY_TUNDRA && l != SNOWY_TUNDRA && k != SNOWY_TUNDRA) {
				if (m == SWAMP) {
					if (i == DESERT
						|| j == DESERT
						|| l == DESERT
						|| k == DESERT
						|| i == SNOWY_TAIGA
						|| j == SNOWY_TAIGA
						|| l == SNOWY_TAIGA
						|| k == SNOWY_TAIGA
						|| i == SNOWY_TUNDRA
						|| j == SNOWY_TUNDRA
						|| l == SNOWY_TUNDRA
						|| k == SNOWY_TUNDRA) {
						return PLAINS;
					}

					if (i == JUNGLE || k == JUNGLE || j == JUNGLE || l == JUNGLE || i == BAMBOO_JUNGLE || k == BAMBOO_JUNGLE || j == BAMBOO_JUNGLE || l == BAMBOO_JUNGLE) {
						return JUNGLE_EDGE;
					}
				}

				return m;
			} else {
				return WOODED_MOUNTAINS;
			}
		} else {
			return is[0];
		}
	}

	private boolean checkEdge(int[] is, int i, int j, int k, int l, int m, int n, int o) {
		if (!Layers.isSame(m, n)) {
			return false;
		} else {
			if (this.isValidTemperatureEdge(i, n) && this.isValidTemperatureEdge(j, n) && this.isValidTemperatureEdge(l, n) && this.isValidTemperatureEdge(k, n)) {
				is[0] = m;
			} else {
				is[0] = o;
			}

			return true;
		}
	}

	private boolean checkEdgeStrict(int[] is, int i, int j, int k, int l, int m, int n, int o) {
		if (m != n) {
			return false;
		} else {
			if (Layers.isSame(i, n) && Layers.isSame(j, n) && Layers.isSame(l, n) && Layers.isSame(k, n)) {
				is[0] = m;
			} else {
				is[0] = o;
			}

			return true;
		}
	}

	private boolean isValidTemperatureEdge(int i, int j) {
		if (Layers.isSame(i, j)) {
			return true;
		} else {
			Biome biome = BuiltinRegistries.BIOME.byId(i);
			Biome biome2 = BuiltinRegistries.BIOME.byId(j);
			if (biome != null && biome2 != null) {
				Biome.BiomeTempCategory biomeTempCategory = biome.getTemperatureCategory();
				Biome.BiomeTempCategory biomeTempCategory2 = biome2.getTemperatureCategory();
				return biomeTempCategory == biomeTempCategory2
					|| biomeTempCategory == Biome.BiomeTempCategory.MEDIUM
					|| biomeTempCategory2 == Biome.BiomeTempCategory.MEDIUM;
			} else {
				return false;
			}
		}
	}
}
