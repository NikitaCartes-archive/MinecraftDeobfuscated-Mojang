/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InteractWith<E extends LivingEntity, T extends LivingEntity>
extends Behavior<E> {
    private final int maxDist;
    private final float speedModifier;
    private final EntityType<? extends T> type;
    private final int interactionRangeSqr;
    private final Predicate<T> targetFilter;
    private final Predicate<E> selfFilter;
    private final MemoryModuleType<T> memory;

    public InteractWith(EntityType<? extends T> entityType, int i, Predicate<E> predicate, Predicate<T> predicate2, MemoryModuleType<T> memoryModuleType, float f, int j) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
        this.type = entityType;
        this.speedModifier = f;
        this.interactionRangeSqr = i * i;
        this.maxDist = j;
        this.targetFilter = predicate2;
        this.selfFilter = predicate;
        this.memory = memoryModuleType;
    }

    public static <T extends LivingEntity> InteractWith<LivingEntity, T> of(EntityType<? extends T> entityType, int i, MemoryModuleType<T> memoryModuleType, float f, int j) {
        return new InteractWith<LivingEntity, LivingEntity>(entityType, i, livingEntity -> true, livingEntity -> true, memoryModuleType, f, j);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, E livingEntity) {
        return this.selfFilter.test(livingEntity) && this.seesAtLeastOneValidTarget(livingEntity);
    }

    private boolean seesAtLeastOneValidTarget(E livingEntity) {
        List<LivingEntity> list = ((LivingEntity)livingEntity).getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).get();
        return list.stream().anyMatch(this::isTargetValid);
    }

    private boolean isTargetValid(LivingEntity livingEntity) {
        return this.type.equals(livingEntity.getType()) && this.targetFilter.test(livingEntity);
    }

    @Override
    protected void start(ServerLevel serverLevel, E livingEntity, long l) {
        Brain<?> brain = ((LivingEntity)livingEntity).getBrain();
        brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).ifPresent(list -> list.stream().filter(livingEntity -> this.type.equals(livingEntity.getType())).map(livingEntity -> livingEntity).filter(livingEntity2 -> livingEntity2.distanceToSqr((Entity)livingEntity) <= (double)this.interactionRangeSqr).filter(this.targetFilter).findFirst().ifPresent(livingEntity -> {
            brain.setMemory(this.memory, livingEntity);
            brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker((Entity)livingEntity, true));
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker((Entity)livingEntity, false), this.speedModifier, this.maxDist));
        }));
    }
}

