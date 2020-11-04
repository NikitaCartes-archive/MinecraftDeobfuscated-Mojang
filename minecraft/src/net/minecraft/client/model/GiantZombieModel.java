package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Giant;

@Environment(EnvType.CLIENT)
public class GiantZombieModel extends AbstractZombieModel<Giant> {
	public GiantZombieModel(ModelPart modelPart) {
		super(modelPart);
	}

	public boolean isAggressive(Giant giant) {
		return false;
	}
}
