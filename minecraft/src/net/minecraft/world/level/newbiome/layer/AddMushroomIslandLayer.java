package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.BishopTransformer;

public enum AddMushroomIslandLayer implements BishopTransformer {
	INSTANCE;

	@Override
	public int apply(Context context, int i, int j, int k, int l, int m) {
		return Layers.isShallowOcean(m)
				&& Layers.isShallowOcean(l)
				&& Layers.isShallowOcean(i)
				&& Layers.isShallowOcean(k)
				&& Layers.isShallowOcean(j)
				&& context.nextRandom(100) == 0
			? 14
			: m;
	}
}
