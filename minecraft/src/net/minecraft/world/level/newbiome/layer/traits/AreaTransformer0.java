package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

public interface AreaTransformer0 {
	default <R extends Area> AreaFactory<R> run(BigContext<R> bigContext) {
		return () -> bigContext.createResult((i, j) -> {
				bigContext.initRandom((long)i, (long)j);
				return this.applyPixel(bigContext, i, j);
			});
	}

	int applyPixel(Context context, int i, int j);
}
