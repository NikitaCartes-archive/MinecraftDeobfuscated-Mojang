package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.breeze.Breeze;

public class BreezeAttackEntitySensor extends NearestLivingEntitySensor<Breeze> {
	public static final int BREEZE_SENSOR_RADIUS = 24;

	@Override
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.copyOf(Iterables.concat(super.requires(), List.of(MemoryModuleType.NEAREST_ATTACKABLE)));
	}

	protected void doTick(ServerLevel serverLevel, Breeze breeze) {
		super.doTick(serverLevel, breeze);
		breeze.getBrain()
			.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES)
			.stream()
			.flatMap(Collection::stream)
			.filter(livingEntity -> Sensor.isEntityAttackable(breeze, livingEntity))
			.findFirst()
			.ifPresentOrElse(
				livingEntity -> breeze.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, livingEntity),
				() -> breeze.getBrain().eraseMemory(MemoryModuleType.NEAREST_ATTACKABLE)
			);
	}

	@Override
	protected int radiusXZ() {
		return 24;
	}

	@Override
	protected int radiusY() {
		return 24;
	}
}
