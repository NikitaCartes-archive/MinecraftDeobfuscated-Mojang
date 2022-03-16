/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
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
import net.minecraft.world.phys.shapes.CollisionContext;

public class TryFindLand
extends Behavior<PathfinderMob> {
    private static final int COOLDOWN_TICKS = 60;
    private final int range;
    private final float speedModifier;
    private long nextOkStartTime;

    public TryFindLand(int i, float f) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));
        this.range = i;
        this.speedModifier = f;
    }

    @Override
    protected void stop(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        this.nextOkStartTime = l + 60L;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        return pathfinderMob.level.getFluidState(pathfinderMob.blockPosition()).is(FluidTags.WATER);
    }

    @Override
    protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        if (l < this.nextOkStartTime) {
            return;
        }
        BlockPos blockPos = pathfinderMob.blockPosition();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        CollisionContext collisionContext = CollisionContext.of(pathfinderMob);
        for (BlockPos blockPos2 : BlockPos.withinManhattan(blockPos, this.range, this.range, this.range)) {
            if (blockPos2.getX() == blockPos.getX() && blockPos2.getZ() == blockPos.getZ()) continue;
            BlockState blockState = serverLevel.getBlockState(blockPos2);
            BlockState blockState2 = serverLevel.getBlockState(mutableBlockPos.setWithOffset((Vec3i)blockPos2, Direction.DOWN));
            if (blockState.is(Blocks.WATER) || !serverLevel.getFluidState(blockPos2).isEmpty() || !blockState.getCollisionShape(serverLevel, blockPos2, collisionContext).isEmpty() || !blockState2.isFaceSturdy(serverLevel, mutableBlockPos, Direction.UP)) continue;
            this.nextOkStartTime = l + 60L;
            BehaviorUtils.setWalkAndLookTargetMemories((LivingEntity)pathfinderMob, blockPos2.immutable(), this.speedModifier, 1);
            return;
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

