package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum SmoothLayer implements CastleTransformer {
	INSTANCE;

	@Override
	public int apply(Context context, int i, int j, int k, int l, int m) {
		boolean bl = j == l;
		boolean bl2 = i == k;
		if (bl == bl2) {
			if (bl) {
				return context.nextRandom(2) == 0 ? l : i;
			} else {
				return m;
			}
		} else {
			return bl ? l : i;
		}
	}
}
