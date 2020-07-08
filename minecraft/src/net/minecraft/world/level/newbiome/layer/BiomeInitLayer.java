package net.minecraft.world.level.newbiome.layer;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C0Transformer;

public class BiomeInitLayer implements C0Transformer {
	private static final int BIRCH_FOREST = BuiltinRegistries.BIOME.getId(Biomes.BIRCH_FOREST);
	private static final int DESERT = BuiltinRegistries.BIOME.getId(Biomes.DESERT);
	private static final int MOUNTAINS = BuiltinRegistries.BIOME.getId(Biomes.MOUNTAINS);
	private static final int FOREST = BuiltinRegistries.BIOME.getId(Biomes.FOREST);
	private static final int SNOWY_TUNDRA = BuiltinRegistries.BIOME.getId(Biomes.SNOWY_TUNDRA);
	private static final int JUNGLE = BuiltinRegistries.BIOME.getId(Biomes.JUNGLE);
	private static final int BADLANDS_PLATEAU = BuiltinRegistries.BIOME.getId(Biomes.BADLANDS_PLATEAU);
	private static final int WOODED_BADLANDS_PLATEAU = BuiltinRegistries.BIOME.getId(Biomes.WOODED_BADLANDS_PLATEAU);
	private static final int MUSHROOM_FIELDS = BuiltinRegistries.BIOME.getId(Biomes.MUSHROOM_FIELDS);
	private static final int PLAINS = BuiltinRegistries.BIOME.getId(Biomes.PLAINS);
	private static final int GIANT_TREE_TAIGA = BuiltinRegistries.BIOME.getId(Biomes.GIANT_TREE_TAIGA);
	private static final int DARK_FOREST = BuiltinRegistries.BIOME.getId(Biomes.DARK_FOREST);
	private static final int SAVANNA = BuiltinRegistries.BIOME.getId(Biomes.SAVANNA);
	private static final int SWAMP = BuiltinRegistries.BIOME.getId(Biomes.SWAMP);
	private static final int TAIGA = BuiltinRegistries.BIOME.getId(Biomes.TAIGA);
	private static final int SNOWY_TAIGA = BuiltinRegistries.BIOME.getId(Biomes.SNOWY_TAIGA);
	private static final int[] LEGACY_WARM_BIOMES = new int[]{DESERT, FOREST, MOUNTAINS, SWAMP, PLAINS, TAIGA};
	private static final int[] WARM_BIOMES = new int[]{DESERT, DESERT, DESERT, SAVANNA, SAVANNA, PLAINS};
	private static final int[] MEDIUM_BIOMES = new int[]{FOREST, DARK_FOREST, MOUNTAINS, PLAINS, BIRCH_FOREST, SWAMP};
	private static final int[] COLD_BIOMES = new int[]{FOREST, MOUNTAINS, TAIGA, PLAINS};
	private static final int[] ICE_BIOMES = new int[]{SNOWY_TUNDRA, SNOWY_TUNDRA, SNOWY_TUNDRA, SNOWY_TAIGA};
	private int[] warmBiomes = WARM_BIOMES;

	public BiomeInitLayer(boolean bl) {
		if (bl) {
			this.warmBiomes = LEGACY_WARM_BIOMES;
		}
	}

	@Override
	public int apply(Context context, int i) {
		int j = (i & 3840) >> 8;
		i &= -3841;
		if (!Layers.isOcean(i) && i != MUSHROOM_FIELDS) {
			switch (i) {
				case 1:
					if (j > 0) {
						return context.nextRandom(3) == 0 ? BADLANDS_PLATEAU : WOODED_BADLANDS_PLATEAU;
					}

					return this.warmBiomes[context.nextRandom(this.warmBiomes.length)];
				case 2:
					if (j > 0) {
						return JUNGLE;
					}

					return MEDIUM_BIOMES[context.nextRandom(MEDIUM_BIOMES.length)];
				case 3:
					if (j > 0) {
						return GIANT_TREE_TAIGA;
					}

					return COLD_BIOMES[context.nextRandom(COLD_BIOMES.length)];
				case 4:
					return ICE_BIOMES[context.nextRandom(ICE_BIOMES.length)];
				default:
					return MUSHROOM_FIELDS;
			}
		} else {
			return i;
		}
	}
}
