/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

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
import net.minecraft.world.entity.monster.warden.WardenAi;

public class Sniffing<E extends Warden>
extends Behavior<E> {
    public Sniffing(int i) {
        super(ImmutableMap.of(MemoryModuleType.IS_SNIFFING, MemoryStatus.VALUE_PRESENT, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.NEAREST_ATTACKABLE, MemoryStatus.REGISTERED), i);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, E warden, long l) {
        return true;
    }

    @Override
    protected void start(ServerLevel serverLevel, E warden, long l) {
        ((Entity)warden).playSound(SoundEvents.WARDEN_SNIFF, 5.0f, 1.0f);
    }

    @Override
    protected void stop(ServerLevel serverLevel, E warden, long l) {
        if (((Entity)warden).hasPose(Pose.SNIFFING)) {
            ((Entity)warden).setPose(Pose.STANDING);
        }
        ((Warden)warden).getBrain().eraseMemory(MemoryModuleType.IS_SNIFFING);
        ((Warden)warden).getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE).filter(Warden::canTargetEntity).ifPresent(livingEntity -> {
            if (warden.closerThan((Entity)livingEntity, 6.0)) {
                warden.increaseAngerAt((Entity)livingEntity);
            }
            WardenAi.setDisturbanceLocation(warden, livingEntity.blockPosition());
        });
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (E)((Warden)livingEntity), l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (E)((Warden)livingEntity), l);
    }
}

