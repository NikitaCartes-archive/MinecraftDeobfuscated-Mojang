/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StayCloseToTarget<E extends LivingEntity>
extends Behavior<E> {
    private final Function<LivingEntity, Optional<PositionTracker>> targetPositionGetter;
    private final int closeEnough;
    private final float speedModifier;

    public StayCloseToTarget(Function<LivingEntity, Optional<PositionTracker>> function, int i, float f) {
        super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.targetPositionGetter = function;
        this.closeEnough = i;
        this.speedModifier = f;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, E livingEntity) {
        Optional<PositionTracker> optional = this.targetPositionGetter.apply((LivingEntity)livingEntity);
        return optional.isPresent() && !((Entity)livingEntity).position().closerThan(optional.get().currentPosition(), this.closeEnough);
    }

    @Override
    protected void start(ServerLevel serverLevel, E livingEntity, long l) {
        BehaviorUtils.setWalkAndLookTargetMemories(livingEntity, this.targetPositionGetter.apply((LivingEntity)livingEntity).get(), this.speedModifier, this.closeEnough / 2);
    }
}

