package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StopAdmiringIfTiredOfTryingToReachItem<E extends Piglin> extends Behavior<E> {
	private final int maxTimeToReachItem;
	private final int disableTime;

	public StopAdmiringIfTiredOfTryingToReachItem(int i, int j) {
		super(
			ImmutableMap.of(
				MemoryModuleType.ADMIRING_ITEM,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM,
				MemoryStatus.REGISTERED,
				MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM,
				MemoryStatus.REGISTERED
			)
		);
		this.maxTimeToReachItem = i;
		this.disableTime = j;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E piglin) {
		return piglin.getOffhandItem().isEmpty();
	}

	protected void start(ServerLevel serverLevel, E piglin, long l) {
		Brain<Piglin> brain = piglin.getBrain();
		Optional<Integer> optional = brain.getMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
		if (!optional.isPresent()) {
			brain.setMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, 0);
		} else {
			int i = (Integer)optional.get();
			if (i > this.maxTimeToReachItem) {
				brain.eraseMemory(MemoryModuleType.ADMIRING_ITEM);
				brain.eraseMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
				brain.setMemoryWithExpiry(MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, true, (long)this.disableTime);
			} else {
				brain.setMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, i + 1);
			}
		}
	}
}
