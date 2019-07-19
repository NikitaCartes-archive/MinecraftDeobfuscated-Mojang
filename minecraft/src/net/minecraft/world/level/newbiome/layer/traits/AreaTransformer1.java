package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.context.BigContext;

public interface AreaTransformer1 extends DimensionTransformer {
	default <R extends Area> AreaFactory<R> run(BigContext<R> bigContext, AreaFactory<R> areaFactory) {
		return () -> {
			R area = areaFactory.make();
			return bigContext.createResult((i, j) -> {
				bigContext.initRandom((long)i, (long)j);
				return this.applyPixel(bigContext, area, i, j);
			}, area);
		};
	}

	int applyPixel(BigContext<?> bigContext, Area area, int i, int j);
}
