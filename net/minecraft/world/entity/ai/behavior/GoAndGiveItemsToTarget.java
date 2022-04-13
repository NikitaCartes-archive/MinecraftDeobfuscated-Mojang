/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.allay.AllayAi;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class GoAndGiveItemsToTarget<E extends LivingEntity>
extends Behavior<E> {
    private static final int CLOSE_ENOUGH_DISTANCE_TO_TARGET = 3;
    private static final int ITEM_PICKUP_COOLDOWN_AFTER_THROWING = 100;
    private final Function<LivingEntity, Optional<PositionTracker>> targetPositionGetter;
    private final float speedModifier;

    public GoAndGiveItemsToTarget(Function<LivingEntity, Optional<PositionTracker>> function, float f) {
        super(Map.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryStatus.REGISTERED));
        this.targetPositionGetter = function;
        this.speedModifier = f;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, E livingEntity) {
        return this.canThrowItemToTarget(livingEntity);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, E livingEntity, long l) {
        return this.canThrowItemToTarget(livingEntity);
    }

    @Override
    protected void start(ServerLevel serverLevel, E livingEntity, long l) {
        this.targetPositionGetter.apply((LivingEntity)livingEntity).ifPresent(positionTracker -> BehaviorUtils.setWalkAndLookTargetMemories(livingEntity, positionTracker, this.speedModifier, 3));
    }

    @Override
    protected void tick(ServerLevel serverLevel, E livingEntity, long l) {
        ItemStack itemStack;
        Optional<PositionTracker> optional = this.targetPositionGetter.apply((LivingEntity)livingEntity);
        if (optional.isEmpty()) {
            return;
        }
        PositionTracker positionTracker = optional.get();
        double d = positionTracker.currentPosition().distanceTo(((Entity)livingEntity).getEyePosition());
        if (d < 3.0 && !(itemStack = ((InventoryCarrier)livingEntity).getInventory().removeItem(0, 1)).isEmpty()) {
            BehaviorUtils.throwItem(livingEntity, itemStack, GoAndGiveItemsToTarget.getThrowPosition(positionTracker));
            if (livingEntity instanceof Allay) {
                Allay allay = (Allay)livingEntity;
                AllayAi.getLikedPlayer(allay).ifPresent(serverPlayer -> this.triggerDropItemOnBlock(positionTracker, itemStack, (ServerPlayer)serverPlayer));
            }
            ((LivingEntity)livingEntity).getBrain().setMemory(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, 100);
        }
    }

    private void triggerDropItemOnBlock(PositionTracker positionTracker, ItemStack itemStack, ServerPlayer serverPlayer) {
        BlockPos blockPos = positionTracker.currentBlockPosition().below();
        CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.trigger(serverPlayer, blockPos, itemStack);
    }

    private boolean canThrowItemToTarget(E livingEntity) {
        return !((InventoryCarrier)livingEntity).getInventory().isEmpty() && this.targetPositionGetter.apply((LivingEntity)livingEntity).isPresent();
    }

    private static Vec3 getThrowPosition(PositionTracker positionTracker) {
        return positionTracker.currentPosition().add(0.0, 1.0, 0.0);
    }
}

