package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

public interface BishopTransformer extends AreaTransformer1, DimensionOffset1Transformer {
	int apply(Context context, int i, int j, int k, int l, int m);

	@Override
	default int applyPixel(BigContext<?> bigContext, Area area, int i, int j) {
		return this.apply(
			bigContext,
			area.get(this.getParentX(i + 0), this.getParentY(j + 2)),
			area.get(this.getParentX(i + 2), this.getParentY(j + 2)),
			area.get(this.getParentX(i + 2), this.getParentY(j + 0)),
			area.get(this.getParentX(i + 0), this.getParentY(j + 0)),
			area.get(this.getParentX(i + 1), this.getParentY(j + 1))
		);
	}
}
