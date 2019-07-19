package net.minecraft.client.renderer.culling;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.AABB;

@Environment(EnvType.CLIENT)
public interface Culler {
	boolean isVisible(AABB aABB);

	void prepare(double d, double e, double f);
}
