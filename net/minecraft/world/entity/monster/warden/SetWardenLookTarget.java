/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;

public class SetWardenLookTarget
extends Behavior<Warden> {
    public SetWardenLookTarget() {
        super(ImmutableMap.of(MemoryModuleType.DISTURBANCE_LOCATION, MemoryStatus.VALUE_PRESENT, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected void start(ServerLevel serverLevel, Warden warden, long l) {
        warden.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(warden.getBrain().getMemory(MemoryModuleType.DISTURBANCE_LOCATION).get()));
    }
}

