package net.minecraft.world.entity.ai.behavior;

import java.util.function.Function;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class BabyFollowAdult {
	public static OneShot<AgeableMob> create(UniformInt uniformInt, float f) {
		return create(uniformInt, livingEntity -> f);
	}

	public static OneShot<AgeableMob> create(UniformInt uniformInt, Function<LivingEntity, Float> function) {
		return BehaviorBuilder.create(
			instance -> instance.group(
						instance.present(MemoryModuleType.NEAREST_VISIBLE_ADULT),
						instance.registered(MemoryModuleType.LOOK_TARGET),
						instance.absent(MemoryModuleType.WALK_TARGET)
					)
					.apply(
						instance,
						(memoryAccessor, memoryAccessor2, memoryAccessor3) -> (serverLevel, ageableMob, l) -> {
								if (!ageableMob.isBaby()) {
									return false;
								} else {
									AgeableMob ageableMob2 = instance.get(memoryAccessor);
									if (ageableMob.closerThan(ageableMob2, (double)(uniformInt.getMaxValue() + 1))
										&& !ageableMob.closerThan(ageableMob2, (double)uniformInt.getMinValue())) {
										WalkTarget walkTarget = new WalkTarget(new EntityTracker(ageableMob2, false), (Float)function.apply(ageableMob), uniformInt.getMinValue() - 1);
										memoryAccessor2.set(new EntityTracker(ageableMob2, true));
										memoryAccessor3.set(walkTarget);
										return true;
									} else {
										return false;
									}
								}
							}
					)
		);
	}
}
