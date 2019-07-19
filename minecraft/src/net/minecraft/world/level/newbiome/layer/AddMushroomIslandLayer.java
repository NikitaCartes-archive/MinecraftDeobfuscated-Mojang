package net.minecraft.world.level.newbiome.layer;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.BishopTransformer;

public enum AddMushroomIslandLayer implements BishopTransformer {
	INSTANCE;

	private static final int MUSHROOM_FIELDS = Registry.BIOME.getId(Biomes.MUSHROOM_FIELDS);

	@Override
	public int apply(Context context, int i, int j, int k, int l, int m) {
		return Layers.isShallowOcean(m)
				&& Layers.isShallowOcean(l)
				&& Layers.isShallowOcean(i)
				&& Layers.isShallowOcean(k)
				&& Layers.isShallowOcean(j)
				&& context.nextRandom(100) == 0
			? MUSHROOM_FIELDS
			: m;
	}
}
