/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StartAttacking<E extends Mob>
extends Behavior<E> {
    private final Predicate<E> canAttackPredicate;
    private final Function<E, Optional<? extends LivingEntity>> targetFinderFunction;

    public StartAttacking(Predicate<E> predicate, Function<E, Optional<? extends LivingEntity>> function) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED));
        this.canAttackPredicate = predicate;
        this.targetFinderFunction = function;
    }

    public StartAttacking(Function<E, Optional<? extends LivingEntity>> function) {
        this((E mob) -> true, function);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, E mob) {
        if (!this.canAttackPredicate.test(mob)) {
            return false;
        }
        Optional<? extends LivingEntity> optional = this.targetFinderFunction.apply(mob);
        return optional.isPresent() && optional.get().isAlive();
    }

    @Override
    protected void start(ServerLevel serverLevel, E mob, long l) {
        this.targetFinderFunction.apply(mob).ifPresent(livingEntity -> this.setAttackTarget(mob, (LivingEntity)livingEntity));
    }

    private void setAttackTarget(E mob, LivingEntity livingEntity) {
        ((LivingEntity)mob).getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, livingEntity);
        ((LivingEntity)mob).getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
    }
}

