package net.minecraft.world.level.newbiome.layer;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C1Transformer;

public enum RareBiomeSpotLayer implements C1Transformer {
	INSTANCE;

	private static final int PLAINS = BuiltinRegistries.BIOME.getId(Biomes.PLAINS);
	private static final int SUNFLOWER_PLAINS = BuiltinRegistries.BIOME.getId(Biomes.SUNFLOWER_PLAINS);

	@Override
	public int apply(Context context, int i) {
		return context.nextRandom(57) == 0 && i == PLAINS ? SUNFLOWER_PLAINS : i;
	}
}
