/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class LookAtTargetSink
extends Behavior<Mob> {
    public LookAtTargetSink(int i, int j) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_PRESENT), i, j);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Mob mob, long l) {
        return mob.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).filter(positionWrapper -> positionWrapper.isVisible(mob)).isPresent();
    }

    @Override
    protected void stop(ServerLevel serverLevel, Mob mob, long l) {
        mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    @Override
    protected void tick(ServerLevel serverLevel, Mob mob, long l) {
        mob.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).ifPresent(positionWrapper -> mob.getLookControl().setLookAt(positionWrapper.getLookAtPos()));
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (Mob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Mob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (Mob)livingEntity, l);
    }
}

