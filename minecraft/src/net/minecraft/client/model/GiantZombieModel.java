package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.monster.Giant;

@Environment(EnvType.CLIENT)
public class GiantZombieModel extends AbstractZombieModel<Giant> {
	public GiantZombieModel() {
		this(0.0F, false);
	}

	public GiantZombieModel(float f, boolean bl) {
		super(RenderType::entitySolid, f, 0.0F, 64, bl ? 32 : 64);
	}

	public boolean isAggressive(Giant giant) {
		return false;
	}
}
