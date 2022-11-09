package net.minecraft.world.entity.ai.behavior;

import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SetWalkTargetFromLookTarget {
	public static OneShot<LivingEntity> create(float f, int i) {
		return create(livingEntity -> true, livingEntity -> f, i);
	}

	public static OneShot<LivingEntity> create(Predicate<LivingEntity> predicate, Function<LivingEntity, Float> function, int i) {
		return BehaviorBuilder.create(
			instance -> instance.group(instance.absent(MemoryModuleType.WALK_TARGET), instance.present(MemoryModuleType.LOOK_TARGET))
					.apply(instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, livingEntity, l) -> {
							if (!predicate.test(livingEntity)) {
								return false;
							} else {
								memoryAccessor.set(new WalkTarget(instance.get(memoryAccessor2), (Float)function.apply(livingEntity), i));
								return true;
							}
						})
		);
	}
}
