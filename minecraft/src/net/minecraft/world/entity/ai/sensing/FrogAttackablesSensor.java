package net.minecraft.world.entity.ai.sensing;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.frog.Frog;

public class FrogAttackablesSensor extends NearestVisibleLivingEntitySensor {
	public static final float TARGET_DETECTION_DISTANCE = 10.0F;

	@Override
	protected boolean isMatchingEntity(LivingEntity livingEntity, LivingEntity livingEntity2) {
		return !livingEntity.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN)
				&& Sensor.isEntityAttackable(livingEntity, livingEntity2)
				&& Frog.canEat(livingEntity2)
			? livingEntity2.closerThan(livingEntity, 10.0)
			: false;
	}

	@Override
	protected MemoryModuleType<LivingEntity> getMemory() {
		return MemoryModuleType.NEAREST_ATTACKABLE;
	}
}
