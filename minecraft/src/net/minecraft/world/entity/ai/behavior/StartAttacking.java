package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StartAttacking {
	public static <E extends Mob> BehaviorControl<E> create(Function<E, Optional<? extends LivingEntity>> function) {
		return create(mob -> true, function);
	}

	public static <E extends Mob> BehaviorControl<E> create(Predicate<E> predicate, Function<E, Optional<? extends LivingEntity>> function) {
		return BehaviorBuilder.create(
			instance -> instance.group(instance.absent(MemoryModuleType.ATTACK_TARGET), instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE))
					.apply(instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, mob, l) -> {
							if (!predicate.test(mob)) {
								return false;
							} else {
								Optional<? extends LivingEntity> optional = (Optional<? extends LivingEntity>)function.apply(mob);
								if (optional.isEmpty()) {
									return false;
								} else {
									LivingEntity livingEntity = (LivingEntity)optional.get();
									if (!mob.canAttack(livingEntity)) {
										return false;
									} else {
										memoryAccessor.set(livingEntity);
										memoryAccessor2.erase();
										return true;
									}
								}
							}
						})
		);
	}
}
