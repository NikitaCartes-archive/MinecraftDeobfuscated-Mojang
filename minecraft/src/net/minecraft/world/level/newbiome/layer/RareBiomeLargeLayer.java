package net.minecraft.world.level.newbiome.layer;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C1Transformer;

public enum RareBiomeLargeLayer implements C1Transformer {
	INSTANCE;

	private static final int JUNGLE = BuiltinRegistries.BIOME.getId(Biomes.JUNGLE);
	private static final int BAMBOO_JUNGLE = BuiltinRegistries.BIOME.getId(Biomes.BAMBOO_JUNGLE);

	@Override
	public int apply(Context context, int i) {
		return context.nextRandom(10) == 0 && i == JUNGLE ? BAMBOO_JUNGLE : i;
	}
}
