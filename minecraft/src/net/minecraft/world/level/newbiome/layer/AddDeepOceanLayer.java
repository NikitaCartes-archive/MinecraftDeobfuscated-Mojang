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
				if (m == 44) {
					return 47;
				}

				if (m == 45) {
					return 48;
				}

				if (m == 0) {
					return 24;
				}

				if (m == 46) {
					return 49;
				}

				if (m == 10) {
					return 50;
				}

				return 24;
			}
		}

		return m;
	}
}
