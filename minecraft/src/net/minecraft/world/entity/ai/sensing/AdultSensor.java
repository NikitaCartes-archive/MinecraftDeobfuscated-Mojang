package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class AdultSensor extends Sensor<AgableMob> {
	@Override
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.VISIBLE_LIVING_ENTITIES);
	}

	protected void doTick(ServerLevel serverLevel, AgableMob agableMob) {
		agableMob.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).ifPresent(list -> this.setNearestVisibleAdult(agableMob, list));
	}

	private void setNearestVisibleAdult(AgableMob agableMob, List<LivingEntity> list) {
		Optional<AgableMob> optional = list.stream()
			.filter(livingEntity -> livingEntity.getType() == agableMob.getType())
			.map(livingEntity -> (AgableMob)livingEntity)
			.filter(agableMobx -> !agableMobx.isBaby())
			.findFirst();
		agableMob.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT, optional);
	}
}
