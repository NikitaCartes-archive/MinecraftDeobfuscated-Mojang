/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;

public class StopAdmiringIfItemTooFarAway<E extends Piglin>
extends Behavior<E> {
    private final int maxDistanceToItem;

    public StopAdmiringIfItemTooFarAway(int i) {
        super(ImmutableMap.of(MemoryModuleType.ADMIRING_ITEM, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.REGISTERED));
        this.maxDistanceToItem = i;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, E piglin) {
        if (!((LivingEntity)piglin).getOffhandItem().isEmpty()) {
            return false;
        }
        Optional<ItemEntity> optional = ((Piglin)piglin).getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
        if (!optional.isPresent()) {
            return true;
        }
        return !optional.get().closerThan((Entity)piglin, this.maxDistanceToItem);
    }

    @Override
    protected void start(ServerLevel serverLevel, E piglin, long l) {
        ((Piglin)piglin).getBrain().eraseMemory(MemoryModuleType.ADMIRING_ITEM);
    }
}

