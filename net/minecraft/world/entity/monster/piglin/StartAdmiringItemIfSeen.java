/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;

public class StartAdmiringItemIfSeen<E extends Piglin>
extends Behavior<E> {
    private final int admireDuration;

    public StartAdmiringItemIfSeen(int i) {
        super(ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.VALUE_PRESENT, MemoryModuleType.ADMIRING_ITEM, MemoryStatus.VALUE_ABSENT, MemoryModuleType.ADMIRING_DISABLED, MemoryStatus.VALUE_ABSENT, MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, MemoryStatus.VALUE_ABSENT));
        this.admireDuration = i;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, E piglin) {
        ItemEntity itemEntity = ((Piglin)piglin).getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).get();
        return PiglinAi.isLovedItem(itemEntity.getItem());
    }

    @Override
    protected void start(ServerLevel serverLevel, E piglin, long l) {
        ((Piglin)piglin).getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, this.admireDuration);
    }
}

