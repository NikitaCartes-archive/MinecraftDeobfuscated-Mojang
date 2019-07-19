package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class UpdateActivityFromSchedule extends Behavior<LivingEntity> {
	public UpdateActivityFromSchedule() {
		super(ImmutableMap.of());
	}

	@Override
	protected void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		livingEntity.getBrain().updateActivity(serverLevel.getDayTime(), serverLevel.getGameTime());
	}
}
