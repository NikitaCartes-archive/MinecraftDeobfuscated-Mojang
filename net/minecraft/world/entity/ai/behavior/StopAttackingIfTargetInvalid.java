/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StopAttackingIfTargetInvalid<E extends Mob>
extends Behavior<E> {
    private static final int TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE = 200;
    private final Predicate<LivingEntity> stopAttackingWhen;
    private final Consumer<E> onTargetErased;

    public StopAttackingIfTargetInvalid(Predicate<LivingEntity> predicate, Consumer<E> consumer) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED));
        this.stopAttackingWhen = predicate;
        this.onTargetErased = consumer;
    }

    public StopAttackingIfTargetInvalid(Predicate<LivingEntity> predicate) {
        this(predicate, mob -> {});
    }

    public StopAttackingIfTargetInvalid(Consumer<E> consumer) {
        this((LivingEntity livingEntity) -> false, consumer);
    }

    public StopAttackingIfTargetInvalid() {
        this((LivingEntity livingEntity) -> false, mob -> {});
    }

    @Override
    protected void start(ServerLevel serverLevel, E mob, long l) {
        LivingEntity livingEntity = this.getAttackTarget(mob);
        if (!livingEntity.canBeSeenAsEnemy()) {
            this.clearAttackTarget(mob);
            return;
        }
        if (StopAttackingIfTargetInvalid.isTiredOfTryingToReachTarget(mob)) {
            this.clearAttackTarget(mob);
            return;
        }
        if (this.isCurrentTargetDeadOrRemoved(mob)) {
            this.clearAttackTarget(mob);
            return;
        }
        if (this.isCurrentTargetInDifferentLevel(mob)) {
            this.clearAttackTarget(mob);
            return;
        }
        if (!EntitySelector.ATTACK_ALLOWED.test(this.getAttackTarget(mob))) {
            this.clearAttackTarget(mob);
            return;
        }
        if (this.stopAttackingWhen.test(this.getAttackTarget(mob))) {
            this.clearAttackTarget(mob);
            return;
        }
    }

    private boolean isCurrentTargetInDifferentLevel(E mob) {
        return this.getAttackTarget(mob).level != ((Mob)mob).level;
    }

    private LivingEntity getAttackTarget(E mob) {
        return ((LivingEntity)mob).getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }

    private static <E extends LivingEntity> boolean isTiredOfTryingToReachTarget(E livingEntity) {
        Optional<Long> optional = livingEntity.getBrain().getMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        return optional.isPresent() && livingEntity.level.getGameTime() - optional.get() > 200L;
    }

    private boolean isCurrentTargetDeadOrRemoved(E mob) {
        Optional<LivingEntity> optional = ((LivingEntity)mob).getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        return optional.isPresent() && !optional.get().isAlive();
    }

    protected void clearAttackTarget(E mob) {
        this.onTargetErased.accept(mob);
        ((LivingEntity)mob).getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
    }
}

