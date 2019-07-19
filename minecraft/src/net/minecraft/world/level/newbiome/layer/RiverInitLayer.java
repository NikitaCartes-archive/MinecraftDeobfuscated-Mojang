package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C0Transformer;

public enum RiverInitLayer implements C0Transformer {
	INSTANCE;

	@Override
	public int apply(Context context, int i) {
		return Layers.isShallowOcean(i) ? i : context.nextRandom(299999) + 2;
	}
}
