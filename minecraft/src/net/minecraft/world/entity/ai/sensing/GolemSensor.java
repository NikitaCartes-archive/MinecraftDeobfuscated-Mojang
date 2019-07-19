package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class GolemSensor extends Sensor<LivingEntity> {
	public GolemSensor() {
		this(200);
	}

	public GolemSensor(int i) {
		super(i);
	}

	@Override
	protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
		checkForNearbyGolem(serverLevel.getGameTime(), livingEntity);
	}

	@Override
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.of(MemoryModuleType.LIVING_ENTITIES);
	}

	public static void checkForNearbyGolem(long l, LivingEntity livingEntity) {
		Brain<?> brain = livingEntity.getBrain();
		Optional<List<LivingEntity>> optional = brain.getMemory(MemoryModuleType.LIVING_ENTITIES);
		if (optional.isPresent()) {
			boolean bl = ((List)optional.get()).stream().anyMatch(livingEntityx -> livingEntityx.getType().equals(EntityType.IRON_GOLEM));
			if (bl) {
				brain.setMemory(MemoryModuleType.GOLEM_LAST_SEEN_TIME, l);
			}
		}
	}
}
