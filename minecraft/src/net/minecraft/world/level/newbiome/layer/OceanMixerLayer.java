package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2;
import net.minecraft.world.level.newbiome.layer.traits.DimensionOffset0Transformer;

public enum OceanMixerLayer implements AreaTransformer2, DimensionOffset0Transformer {
	INSTANCE;

	@Override
	public int applyPixel(Context context, Area area, Area area2, int i, int j) {
		int k = area.get(this.getParentX(i), this.getParentY(j));
		int l = area2.get(this.getParentX(i), this.getParentY(j));
		if (!Layers.isOcean(k)) {
			return k;
		} else {
			int m = 8;
			int n = 4;

			for (int o = -8; o <= 8; o += 4) {
				for (int p = -8; p <= 8; p += 4) {
					int q = area.get(this.getParentX(i + o), this.getParentY(j + p));
					if (!Layers.isOcean(q)) {
						if (l == 44) {
							return 45;
						}

						if (l == 10) {
							return 46;
						}
					}
				}
			}

			if (k == 24) {
				if (l == 45) {
					return 48;
				}

				if (l == 0) {
					return 24;
				}

				if (l == 46) {
					return 49;
				}

				if (l == 10) {
					return 50;
				}
			}

			return l;
		}
	}
}
