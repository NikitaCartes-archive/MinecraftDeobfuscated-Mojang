package net.minecraft.world.entity.ai.sensing;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class MobSensor<T extends LivingEntity> extends Sensor<T> {
	private final BiPredicate<T, LivingEntity> mobTest;
	private final Predicate<T> readyTest;
	private final MemoryModuleType<Boolean> toSet;
	private final int memoryTimeToLive;

	public MobSensor(int i, BiPredicate<T, LivingEntity> biPredicate, Predicate<T> predicate, MemoryModuleType<Boolean> memoryModuleType, int j) {
		super(i);
		this.mobTest = biPredicate;
		this.readyTest = predicate;
		this.toSet = memoryModuleType;
		this.memoryTimeToLive = j;
	}

	@Override
	protected void doTick(ServerLevel serverLevel, T livingEntity) {
		if (!this.readyTest.test(livingEntity)) {
			this.clearMemory(livingEntity);
		} else {
			this.checkForMobsNearby(livingEntity);
		}
	}

	@Override
	public Set<MemoryModuleType<?>> requires() {
		return Set.of(MemoryModuleType.NEAREST_LIVING_ENTITIES);
	}

	public void checkForMobsNearby(T livingEntity) {
		Optional<List<LivingEntity>> optional = livingEntity.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
		if (!optional.isEmpty()) {
			boolean bl = ((List)optional.get()).stream().anyMatch(livingEntity2 -> this.mobTest.test(livingEntity, livingEntity2));
			if (bl) {
				this.mobDetected(livingEntity);
			}
		}
	}

	public void mobDetected(T livingEntity) {
		livingEntity.getBrain().setMemoryWithExpiry(this.toSet, true, (long)this.memoryTimeToLive);
	}

	public void clearMemory(T livingEntity) {
		livingEntity.getBrain().eraseMemory(this.toSet);
	}
}
