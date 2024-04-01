package net.minecraft.world.entity.ambient;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class Batato extends Bat {
	public Batato(EntityType<? extends Bat> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public boolean isPotato() {
		return true;
	}
}
