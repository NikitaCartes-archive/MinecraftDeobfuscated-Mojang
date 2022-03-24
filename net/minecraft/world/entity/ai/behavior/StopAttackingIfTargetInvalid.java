/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StopAttackingIfTargetInvalid<E extends Mob>
extends Behavior<E> {
    private static final int TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE = 200;
    private final Predicate<LivingEntity> stopAttackingWhen;
    private final BiConsumer<E, LivingEntity> onTargetErased;
    private final boolean canGrowTiredOfTryingToReachTarget;

    public StopAttackingIfTargetInvalid(Predicate<LivingEntity> predicate, BiConsumer<E, LivingEntity> biConsumer, boolean bl) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED));
        this.stopAttackingWhen = predicate;
        this.onTargetErased = biConsumer;
        this.canGrowTiredOfTryingToReachTarget = bl;
    }

    public StopAttackingIfTargetInvalid(Predicate<LivingEntity> predicate, BiConsumer<E, LivingEntity> biConsumer) {
        this(predicate, biConsumer, true);
    }

    public StopAttackingIfTargetInvalid(Predicate<LivingEntity> predicate) {
        this(predicate, (mob, livingEntity) -> {});
    }

    public StopAttackingIfTargetInvalid(BiConsumer<E, LivingEntity> biConsumer) {
        this((LivingEntity livingEntity) -> false, biConsumer);
    }

    public StopAttackingIfTargetInvalid() {
        this((LivingEntity livingEntity) -> false, (mob, livingEntity) -> {});
    }

    @Override
    protected void start(ServerLevel serverLevel, E mob, long l) {
        LivingEntity livingEntity = this.getAttackTarget(mob);
        if (!((LivingEntity)mob).canAttack(livingEntity)) {
            this.clearAttackTarget(mob);
            return;
        }
        if (this.canGrowTiredOfTryingToReachTarget && StopAttackingIfTargetInvalid.isTiredOfTryingToReachTarget(mob)) {
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
        this.onTargetErased.accept(mob, this.getAttackTarget(mob));
        ((LivingEntity)mob).getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
    }
}

