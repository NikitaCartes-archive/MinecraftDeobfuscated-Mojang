/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class SetLookAndInteract {
    public static BehaviorControl<LivingEntity> create(EntityType<?> entityType, int i) {
        int j = i * i;
        return BehaviorBuilder.create(instance -> instance.group(instance.registered(MemoryModuleType.LOOK_TARGET), instance.absent(MemoryModuleType.INTERACTION_TARGET), instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply(instance, (memoryAccessor, memoryAccessor2, memoryAccessor3) -> (serverLevel, livingEntity, l) -> {
            Optional<LivingEntity> optional = ((NearestVisibleLivingEntities)instance.get(memoryAccessor3)).findClosest(livingEntity2 -> livingEntity2.distanceToSqr(livingEntity) <= (double)j && entityType.equals(livingEntity2.getType()));
            if (optional.isEmpty()) {
                return false;
            }
            LivingEntity livingEntity22 = optional.get();
            memoryAccessor2.set(livingEntity22);
            memoryAccessor.set(new EntityTracker(livingEntity22, true));
            return true;
        }));
    }
}

