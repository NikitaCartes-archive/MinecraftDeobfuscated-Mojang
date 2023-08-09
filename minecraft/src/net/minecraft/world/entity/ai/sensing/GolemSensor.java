package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class GolemSensor extends Sensor<LivingEntity> {
	private static final int GOLEM_SCAN_RATE = 200;
	private static final int MEMORY_TIME_TO_LIVE = 599;

	public GolemSensor() {
		this(200);
	}

	public GolemSensor(int i) {
		super(i);
	}

	@Override
	protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
		checkForNearbyGolem(livingEntity);
	}

	@Override
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.of(MemoryModuleType.NEAREST_LIVING_ENTITIES);
	}

	public static void checkForNearbyGolem(LivingEntity livingEntity) {
		Optional<List<LivingEntity>> optional = livingEntity.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
		if (!optional.isEmpty()) {
			boolean bl = ((List)optional.get()).stream().anyMatch(livingEntityx -> livingEntityx.getType().equals(EntityType.IRON_GOLEM));
			if (bl) {
				golemDetected(livingEntity);
			}
		}
	}

	public static void golemDetected(LivingEntity livingEntity) {
		livingEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.GOLEM_DETECTED_RECENTLY, true, 599L);
	}
}
