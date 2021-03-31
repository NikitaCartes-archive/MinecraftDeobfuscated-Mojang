package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;

public class VillagerCalmDown extends Behavior<Villager> {
	private static final int SAFE_DISTANCE_FROM_DANGER = 36;

	public VillagerCalmDown() {
		super(ImmutableMap.of());
	}

	protected void start(ServerLevel serverLevel, Villager villager, long l) {
		boolean bl = VillagerPanicTrigger.isHurt(villager) || VillagerPanicTrigger.hasHostile(villager) || isCloseToEntityThatHurtMe(villager);
		if (!bl) {
			villager.getBrain().eraseMemory(MemoryModuleType.HURT_BY);
			villager.getBrain().eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
			villager.getBrain().updateActivityFromSchedule(serverLevel.getDayTime(), serverLevel.getGameTime());
		}
	}

	private static boolean isCloseToEntityThatHurtMe(Villager villager) {
		return villager.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY).filter(livingEntity -> livingEntity.distanceToSqr(villager) <= 36.0).isPresent();
	}
}
