package net.minecraft.world.entity.ambient;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public abstract class AmbientCreature extends Mob {
	protected AmbientCreature(EntityType<? extends AmbientCreature> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}
}
