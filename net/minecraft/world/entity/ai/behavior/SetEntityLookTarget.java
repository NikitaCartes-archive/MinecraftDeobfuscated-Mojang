/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityPosWrapper;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class SetEntityLookTarget
extends Behavior<LivingEntity> {
    private final Predicate<LivingEntity> predicate;
    private final float maxDistSqr;

    public SetEntityLookTarget(MobCategory mobCategory, float f) {
        this((LivingEntity livingEntity) -> mobCategory.equals((Object)livingEntity.getType().getCategory()), f);
    }

    public SetEntityLookTarget(EntityType<?> entityType, float f) {
        this((LivingEntity livingEntity) -> entityType.equals(livingEntity.getType()), f);
    }

    public SetEntityLookTarget(float f) {
        this((LivingEntity livingEntity) -> true, f);
    }

    public SetEntityLookTarget(Predicate<LivingEntity> predicate, float f) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
        this.predicate = predicate;
        this.maxDistSqr = f * f;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).get().stream().anyMatch(this.predicate);
    }

    @Override
    protected void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        Brain<?> brain = livingEntity.getBrain();
        brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).ifPresent(list -> list.stream().filter(this.predicate).filter(livingEntity2 -> livingEntity2.distanceToSqr(livingEntity) <= (double)this.maxDistSqr).findFirst().ifPresent(livingEntity -> brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper((Entity)livingEntity))));
    }
}

