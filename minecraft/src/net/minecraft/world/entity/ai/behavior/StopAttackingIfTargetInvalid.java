package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StopAttackingIfTargetInvalid {
	private static final int TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE = 200;

	public static <E extends Mob> BehaviorControl<E> create(StopAttackingIfTargetInvalid.TargetErasedCallback<E> targetErasedCallback) {
		return create((serverLevel, livingEntity) -> false, targetErasedCallback, true);
	}

	public static <E extends Mob> BehaviorControl<E> create(StopAttackingIfTargetInvalid.StopAttackCondition stopAttackCondition) {
		return create(stopAttackCondition, (serverLevel, mob, livingEntity) -> {
		}, true);
	}

	public static <E extends Mob> BehaviorControl<E> create() {
		return create((serverLevel, livingEntity) -> false, (serverLevel, mob, livingEntity) -> {
		}, true);
	}

	public static <E extends Mob> BehaviorControl<E> create(
		StopAttackingIfTargetInvalid.StopAttackCondition stopAttackCondition, StopAttackingIfTargetInvalid.TargetErasedCallback<E> targetErasedCallback, boolean bl
	) {
		return BehaviorBuilder.create(
			instance -> instance.group(instance.present(MemoryModuleType.ATTACK_TARGET), instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE))
					.apply(
						instance,
						(memoryAccessor, memoryAccessor2) -> (serverLevel, mob, l) -> {
								LivingEntity livingEntity = instance.get(memoryAccessor);
								if (mob.canAttack(livingEntity)
									&& (!bl || !isTiredOfTryingToReachTarget(mob, instance.tryGet(memoryAccessor2)))
									&& livingEntity.isAlive()
									&& livingEntity.level() == mob.level()
									&& !stopAttackCondition.test(serverLevel, livingEntity)) {
									return true;
								} else {
									targetErasedCallback.accept(serverLevel, (E)mob, livingEntity);
									memoryAccessor.erase();
									return true;
								}
							}
					)
		);
	}

	private static boolean isTiredOfTryingToReachTarget(LivingEntity livingEntity, Optional<Long> optional) {
		return optional.isPresent() && livingEntity.level().getGameTime() - (Long)optional.get() > 200L;
	}

	@FunctionalInterface
	public interface StopAttackCondition {
		boolean test(ServerLevel serverLevel, LivingEntity livingEntity);
	}

	@FunctionalInterface
	public interface TargetErasedCallback<E> {
		void accept(ServerLevel serverLevel, E object, LivingEntity livingEntity);
	}
}
