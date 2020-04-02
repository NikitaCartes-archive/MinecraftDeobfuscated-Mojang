/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class BackUpIfTooClose<E extends Mob>
extends Behavior<E> {
    private final int tooCloseDistance;
    private final float strafeSpeed;

    public BackUpIfTooClose(int i, float f) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
        this.tooCloseDistance = i;
        this.strafeSpeed = f;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, E mob) {
        return this.isTargetVisible(mob) && this.isTargetTooClose(mob);
    }

    @Override
    protected void start(ServerLevel serverLevel, E mob, long l) {
        ((LivingEntity)mob).getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.getTarget(mob)));
        ((Mob)mob).getMoveControl().strafe(-this.strafeSpeed, 0.0f);
        ((Mob)mob).yRot = Mth.rotateIfNecessary(((Mob)mob).yRot, ((Mob)mob).yHeadRot, 0.0f);
    }

    private boolean isTargetVisible(E mob) {
        return ((LivingEntity)mob).getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).get().contains(this.getTarget(mob));
    }

    private boolean isTargetTooClose(E mob) {
        return this.getTarget(mob).closerThan((Entity)mob, this.tooCloseDistance);
    }

    private LivingEntity getTarget(E mob) {
        return ((LivingEntity)mob).getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }
}

