package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

public interface AreaTransformer2 extends DimensionTransformer {
	default <R extends Area> AreaFactory<R> run(BigContext<R> bigContext, AreaFactory<R> areaFactory, AreaFactory<R> areaFactory2) {
		return () -> {
			R area = areaFactory.make();
			R area2 = areaFactory2.make();
			return bigContext.createResult((i, j) -> {
				bigContext.initRandom((long)i, (long)j);
				return this.applyPixel(bigContext, area, area2, i, j);
			}, area, area2);
		};
	}

	int applyPixel(Context context, Area area, Area area2, int i, int j);
}
