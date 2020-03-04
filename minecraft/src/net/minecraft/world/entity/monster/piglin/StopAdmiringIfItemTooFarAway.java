package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;

public class StopAdmiringIfItemTooFarAway<E extends Piglin> extends Behavior<E> {
	private final int maxDistanceToItem;

	public StopAdmiringIfItemTooFarAway(int i) {
		super(ImmutableMap.of(MemoryModuleType.ADMIRING_ITEM, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.REGISTERED));
		this.maxDistanceToItem = i;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E piglin) {
		if (!piglin.getOffhandItem().isEmpty()) {
			return false;
		} else {
			Optional<ItemEntity> optional = piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
			return !optional.isPresent() ? true : !((ItemEntity)optional.get()).closerThan(piglin, (double)this.maxDistanceToItem);
		}
	}

	protected void start(ServerLevel serverLevel, E piglin, long l) {
		piglin.getBrain().eraseMemory(MemoryModuleType.ADMIRING_ITEM);
	}
}
