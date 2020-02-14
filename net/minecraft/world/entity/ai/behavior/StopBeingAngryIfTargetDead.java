/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StopBeingAngryIfTargetDead<E extends Mob>
extends Behavior<E> {
    public StopBeingAngryIfTargetDead() {
        super(ImmutableMap.of(MemoryModuleType.ANGRY_AT, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected void start(ServerLevel serverLevel, E mob, long l) {
        if (this.isCurrentTargetDeadOrRemoved(mob)) {
            ((LivingEntity)mob).getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
        }
    }

    private boolean isCurrentTargetDeadOrRemoved(E mob) {
        Optional<LivingEntity> optional = BehaviorUtils.getLivingEntityFromUUIDMemory(mob, MemoryModuleType.ANGRY_AT);
        return !optional.isPresent() || !optional.get().isAlive();
    }
}

