package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.layer.LayerBiomes;

public interface DimensionTransformer extends LayerBiomes {
	int getParentX(int i);

	int getParentY(int i);
}
