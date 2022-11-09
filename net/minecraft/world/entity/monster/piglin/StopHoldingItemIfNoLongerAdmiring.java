/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster.piglin;

import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.Items;

public class StopHoldingItemIfNoLongerAdmiring {
    public static BehaviorControl<Piglin> create() {
        return BehaviorBuilder.create(instance -> instance.group(instance.absent(MemoryModuleType.ADMIRING_ITEM)).apply(instance, memoryAccessor -> (serverLevel, piglin, l) -> {
            if (piglin.getOffhandItem().isEmpty() || piglin.getOffhandItem().is(Items.SHIELD)) {
                return false;
            }
            PiglinAi.stopHoldingOffHandItem(piglin, true);
            return true;
        }));
    }
}

