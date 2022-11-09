package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SocializeAtBell {
	private static final float SPEED_MODIFIER = 0.3F;

	public static OneShot<LivingEntity> create() {
		return BehaviorBuilder.create(
			instance -> instance.group(
						instance.registered(MemoryModuleType.WALK_TARGET),
						instance.registered(MemoryModuleType.LOOK_TARGET),
						instance.present(MemoryModuleType.MEETING_POINT),
						instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES),
						instance.absent(MemoryModuleType.INTERACTION_TARGET)
					)
					.apply(
						instance,
						(memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4, memoryAccessor5) -> (serverLevel, livingEntity, l) -> {
								GlobalPos globalPos = instance.get(memoryAccessor3);
								NearestVisibleLivingEntities nearestVisibleLivingEntities = instance.get(memoryAccessor4);
								if (serverLevel.getRandom().nextInt(100) == 0
									&& serverLevel.dimension() == globalPos.dimension()
									&& globalPos.pos().closerToCenterThan(livingEntity.position(), 4.0)
									&& nearestVisibleLivingEntities.contains(livingEntityx -> EntityType.VILLAGER.equals(livingEntityx.getType()))) {
									nearestVisibleLivingEntities.findClosest(
											livingEntity2 -> EntityType.VILLAGER.equals(livingEntity2.getType()) && livingEntity2.distanceToSqr(livingEntity) <= 32.0
										)
										.ifPresent(livingEntityx -> {
											memoryAccessor5.set(livingEntityx);
											memoryAccessor2.set(new EntityTracker(livingEntityx, true));
											memoryAccessor.set(new WalkTarget(new EntityTracker(livingEntityx, false), 0.3F, 1));
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
