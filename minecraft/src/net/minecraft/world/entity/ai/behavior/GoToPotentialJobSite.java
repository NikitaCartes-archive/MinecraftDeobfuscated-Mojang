package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;

public class GoToPotentialJobSite extends Behavior<Villager> {
	final float speedModifier;

	public GoToPotentialJobSite(float f) {
		super(ImmutableMap.of(MemoryModuleType.POTENTIAL_JOB_SITE, MemoryStatus.VALUE_PRESENT));
		this.speedModifier = f;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
		return (Boolean)villager.getBrain()
			.getActiveNonCoreActivity()
			.map(activity -> activity == Activity.IDLE || activity == Activity.WORK || activity == Activity.PLAY)
			.orElse(true);
	}

	protected void start(ServerLevel serverLevel, Villager villager, long l) {
		BehaviorUtils.setWalkAndLookTargetMemories(
			villager, ((GlobalPos)villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get()).pos(), this.speedModifier, 1
		);
	}
}
