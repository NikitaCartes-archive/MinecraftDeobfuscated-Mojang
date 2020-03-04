package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StopHoldingItemIfNoLongerAdmiring<E extends Piglin> extends Behavior<E> {
	public StopHoldingItemIfNoLongerAdmiring() {
		super(ImmutableMap.of(MemoryModuleType.ADMIRING_ITEM, MemoryStatus.VALUE_ABSENT));
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E piglin) {
		return !piglin.getOffhandItem().isEmpty();
	}

	protected void start(ServerLevel serverLevel, E piglin, long l) {
		PiglinAi.stopHoldingOffHandItem(piglin, true);
	}
}
