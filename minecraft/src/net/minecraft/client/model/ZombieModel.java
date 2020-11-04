package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Zombie;

@Environment(EnvType.CLIENT)
public class ZombieModel<T extends Zombie> extends AbstractZombieModel<T> {
	public ZombieModel(ModelPart modelPart) {
		super(modelPart);
	}

	public boolean isAggressive(T zombie) {
		return zombie.isAggressive();
	}
}
