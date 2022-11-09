package net.minecraft.world.entity.ai.behavior;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class BackUpIfTooClose {
	public static OneShot<Mob> create(int i, float f) {
		return BehaviorBuilder.create(
			instance -> instance.group(
						instance.absent(MemoryModuleType.WALK_TARGET),
						instance.registered(MemoryModuleType.LOOK_TARGET),
						instance.present(MemoryModuleType.ATTACK_TARGET),
						instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
					)
					.apply(instance, (memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4) -> (serverLevel, mob, l) -> {
							LivingEntity livingEntity = instance.get(memoryAccessor3);
							if (livingEntity.closerThan(mob, (double)i) && instance.<NearestVisibleLivingEntities>get(memoryAccessor4).contains(livingEntity)) {
								memoryAccessor2.set(new EntityTracker(livingEntity, true));
								mob.getMoveControl().strafe(-f, 0.0F);
								mob.setYRot(Mth.rotateIfNecessary(mob.getYRot(), mob.yHeadRot, 0.0F));
								return true;
							} else {
								return false;
							}
						})
		);
	}
}
