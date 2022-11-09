/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.schedule.Activity;

public class WakeUp {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(instance -> instance.point((serverLevel, livingEntity, l) -> {
            if (livingEntity.getBrain().isActive(Activity.REST) || !livingEntity.isSleeping()) {
                return false;
            }
            livingEntity.stopSleeping();
            return true;
        }));
    }
}

