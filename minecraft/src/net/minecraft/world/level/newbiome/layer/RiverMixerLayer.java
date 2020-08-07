package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2;
import net.minecraft.world.level.newbiome.layer.traits.DimensionOffset0Transformer;

public enum RiverMixerLayer implements AreaTransformer2, DimensionOffset0Transformer {
	INSTANCE;

	@Override
	public int applyPixel(Context context, Area area, Area area2, int i, int j) {
		int k = area.get(this.getParentX(i), this.getParentY(j));
		int l = area2.get(this.getParentX(i), this.getParentY(j));
		if (Layers.isOcean(k)) {
			return k;
		} else if (l == 7) {
			if (k == 12) {
				return 11;
			} else {
				return k != 14 && k != 15 ? l & 0xFF : 15;
			}
		} else {
			return k;
		}
	}
}
