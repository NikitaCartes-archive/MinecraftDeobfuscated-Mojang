/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StartAttacking {
    public static <E extends Mob> BehaviorControl<E> create(Function<E, Optional<? extends LivingEntity>> function) {
        return StartAttacking.create(mob -> true, function);
    }

    public static <E extends Mob> BehaviorControl<E> create(Predicate<E> predicate, Function<E, Optional<? extends LivingEntity>> function) {
        return BehaviorBuilder.create((BehaviorBuilder.Instance<E> instance) -> instance.group(instance.absent(MemoryModuleType.ATTACK_TARGET), instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply(instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, mob, l) -> {
            if (!predicate.test(mob)) {
                return false;
            }
            Optional optional = (Optional)function.apply(mob);
            if (optional.isEmpty()) {
                return false;
            }
            LivingEntity livingEntity = (LivingEntity)optional.get();
            if (!mob.canAttack(livingEntity)) {
                return false;
            }
            memoryAccessor.set(livingEntity);
            memoryAccessor2.erase();
            return true;
        }));
    }
}

