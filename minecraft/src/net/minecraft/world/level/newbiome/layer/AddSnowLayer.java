package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C1Transformer;

public enum AddSnowLayer implements C1Transformer {
	INSTANCE;

	@Override
	public int apply(Context context, int i) {
		if (Layers.isShallowOcean(i)) {
			return i;
		} else {
			int j = context.nextRandom(6);
			if (j == 0) {
				return 4;
			} else {
				return j == 1 ? 3 : 1;
			}
		}
	}
}
