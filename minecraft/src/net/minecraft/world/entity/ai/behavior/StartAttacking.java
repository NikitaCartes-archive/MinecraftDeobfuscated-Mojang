package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StartAttacking {
	public static <E extends Mob> BehaviorControl<E> create(StartAttacking.TargetFinder<E> targetFinder) {
		return create((serverLevel, mob) -> true, targetFinder);
	}

	public static <E extends Mob> BehaviorControl<E> create(
		StartAttacking.StartAttackingCondition<E> startAttackingCondition, StartAttacking.TargetFinder<E> targetFinder
	) {
		return BehaviorBuilder.create(
			instance -> instance.group(instance.absent(MemoryModuleType.ATTACK_TARGET), instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE))
					.apply(instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, mob, l) -> {
							if (!startAttackingCondition.test(serverLevel, (E)mob)) {
								return false;
							} else {
								Optional<? extends LivingEntity> optional = targetFinder.get(serverLevel, (E)mob);
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

	@FunctionalInterface
	public interface StartAttackingCondition<E> {
		boolean test(ServerLevel serverLevel, E object);
	}

	@FunctionalInterface
	public interface TargetFinder<E> {
		Optional<? extends LivingEntity> get(ServerLevel serverLevel, E object);
	}
}
