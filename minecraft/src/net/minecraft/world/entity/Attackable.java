package net.minecraft.world.entity;

import javax.annotation.Nullable;

public interface Attackable {
	@Nullable
	LivingEntity getLastAttacker();
}
