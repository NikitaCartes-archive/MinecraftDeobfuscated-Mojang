package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum RemoveTooMuchOceanLayer implements CastleTransformer {
	INSTANCE;

	@Override
	public int apply(Context context, int i, int j, int k, int l, int m) {
		return Layers.isShallowOcean(m)
				&& Layers.isShallowOcean(i)
				&& Layers.isShallowOcean(j)
				&& Layers.isShallowOcean(l)
				&& Layers.isShallowOcean(k)
				&& context.nextRandom(2) == 0
			? 1
			: m;
	}
}
