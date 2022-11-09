/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster.piglin;

import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;

public class StopAdmiringIfItemTooFarAway<E extends Piglin> {
    public static BehaviorControl<LivingEntity> create(int i) {
        return BehaviorBuilder.create(instance -> instance.group(instance.present(MemoryModuleType.ADMIRING_ITEM), instance.registered(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)).apply(instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, livingEntity, l) -> {
            if (!livingEntity.getOffhandItem().isEmpty()) {
                return false;
            }
            Optional optional = instance.tryGet(memoryAccessor2);
            if (optional.isPresent() && ((ItemEntity)optional.get()).closerThan(livingEntity, i)) {
                return false;
            }
            memoryAccessor.erase();
            return true;
        }));
    }
}

