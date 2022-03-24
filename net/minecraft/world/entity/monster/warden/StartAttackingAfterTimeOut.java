/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster.warden;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;

public class StartAttackingAfterTimeOut
extends StartAttacking<Warden> {
    public StartAttackingAfterTimeOut(Predicate<Warden> predicate, Function<Warden, Optional<? extends LivingEntity>> function, int i) {
        super(predicate, function, i);
    }

    @Override
    protected void start(ServerLevel serverLevel, Warden warden, long l) {
        BehaviorUtils.lookAtEntity(warden, warden.getBrain().getMemory(MemoryModuleType.ROAR_TARGET).get());
    }

    @Override
    protected void stop(ServerLevel serverLevel, Warden warden, long l) {
        this.startAttacking(serverLevel, warden, l);
    }

    private void startAttacking(ServerLevel serverLevel, Warden warden, long l) {
        super.start(serverLevel, warden, l);
        warden.getBrain().eraseMemory(MemoryModuleType.ROAR_TARGET);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Warden warden, long l) {
        Optional<LivingEntity> optional = warden.getBrain().getMemory(MemoryModuleType.ROAR_TARGET);
        return optional.filter(EntitySelector.NO_CREATIVE_OR_SPECTATOR).isPresent();
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (Warden)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Warden)livingEntity, l);
    }
}

