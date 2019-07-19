package net.minecraft.world.level.newbiome.layer;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum ShoreLayer implements CastleTransformer {
	INSTANCE;

	private static final int BEACH = Registry.BIOME.getId(Biomes.BEACH);
	private static final int SNOWY_BEACH = Registry.BIOME.getId(Biomes.SNOWY_BEACH);
	private static final int DESERT = Registry.BIOME.getId(Biomes.DESERT);
	private static final int MOUNTAINS = Registry.BIOME.getId(Biomes.MOUNTAINS);
	private static final int WOODED_MOUNTAINS = Registry.BIOME.getId(Biomes.WOODED_MOUNTAINS);
	private static final int FOREST = Registry.BIOME.getId(Biomes.FOREST);
	private static final int JUNGLE = Registry.BIOME.getId(Biomes.JUNGLE);
	private static final int JUNGLE_EDGE = Registry.BIOME.getId(Biomes.JUNGLE_EDGE);
	private static final int JUNGLE_HILLS = Registry.BIOME.getId(Biomes.JUNGLE_HILLS);
	private static final int BADLANDS = Registry.BIOME.getId(Biomes.BADLANDS);
	private static final int WOODED_BADLANDS_PLATEAU = Registry.BIOME.getId(Biomes.WOODED_BADLANDS_PLATEAU);
	private static final int BADLANDS_PLATEAU = Registry.BIOME.getId(Biomes.BADLANDS_PLATEAU);
	private static final int ERODED_BADLANDS = Registry.BIOME.getId(Biomes.ERODED_BADLANDS);
	private static final int MODIFIED_WOODED_BADLANDS_PLATEAU = Registry.BIOME.getId(Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU);
	private static final int MODIFIED_BADLANDS_PLATEAU = Registry.BIOME.getId(Biomes.MODIFIED_BADLANDS_PLATEAU);
	private static final int MUSHROOM_FIELDS = Registry.BIOME.getId(Biomes.MUSHROOM_FIELDS);
	private static final int MUSHROOM_FIELD_SHORE = Registry.BIOME.getId(Biomes.MUSHROOM_FIELD_SHORE);
	private static final int RIVER = Registry.BIOME.getId(Biomes.RIVER);
	private static final int MOUNTAIN_EDGE = Registry.BIOME.getId(Biomes.MOUNTAIN_EDGE);
	private static final int STONE_SHORE = Registry.BIOME.getId(Biomes.STONE_SHORE);
	private static final int SWAMP = Registry.BIOME.getId(Biomes.SWAMP);
	private static final int TAIGA = Registry.BIOME.getId(Biomes.TAIGA);

	@Override
	public int apply(Context context, int i, int j, int k, int l, int m) {
		Biome biome = Registry.BIOME.byId(m);
		if (m == MUSHROOM_FIELDS) {
			if (Layers.isShallowOcean(i) || Layers.isShallowOcean(j) || Layers.isShallowOcean(k) || Layers.isShallowOcean(l)) {
				return MUSHROOM_FIELD_SHORE;
			}
		} else if (biome != null && biome.getBiomeCategory() == Biome.BiomeCategory.JUNGLE) {
			if (!isJungleCompatible(i) || !isJungleCompatible(j) || !isJungleCompatible(k) || !isJungleCompatible(l)) {
				return JUNGLE_EDGE;
			}

			if (Layers.isOcean(i) || Layers.isOcean(j) || Layers.isOcean(k) || Layers.isOcean(l)) {
				return BEACH;
			}
		} else if (m != MOUNTAINS && m != WOODED_MOUNTAINS && m != MOUNTAIN_EDGE) {
			if (biome != null && biome.getPrecipitation() == Biome.Precipitation.SNOW) {
				if (!Layers.isOcean(m) && (Layers.isOcean(i) || Layers.isOcean(j) || Layers.isOcean(k) || Layers.isOcean(l))) {
					return SNOWY_BEACH;
				}
			} else if (m != BADLANDS && m != WOODED_BADLANDS_PLATEAU) {
				if (!Layers.isOcean(m) && m != RIVER && m != SWAMP && (Layers.isOcean(i) || Layers.isOcean(j) || Layers.isOcean(k) || Layers.isOcean(l))) {
					return BEACH;
				}
			} else if (!Layers.isOcean(i)
				&& !Layers.isOcean(j)
				&& !Layers.isOcean(k)
				&& !Layers.isOcean(l)
				&& (!this.isMesa(i) || !this.isMesa(j) || !this.isMesa(k) || !this.isMesa(l))) {
				return DESERT;
			}
		} else if (!Layers.isOcean(m) && (Layers.isOcean(i) || Layers.isOcean(j) || Layers.isOcean(k) || Layers.isOcean(l))) {
			return STONE_SHORE;
		}

		return m;
	}

	private static boolean isJungleCompatible(int i) {
		return Registry.BIOME.byId(i) != null && Registry.BIOME.byId(i).getBiomeCategory() == Biome.BiomeCategory.JUNGLE
			? true
			: i == JUNGLE_EDGE || i == JUNGLE || i == JUNGLE_HILLS || i == FOREST || i == TAIGA || Layers.isOcean(i);
	}

	private boolean isMesa(int i) {
		return i == BADLANDS
			|| i == WOODED_BADLANDS_PLATEAU
			|| i == BADLANDS_PLATEAU
			|| i == ERODED_BADLANDS
			|| i == MODIFIED_WOODED_BADLANDS_PLATEAU
			|| i == MODIFIED_BADLANDS_PLATEAU;
	}
}
