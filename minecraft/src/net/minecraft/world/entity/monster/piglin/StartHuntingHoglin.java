package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.hoglin.Hoglin;

public class StartHuntingHoglin<E extends Piglin> extends Behavior<E> {
	public StartHuntingHoglin() {
		super(
			ImmutableMap.of(
				MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLIN,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.ANGRY_AT,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.HUNTED_RECENTLY,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS,
				MemoryStatus.REGISTERED
			)
		);
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Piglin piglin) {
		return !piglin.isBaby() && !PiglinAi.hasAnyoneNearbyHuntedRecently(piglin);
	}

	protected void start(ServerLevel serverLevel, E piglin, long l) {
		Hoglin hoglin = (Hoglin)piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLIN).get();
		PiglinAi.setAngerTarget(piglin, hoglin);
		PiglinAi.dontKillAnyMoreHoglinsForAWhile(piglin);
		PiglinAi.broadcastAngerTarget(piglin, hoglin);
		PiglinAi.broadcastDontKillAnyMoreHoglinsForAWhile(piglin);
	}
}
