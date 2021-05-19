/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class TryFindWater
extends Behavior<PathfinderMob> {
    private final int range;
    private final float speedModifier;
    private long nextOkStartTime;

    public TryFindWater(int i, float f) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));
        this.range = i;
        this.speedModifier = f;
    }

    @Override
    protected void stop(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        this.nextOkStartTime = l + 20L + 2L;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        return !pathfinderMob.level.getFluidState(pathfinderMob.blockPosition()).is(FluidTags.WATER);
    }

    @Override
    protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        if (l < this.nextOkStartTime) {
            return;
        }
        BlockPos blockPos = null;
        BlockPos blockPos2 = null;
        BlockPos blockPos3 = pathfinderMob.blockPosition();
        Iterable<BlockPos> iterable = BlockPos.withinManhattan(blockPos3, this.range, this.range, this.range);
        for (BlockPos blockPos4 : iterable) {
            if (blockPos4.getX() == blockPos3.getX() && blockPos4.getZ() == blockPos3.getZ()) continue;
            BlockState blockState = pathfinderMob.level.getBlockState(blockPos4.above());
            BlockState blockState2 = pathfinderMob.level.getBlockState(blockPos4);
            if (!blockState2.is(Blocks.WATER)) continue;
            if (blockState.isAir()) {
                blockPos = blockPos4.immutable();
                break;
            }
            if (blockPos2 != null || blockPos4.closerThan(pathfinderMob.position(), 1.5)) continue;
            blockPos2 = blockPos4.immutable();
        }
        if (blockPos == null) {
            blockPos = blockPos2;
        }
        if (blockPos != null) {
            this.nextOkStartTime = l + 40L;
            BehaviorUtils.setWalkAndLookTargetMemories((LivingEntity)pathfinderMob, blockPos, this.speedModifier, 0);
        }
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (PathfinderMob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (PathfinderMob)livingEntity, l);
    }
}

