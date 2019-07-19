package net.minecraft.world.level.newbiome.layer;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum RiverLayer implements CastleTransformer {
	INSTANCE;

	public static final int RIVER = Registry.BIOME.getId(Biomes.RIVER);

	@Override
	public int apply(Context context, int i, int j, int k, int l, int m) {
		int n = riverFilter(m);
		return n == riverFilter(l) && n == riverFilter(i) && n == riverFilter(j) && n == riverFilter(k) ? -1 : RIVER;
	}

	private static int riverFilter(int i) {
		return i >= 2 ? 2 + (i & 1) : i;
	}
}
