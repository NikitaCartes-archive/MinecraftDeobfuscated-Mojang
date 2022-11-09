/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SocializeAtBell {
    private static final float SPEED_MODIFIER = 0.3f;

    public static OneShot<LivingEntity> create() {
        return BehaviorBuilder.create(instance -> instance.group(instance.registered(MemoryModuleType.WALK_TARGET), instance.registered(MemoryModuleType.LOOK_TARGET), instance.present(MemoryModuleType.MEETING_POINT), instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES), instance.absent(MemoryModuleType.INTERACTION_TARGET)).apply(instance, (memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4, memoryAccessor5) -> (serverLevel, livingEntity3, l) -> {
            GlobalPos globalPos = (GlobalPos)instance.get(memoryAccessor3);
            NearestVisibleLivingEntities nearestVisibleLivingEntities = (NearestVisibleLivingEntities)instance.get(memoryAccessor4);
            if (serverLevel.getRandom().nextInt(100) == 0 && serverLevel.dimension() == globalPos.dimension() && globalPos.pos().closerToCenterThan(livingEntity3.position(), 4.0) && nearestVisibleLivingEntities.contains(livingEntity -> EntityType.VILLAGER.equals(livingEntity.getType()))) {
                nearestVisibleLivingEntities.findClosest(livingEntity2 -> EntityType.VILLAGER.equals(livingEntity2.getType()) && livingEntity2.distanceToSqr(livingEntity3) <= 32.0).ifPresent(livingEntity -> {
                    memoryAccessor5.set(livingEntity);
                    memoryAccessor2.set(new EntityTracker((Entity)livingEntity, true));
                    memoryAccessor.set(new WalkTarget(new EntityTracker((Entity)livingEntity, false), 0.3f, 1));
                });
                return true;
            }
            return false;
        }));
    }
}

