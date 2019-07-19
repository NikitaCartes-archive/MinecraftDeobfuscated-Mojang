package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum AddDeepOceanLayer implements CastleTransformer {
	INSTANCE;

	@Override
	public int apply(Context context, int i, int j, int k, int l, int m) {
		if (Layers.isShallowOcean(m)) {
			int n = 0;
			if (Layers.isShallowOcean(i)) {
				n++;
			}

			if (Layers.isShallowOcean(j)) {
				n++;
			}

			if (Layers.isShallowOcean(l)) {
				n++;
			}

			if (Layers.isShallowOcean(k)) {
				n++;
			}

			if (n > 3) {
				if (m == Layers.WARM_OCEAN) {
					return Layers.DEEP_WARM_OCEAN;
				}

				if (m == Layers.LUKEWARM_OCEAN) {
					return Layers.DEEP_LUKEWARM_OCEAN;
				}

				if (m == Layers.OCEAN) {
					return Layers.DEEP_OCEAN;
				}

				if (m == Layers.COLD_OCEAN) {
					return Layers.DEEP_COLD_OCEAN;
				}

				if (m == Layers.FROZEN_OCEAN) {
					return Layers.DEEP_FROZEN_OCEAN;
				}

				return Layers.DEEP_OCEAN;
			}
		}

		return m;
	}
}
