/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StopAttackingIfTargetInvalid {
    private static final int TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE = 200;

    public static <E extends Mob> BehaviorControl<E> create(BiConsumer<E, LivingEntity> biConsumer) {
        return StopAttackingIfTargetInvalid.create(livingEntity -> false, biConsumer, true);
    }

    public static <E extends Mob> BehaviorControl<E> create(Predicate<LivingEntity> predicate) {
        return StopAttackingIfTargetInvalid.create(predicate, (mob, livingEntity) -> {}, true);
    }

    public static <E extends Mob> BehaviorControl<E> create() {
        return StopAttackingIfTargetInvalid.create(livingEntity -> false, (mob, livingEntity) -> {}, true);
    }

    public static <E extends Mob> BehaviorControl<E> create(Predicate<LivingEntity> predicate, BiConsumer<E, LivingEntity> biConsumer, boolean bl) {
        return BehaviorBuilder.create((BehaviorBuilder.Instance<E> instance) -> instance.group(instance.present(MemoryModuleType.ATTACK_TARGET), instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply(instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, mob, l) -> {
            LivingEntity livingEntity = (LivingEntity)instance.get(memoryAccessor);
            if (!mob.canAttack(livingEntity) || bl && StopAttackingIfTargetInvalid.isTiredOfTryingToReachTarget(mob, instance.tryGet(memoryAccessor2)) || !livingEntity.isAlive() || livingEntity.level != mob.level || predicate.test(livingEntity)) {
                biConsumer.accept(mob, livingEntity);
                memoryAccessor.erase();
                return true;
            }
            return true;
        }));
    }

    private static boolean isTiredOfTryingToReachTarget(LivingEntity livingEntity, Optional<Long> optional) {
        return optional.isPresent() && livingEntity.level.getGameTime() - optional.get() > 200L;
    }
}

