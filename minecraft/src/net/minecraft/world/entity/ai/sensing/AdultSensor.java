package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class AdultSensor extends Sensor<AgeableMob> {
	@Override
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.VISIBLE_LIVING_ENTITIES);
	}

	protected void doTick(ServerLevel serverLevel, AgeableMob ageableMob) {
		ageableMob.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).ifPresent(list -> this.setNearestVisibleAdult(ageableMob, list));
	}

	private void setNearestVisibleAdult(AgeableMob ageableMob, List<LivingEntity> list) {
		Optional<AgeableMob> optional = list.stream()
			.filter(livingEntity -> livingEntity.getType() == ageableMob.getType())
			.map(livingEntity -> (AgeableMob)livingEntity)
			.filter(ageableMobx -> !ageableMobx.isBaby())
			.findFirst();
		ageableMob.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT, optional);
	}
}
