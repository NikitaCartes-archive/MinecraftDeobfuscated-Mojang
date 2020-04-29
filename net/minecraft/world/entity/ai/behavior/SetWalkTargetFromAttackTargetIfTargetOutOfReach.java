/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SetWalkTargetFromAttackTargetIfTargetOutOfReach
extends Behavior<Mob> {
    private final float speedModifier;

    public SetWalkTargetFromAttackTargetIfTargetOutOfReach(float f) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryStatus.REGISTERED));
        this.speedModifier = f;
    }

    @Override
    protected void start(ServerLevel serverLevel, Mob mob, long l) {
        LivingEntity livingEntity = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        if (BehaviorUtils.canSee(mob, livingEntity) && BehaviorUtils.isWithinAttackRange(mob, livingEntity, 1)) {
            this.clearWalkTarget(mob);
        } else {
            this.setWalkAndLookTarget(mob, livingEntity);
        }
    }

    private void setWalkAndLookTarget(LivingEntity livingEntity, LivingEntity livingEntity2) {
        Brain<?> brain = livingEntity.getBrain();
        brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(livingEntity2, true));
        WalkTarget walkTarget = new WalkTarget(new EntityTracker(livingEntity2, false), this.speedModifier, 0);
        brain.setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
    }

    private void clearWalkTarget(LivingEntity livingEntity) {
        livingEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }
}

