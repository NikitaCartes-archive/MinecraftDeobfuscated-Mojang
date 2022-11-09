package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public interface BehaviorControl<E extends LivingEntity> {
	Behavior.Status getStatus();

	boolean tryStart(ServerLevel serverLevel, E livingEntity, long l);

	void tickOrStop(ServerLevel serverLevel, E livingEntity, long l);

	void doStop(ServerLevel serverLevel, E livingEntity, long l);

	String debugString();
}
