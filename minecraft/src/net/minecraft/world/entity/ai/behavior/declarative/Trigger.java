package net.minecraft.world.entity.ai.behavior.declarative;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public interface Trigger<E extends LivingEntity> {
	boolean trigger(ServerLevel serverLevel, E livingEntity, long l);
}
