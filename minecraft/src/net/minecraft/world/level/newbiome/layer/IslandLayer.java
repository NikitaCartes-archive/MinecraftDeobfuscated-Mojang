package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer0;

public enum IslandLayer implements AreaTransformer0 {
	INSTANCE;

	@Override
	public int applyPixel(Context context, int i, int j) {
		if (i == 0 && j == 0) {
			return 1;
		} else {
			return context.nextRandom(10) == 0 ? 1 : 0;
		}
	}
}
