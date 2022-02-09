package net.minecraft.world.entity.ai.sensing;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class AxolotlAttackablesSensor extends NearestVisibleLivingEntitySensor {
	public static final float TARGET_DETECTION_DISTANCE = 8.0F;

	@Override
	protected boolean isMatchingEntity(LivingEntity livingEntity, LivingEntity livingEntity2) {
		return this.isClose(livingEntity, livingEntity2)
			&& livingEntity2.isInWaterOrBubble()
			&& (this.isHostileTarget(livingEntity2) || this.isHuntTarget(livingEntity, livingEntity2))
			&& Sensor.isEntityAttackable(livingEntity, livingEntity2);
	}

	private boolean isHuntTarget(LivingEntity livingEntity, LivingEntity livingEntity2) {
		return !livingEntity.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN) && livingEntity2.getType().is(EntityTypeTags.AXOLOTL_HUNT_TARGETS);
	}

	private boolean isHostileTarget(LivingEntity livingEntity) {
		return livingEntity.getType().is(EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES);
	}

	private boolean isClose(LivingEntity livingEntity, LivingEntity livingEntity2) {
		return livingEntity2.distanceToSqr(livingEntity) <= 64.0;
	}

	@Override
	protected MemoryModuleType<LivingEntity> getMemory() {
		return MemoryModuleType.NEAREST_ATTACKABLE;
	}
}
