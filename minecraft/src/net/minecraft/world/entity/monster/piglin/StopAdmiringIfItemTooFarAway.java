package net.minecraft.world.entity.monster.piglin;

import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;

public class StopAdmiringIfItemTooFarAway<E extends Piglin> {
	public static BehaviorControl<LivingEntity> create(int i) {
		return BehaviorBuilder.create(
			instance -> instance.group(instance.present(MemoryModuleType.ADMIRING_ITEM), instance.registered(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM))
					.apply(instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, livingEntity, l) -> {
							if (!livingEntity.getOffhandItem().isEmpty()) {
								return false;
							} else {
								Optional<ItemEntity> optional = instance.tryGet(memoryAccessor2);
								if (optional.isPresent() && ((ItemEntity)optional.get()).closerThan(livingEntity, (double)i)) {
									return false;
								} else {
									memoryAccessor.erase();
									return true;
								}
							}
						})
		);
	}
}
