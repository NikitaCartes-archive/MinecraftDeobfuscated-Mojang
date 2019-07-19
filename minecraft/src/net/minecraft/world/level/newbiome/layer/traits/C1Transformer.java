package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

public interface C1Transformer extends AreaTransformer1, DimensionOffset1Transformer {
	int apply(Context context, int i);

	@Override
	default int applyPixel(BigContext<?> bigContext, Area area, int i, int j) {
		int k = area.get(this.getParentX(i + 1), this.getParentY(j + 1));
		return this.apply(bigContext, k);
	}
}
