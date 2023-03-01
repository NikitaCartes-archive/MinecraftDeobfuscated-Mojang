/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;

public class Digging<E extends Warden>
extends Behavior<E> {
    public Digging(int i) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), i);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, E warden, long l) {
        return ((Entity)warden).getRemovalReason() == null;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, E warden) {
        return ((Entity)warden).isOnGround() || ((Entity)warden).isInWater() || ((Entity)warden).isInLava();
    }

    @Override
    protected void start(ServerLevel serverLevel, E warden, long l) {
        if (((Entity)warden).isOnGround()) {
            ((Entity)warden).setPose(Pose.DIGGING);
            ((Entity)warden).playSound(SoundEvents.WARDEN_DIG, 5.0f, 1.0f);
        } else {
            ((Entity)warden).playSound(SoundEvents.WARDEN_AGITATED, 5.0f, 1.0f);
            this.stop(serverLevel, warden, l);
        }
    }

    @Override
    protected void stop(ServerLevel serverLevel, E warden, long l) {
        if (((Entity)warden).getRemovalReason() == null) {
            ((LivingEntity)warden).remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (E)((Warden)livingEntity), l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (E)((Warden)livingEntity), l);
    }
}

