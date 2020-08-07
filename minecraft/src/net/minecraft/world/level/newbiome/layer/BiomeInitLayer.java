package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C0Transformer;

public class BiomeInitLayer implements C0Transformer {
	private static final int[] LEGACY_WARM_BIOMES = new int[]{2, 4, 3, 6, 1, 5};
	private static final int[] WARM_BIOMES = new int[]{2, 2, 2, 35, 35, 1};
	private static final int[] MEDIUM_BIOMES = new int[]{4, 29, 3, 1, 27, 6};
	private static final int[] COLD_BIOMES = new int[]{4, 3, 5, 1};
	private static final int[] ICE_BIOMES = new int[]{12, 12, 12, 30};
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
		if (!Layers.isOcean(i) && i != 14) {
			switch (i) {
				case 1:
					if (j > 0) {
						return context.nextRandom(3) == 0 ? 39 : 38;
					}

					return this.warmBiomes[context.nextRandom(this.warmBiomes.length)];
				case 2:
					if (j > 0) {
						return 21;
					}

					return MEDIUM_BIOMES[context.nextRandom(MEDIUM_BIOMES.length)];
				case 3:
					if (j > 0) {
						return 32;
					}

					return COLD_BIOMES[context.nextRandom(COLD_BIOMES.length)];
				case 4:
					return ICE_BIOMES[context.nextRandom(ICE_BIOMES.length)];
				default:
					return 14;
			}
		} else {
			return i;
		}
	}
}
