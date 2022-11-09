package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class Mount {
	private static final int CLOSE_ENOUGH_TO_START_RIDING_DIST = 1;

	public static BehaviorControl<LivingEntity> create(float f) {
		return BehaviorBuilder.create(
			instance -> instance.group(
						instance.registered(MemoryModuleType.LOOK_TARGET), instance.absent(MemoryModuleType.WALK_TARGET), instance.present(MemoryModuleType.RIDE_TARGET)
					)
					.apply(instance, (memoryAccessor, memoryAccessor2, memoryAccessor3) -> (serverLevel, livingEntity, l) -> {
							if (livingEntity.isPassenger()) {
								return false;
							} else {
								Entity entity = instance.get(memoryAccessor3);
								if (entity.closerThan(livingEntity, 1.0)) {
									livingEntity.startRiding(entity);
								} else {
									memoryAccessor.set(new EntityTracker(entity, true));
									memoryAccessor2.set(new WalkTarget(new EntityTracker(entity, false), f, 1));
								}

								return true;
							}
						})
		);
	}
}
