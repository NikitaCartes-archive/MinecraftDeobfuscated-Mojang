package net.minecraft.world.entity.monster.piglin;

import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StopAdmiringIfTiredOfTryingToReachItem {
	public static BehaviorControl<LivingEntity> create(int i, int j) {
		return BehaviorBuilder.create(
			instance -> instance.group(
						instance.present(MemoryModuleType.ADMIRING_ITEM),
						instance.present(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM),
						instance.registered(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM),
						instance.registered(MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM)
					)
					.apply(instance, (memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4) -> (serverLevel, livingEntity, l) -> {
							if (!livingEntity.getOffhandItem().isEmpty()) {
								return false;
							} else {
								Optional<Integer> optional = instance.tryGet(memoryAccessor3);
								if (optional.isEmpty()) {
									memoryAccessor3.set(0);
								} else {
									int k = (Integer)optional.get();
									if (k > i) {
										memoryAccessor.erase();
										memoryAccessor3.erase();
										memoryAccessor4.setWithExpiry(true, (long)j);
									} else {
										memoryAccessor3.set(k + 1);
									}
								}

								return true;
							}
						})
		);
	}
}
