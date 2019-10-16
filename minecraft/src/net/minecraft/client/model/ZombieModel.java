package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.monster.Zombie;

@Environment(EnvType.CLIENT)
public class ZombieModel<T extends Zombie> extends AbstractZombieModel<T> {
	public ZombieModel(float f, boolean bl) {
		this(f, 0.0F, 64, bl ? 32 : 64);
	}

	protected ZombieModel(float f, float g, int i, int j) {
		super(f, g, i, j);
	}

	public boolean isAggressive(T zombie) {
		return zombie.isAggressive();
	}
}
