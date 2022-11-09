package net.minecraft.world.entity.monster.piglin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;

public class StartAdmiringItemIfSeen {
	public static BehaviorControl<LivingEntity> create(int i) {
		return BehaviorBuilder.create(
			instance -> instance.group(
						instance.present(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM),
						instance.absent(MemoryModuleType.ADMIRING_ITEM),
						instance.absent(MemoryModuleType.ADMIRING_DISABLED),
						instance.absent(MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM)
					)
					.apply(instance, (memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4) -> (serverLevel, livingEntity, l) -> {
							ItemEntity itemEntity = instance.get(memoryAccessor);
							if (!PiglinAi.isLovedItem(itemEntity.getItem())) {
								return false;
							} else {
								memoryAccessor2.setWithExpiry(true, (long)i);
								return true;
							}
						})
		);
	}
}
