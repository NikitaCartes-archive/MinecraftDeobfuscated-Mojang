package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class IsInWaterSensor extends Sensor<LivingEntity> {
	@Override
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.of(MemoryModuleType.IS_IN_WATER);
	}

	@Override
	protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
		if (livingEntity.isInWater()) {
			livingEntity.getBrain().setMemory(MemoryModuleType.IS_IN_WATER, Unit.INSTANCE);
		} else {
			livingEntity.getBrain().eraseMemory(MemoryModuleType.IS_IN_WATER);
		}
	}
}
