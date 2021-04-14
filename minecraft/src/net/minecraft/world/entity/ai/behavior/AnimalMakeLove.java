package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Animal;

public class AnimalMakeLove extends Behavior<Animal> {
	private static final int BREED_RANGE = 3;
	private static final int MIN_DURATION = 60;
	private static final int MAX_DURATION = 110;
	private final EntityType<? extends Animal> partnerType;
	private final float speedModifier;
	private long spawnChildAtTime;

	public AnimalMakeLove(EntityType<? extends Animal> entityType, float f) {
		super(
			ImmutableMap.of(
				MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.BREED_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED
			),
			110
		);
		this.partnerType = entityType;
		this.speedModifier = f;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Animal animal) {
		return animal.isInLove() && this.findValidBreedPartner(animal).isPresent();
	}

	protected void start(ServerLevel serverLevel, Animal animal, long l) {
		Animal animal2 = (Animal)this.findValidBreedPartner(animal).get();
		animal.getBrain().setMemory(MemoryModuleType.BREED_TARGET, animal2);
		animal2.getBrain().setMemory(MemoryModuleType.BREED_TARGET, animal);
		BehaviorUtils.lockGazeAndWalkToEachOther(animal, animal2, this.speedModifier);
		int i = 60 + animal.getRandom().nextInt(50);
		this.spawnChildAtTime = l + (long)i;
	}

	protected boolean canStillUse(ServerLevel serverLevel, Animal animal, long l) {
		if (!this.hasBreedTargetOfRightType(animal)) {
			return false;
		} else {
			Animal animal2 = this.getBreedTarget(animal);
			return animal2.isAlive() && animal.canMate(animal2) && BehaviorUtils.entityIsVisible(animal.getBrain(), animal2) && l <= this.spawnChildAtTime;
		}
	}

	protected void tick(ServerLevel serverLevel, Animal animal, long l) {
		Animal animal2 = this.getBreedTarget(animal);
		BehaviorUtils.lockGazeAndWalkToEachOther(animal, animal2, this.speedModifier);
		if (animal.closerThan(animal2, 3.0)) {
			if (l >= this.spawnChildAtTime) {
				animal.spawnChildFromBreeding(serverLevel, animal2);
				animal.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
				animal2.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
			}
		}
	}

	protected void stop(ServerLevel serverLevel, Animal animal, long l) {
		animal.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
		animal.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
		animal.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
		this.spawnChildAtTime = 0L;
	}

	private Animal getBreedTarget(Animal animal) {
		return (Animal)animal.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
	}

	private boolean hasBreedTargetOfRightType(Animal animal) {
		Brain<?> brain = animal.getBrain();
		return brain.hasMemoryValue(MemoryModuleType.BREED_TARGET)
			&& ((AgeableMob)brain.getMemory(MemoryModuleType.BREED_TARGET).get()).getType() == this.partnerType;
	}

	private Optional<? extends Animal> findValidBreedPartner(Animal animal) {
		return ((List)animal.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get())
			.stream()
			.filter(livingEntity -> livingEntity.getType() == this.partnerType)
			.map(livingEntity -> (Animal)livingEntity)
			.filter(animal::canMate)
			.findFirst();
	}
}
