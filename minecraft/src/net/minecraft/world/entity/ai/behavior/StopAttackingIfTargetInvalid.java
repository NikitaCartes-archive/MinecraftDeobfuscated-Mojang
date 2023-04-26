package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StopAttackingIfTargetInvalid {
	private static final int TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE = 200;

	public static <E extends Mob> BehaviorControl<E> create(BiConsumer<E, LivingEntity> biConsumer) {
		return create(livingEntity -> false, biConsumer, true);
	}

	public static <E extends Mob> BehaviorControl<E> create(Predicate<LivingEntity> predicate) {
		return create(predicate, (mob, livingEntity) -> {
		}, true);
	}

	public static <E extends Mob> BehaviorControl<E> create() {
		return create(livingEntity -> false, (mob, livingEntity) -> {
		}, true);
	}

	public static <E extends Mob> BehaviorControl<E> create(Predicate<LivingEntity> predicate, BiConsumer<E, LivingEntity> biConsumer, boolean bl) {
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
									&& !predicate.test(livingEntity)) {
									return true;
								} else {
									biConsumer.accept(mob, livingEntity);
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
}
