package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C1Transformer;

public enum RareBiomeSpotLayer implements C1Transformer {
	INSTANCE;

	@Override
	public int apply(Context context, int i) {
		return context.nextRandom(57) == 0 && i == 1 ? 129 : i;
	}
}
