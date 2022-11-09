package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InteractWith {
	public static <T extends LivingEntity> BehaviorControl<LivingEntity> of(
		EntityType<? extends T> entityType, int i, MemoryModuleType<T> memoryModuleType, float f, int j
	) {
		return of(entityType, i, livingEntity -> true, livingEntity -> true, memoryModuleType, f, j);
	}

	public static <E extends LivingEntity, T extends LivingEntity> BehaviorControl<E> of(
		EntityType<? extends T> entityType, int i, Predicate<E> predicate, Predicate<T> predicate2, MemoryModuleType<T> memoryModuleType, float f, int j
	) {
		int k = i * i;
		Predicate<LivingEntity> predicate3 = livingEntity -> entityType.equals(livingEntity.getType()) && predicate2.test(livingEntity);
		return BehaviorBuilder.create(
			instance -> instance.group(
						instance.registered(memoryModuleType),
						instance.registered(MemoryModuleType.LOOK_TARGET),
						instance.absent(MemoryModuleType.WALK_TARGET),
						instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
					)
					.apply(
						instance,
						(memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4) -> (serverLevel, livingEntity, l) -> {
								NearestVisibleLivingEntities nearestVisibleLivingEntities = instance.get(memoryAccessor4);
								if (predicate.test(livingEntity) && nearestVisibleLivingEntities.contains(predicate3)) {
									Optional<LivingEntity> optional = nearestVisibleLivingEntities.findClosest(
										livingEntity2 -> livingEntity2.distanceToSqr(livingEntity) <= (double)k && predicate3.test(livingEntity2)
									);
									optional.ifPresent(livingEntityx -> {
										memoryAccessor.set(livingEntityx);
										memoryAccessor2.set(new EntityTracker(livingEntityx, true));
										memoryAccessor3.set(new WalkTarget(new EntityTracker(livingEntityx, false), f, j));
									});
									return true;
								} else {
									return false;
								}
							}
					)
		);
	}
}
