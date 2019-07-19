package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.BishopTransformer;

public enum AddIslandLayer implements BishopTransformer {
	INSTANCE;

	@Override
	public int apply(Context context, int i, int j, int k, int l, int m) {
		if (!Layers.isShallowOcean(m) || Layers.isShallowOcean(l) && Layers.isShallowOcean(k) && Layers.isShallowOcean(i) && Layers.isShallowOcean(j)) {
			if (!Layers.isShallowOcean(m)
				&& (Layers.isShallowOcean(l) || Layers.isShallowOcean(i) || Layers.isShallowOcean(k) || Layers.isShallowOcean(j))
				&& context.nextRandom(5) == 0) {
				if (Layers.isShallowOcean(l)) {
					return m == 4 ? 4 : l;
				}

				if (Layers.isShallowOcean(i)) {
					return m == 4 ? 4 : i;
				}

				if (Layers.isShallowOcean(k)) {
					return m == 4 ? 4 : k;
				}

				if (Layers.isShallowOcean(j)) {
					return m == 4 ? 4 : j;
				}
			}

			return m;
		} else {
			int n = 1;
			int o = 1;
			if (!Layers.isShallowOcean(l) && context.nextRandom(n++) == 0) {
				o = l;
			}

			if (!Layers.isShallowOcean(k) && context.nextRandom(n++) == 0) {
				o = k;
			}

			if (!Layers.isShallowOcean(i) && context.nextRandom(n++) == 0) {
				o = i;
			}

			if (!Layers.isShallowOcean(j) && context.nextRandom(n++) == 0) {
				o = j;
			}

			if (context.nextRandom(3) == 0) {
				return o;
			} else {
				return o == 4 ? 4 : m;
			}
		}
	}
}
