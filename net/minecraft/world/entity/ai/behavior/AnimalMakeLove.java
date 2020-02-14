/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Animal;

public class AnimalMakeLove
extends Behavior<Animal> {
    private final EntityType<? extends Animal> partnerType;
    private long spawnChildAtTime;

    public AnimalMakeLove(EntityType<? extends Animal> entityType) {
        super(ImmutableMap.of(MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT, MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED), 325);
        this.partnerType = entityType;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Animal animal) {
        return animal.isInLove() && this.findValidBreedPartner(animal).isPresent();
    }

    @Override
    protected void start(ServerLevel serverLevel, Animal animal, long l) {
        Animal animal2 = this.findValidBreedPartner(animal).get();
        animal.getBrain().setMemory(MemoryModuleType.BREED_TARGET, animal2);
        animal2.getBrain().setMemory(MemoryModuleType.BREED_TARGET, animal);
        BehaviorUtils.lockGazeAndWalkToEachOther(animal, animal2);
        int i = 275 + animal.getRandom().nextInt(50);
        this.spawnChildAtTime = l + (long)i;
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Animal animal, long l) {
        if (!this.hasBreedTargetOfRightType(animal)) {
            return false;
        }
        Animal animal2 = this.getBreedTarget(animal);
        return animal2.isAlive() && animal.canMate(animal2) && BehaviorUtils.entityIsVisible(animal.getBrain(), animal2) && l <= this.spawnChildAtTime;
    }

    @Override
    protected void tick(ServerLevel serverLevel, Animal animal, long l) {
        Animal animal2 = this.getBreedTarget(animal);
        BehaviorUtils.lockGazeAndWalkToEachOther(animal, animal2);
        if (!animal.closerThan(animal2, 3.0)) {
            return;
        }
        if (l >= this.spawnChildAtTime) {
            animal.spawnChildFromBreeding(serverLevel, animal2);
            animal.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
            animal2.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
        }
    }

    @Override
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
        Brain<AgableMob> brain = animal.getBrain();
        return brain.hasMemoryValue(MemoryModuleType.BREED_TARGET) && brain.getMemory(MemoryModuleType.BREED_TARGET).get().getType() == this.partnerType;
    }

    private Optional<? extends Animal> findValidBreedPartner(Animal animal) {
        return animal.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).get().stream().filter(livingEntity -> livingEntity.getType() == this.partnerType).map(livingEntity -> (Animal)livingEntity).filter(animal::canMate).findFirst();
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Animal)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (Animal)livingEntity, l);
    }
}

