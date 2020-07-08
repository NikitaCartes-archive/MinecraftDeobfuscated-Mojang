package net.minecraft.world.level.newbiome.layer;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2;
import net.minecraft.world.level.newbiome.layer.traits.DimensionOffset0Transformer;

public enum RiverMixerLayer implements AreaTransformer2, DimensionOffset0Transformer {
	INSTANCE;

	private static final int FROZEN_RIVER = BuiltinRegistries.BIOME.getId(Biomes.FROZEN_RIVER);
	private static final int SNOWY_TUNDRA = BuiltinRegistries.BIOME.getId(Biomes.SNOWY_TUNDRA);
	private static final int MUSHROOM_FIELDS = BuiltinRegistries.BIOME.getId(Biomes.MUSHROOM_FIELDS);
	private static final int MUSHROOM_FIELD_SHORE = BuiltinRegistries.BIOME.getId(Biomes.MUSHROOM_FIELD_SHORE);
	private static final int RIVER = BuiltinRegistries.BIOME.getId(Biomes.RIVER);

	@Override
	public int applyPixel(Context context, Area area, Area area2, int i, int j) {
		int k = area.get(this.getParentX(i), this.getParentY(j));
		int l = area2.get(this.getParentX(i), this.getParentY(j));
		if (Layers.isOcean(k)) {
			return k;
		} else if (l == RIVER) {
			if (k == SNOWY_TUNDRA) {
				return FROZEN_RIVER;
			} else {
				return k != MUSHROOM_FIELDS && k != MUSHROOM_FIELD_SHORE ? l & 0xFF : MUSHROOM_FIELD_SHORE;
			}
		} else {
			return k;
		}
	}
}
