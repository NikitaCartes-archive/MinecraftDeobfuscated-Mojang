package net.minecraft.world.entity;

import javax.annotation.Nullable;

public interface Targeting {
	@Nullable
	LivingEntity getTarget();
}
