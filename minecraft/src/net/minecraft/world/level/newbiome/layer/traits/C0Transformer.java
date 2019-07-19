package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

public interface C0Transformer extends AreaTransformer1, DimensionOffset0Transformer {
	int apply(Context context, int i);

	@Override
	default int applyPixel(BigContext<?> bigContext, Area area, int i, int j) {
		return this.apply(bigContext, area.get(this.getParentX(i), this.getParentY(j)));
	}
}
