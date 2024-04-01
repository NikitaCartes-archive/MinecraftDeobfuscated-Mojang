package net.minecraft.world.entity.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EyeOfPotato extends EyeOfEnder {
	public EyeOfPotato(EntityType<? extends EyeOfPotato> entityType, Level level) {
		super(entityType, level);
	}

	public EyeOfPotato(Level level, double d, double e, double f) {
		this(EntityType.EYE_OF_POTATO, level);
		this.setPos(d, e, f);
	}
}
