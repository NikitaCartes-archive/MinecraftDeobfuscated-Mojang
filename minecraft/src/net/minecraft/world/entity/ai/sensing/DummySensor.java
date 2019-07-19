package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class DummySensor extends Sensor<LivingEntity> {
	@Override
	protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
	}

	@Override
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.of();
	}
}
