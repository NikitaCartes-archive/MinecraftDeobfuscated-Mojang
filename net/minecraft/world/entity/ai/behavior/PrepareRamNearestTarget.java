/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class PrepareRamNearestTarget<E extends PathfinderMob>
extends Behavior<E> {
    public static final int TIME_OUT_DURATION = 160;
    private final ToIntFunction<E> getCooldownOnFail;
    private final int minRamDistance;
    private final int maxRamDistance;
    private final float walkSpeed;
    private final TargetingConditions ramTargeting;
    private final int ramPrepareTime;
    private final Function<E, SoundEvent> getPrepareRamSound;
    private Optional<Long> reachedRamPositionTimestamp = Optional.empty();
    private Optional<RamCandidate> ramCandidate = Optional.empty();

    public PrepareRamNearestTarget(ToIntFunction<E> toIntFunction, int i, int j, float f, TargetingConditions targetingConditions, int k, Function<E, SoundEvent> function) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT, MemoryModuleType.RAM_TARGET, MemoryStatus.VALUE_ABSENT), 160);
        this.getCooldownOnFail = toIntFunction;
        this.minRamDistance = i;
        this.maxRamDistance = j;
        this.walkSpeed = f;
        this.ramTargeting = targetingConditions;
        this.ramPrepareTime = k;
        this.getPrepareRamSound = function;
    }

    @Override
    protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        Brain<?> brain = pathfinderMob.getBrain();
        brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).flatMap(list -> list.stream().filter(livingEntity -> this.ramTargeting.test(pathfinderMob, (LivingEntity)livingEntity)).findFirst()).ifPresent(livingEntity -> this.chooseRamPosition(pathfinderMob, (LivingEntity)livingEntity));
    }

    @Override
    protected void stop(ServerLevel serverLevel, E pathfinderMob, long l) {
        Brain<Vec3> brain = ((LivingEntity)pathfinderMob).getBrain();
        if (!brain.hasMemoryValue(MemoryModuleType.RAM_TARGET)) {
            serverLevel.broadcastEntityEvent((Entity)pathfinderMob, (byte)59);
            brain.setMemory(MemoryModuleType.RAM_COOLDOWN_TICKS, this.getCooldownOnFail.applyAsInt(pathfinderMob));
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        return this.ramCandidate.isPresent() && this.ramCandidate.get().getTarget().isAlive();
    }

    @Override
    protected void tick(ServerLevel serverLevel, E pathfinderMob, long l) {
        boolean bl;
        if (!this.ramCandidate.isPresent()) {
            return;
        }
        ((LivingEntity)pathfinderMob).getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.ramCandidate.get().getStartPosition(), this.walkSpeed, 0));
        ((LivingEntity)pathfinderMob).getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.ramCandidate.get().getTarget(), true));
        boolean bl2 = bl = !this.ramCandidate.get().getTarget().blockPosition().equals(this.ramCandidate.get().getTargetPosition());
        if (bl) {
            serverLevel.broadcastEntityEvent((Entity)pathfinderMob, (byte)59);
            ((Mob)pathfinderMob).getNavigation().stop();
            this.chooseRamPosition((PathfinderMob)pathfinderMob, this.ramCandidate.get().target);
        } else {
            BlockPos blockPos = ((Entity)pathfinderMob).blockPosition();
            if (blockPos.equals(this.ramCandidate.get().getStartPosition())) {
                serverLevel.broadcastEntityEvent((Entity)pathfinderMob, (byte)58);
                if (!this.reachedRamPositionTimestamp.isPresent()) {
                    this.reachedRamPositionTimestamp = Optional.of(l);
                }
                if (l - this.reachedRamPositionTimestamp.get() >= (long)this.ramPrepareTime) {
                    ((LivingEntity)pathfinderMob).getBrain().setMemory(MemoryModuleType.RAM_TARGET, this.getEdgeOfBlock(blockPos, this.ramCandidate.get().getTargetPosition()));
                    serverLevel.playSound(null, (Entity)pathfinderMob, this.getPrepareRamSound.apply(pathfinderMob), SoundSource.HOSTILE, 1.0f, ((LivingEntity)pathfinderMob).getVoicePitch());
                    this.ramCandidate = Optional.empty();
                }
            }
        }
    }

    private Vec3 getEdgeOfBlock(BlockPos blockPos, BlockPos blockPos2) {
        double d = 0.5;
        double e = 0.5 * (double)Mth.sign(blockPos2.getX() - blockPos.getX());
        double f = 0.5 * (double)Mth.sign(blockPos2.getZ() - blockPos.getZ());
        return Vec3.atBottomCenterOf(blockPos2).add(e, 0.0, f);
    }

    private Optional<BlockPos> calculateRammingStartPosition(PathfinderMob pathfinderMob, LivingEntity livingEntity) {
        BlockPos blockPos2 = livingEntity.blockPosition();
        if (!this.isWalkableBlock(pathfinderMob, blockPos2)) {
            return Optional.empty();
        }
        ArrayList<BlockPos> list = Lists.newArrayList();
        BlockPos.MutableBlockPos mutableBlockPos = blockPos2.mutable();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            mutableBlockPos.set(blockPos2);
            for (int i = 0; i < this.maxRamDistance; ++i) {
                if (this.isWalkableBlock(pathfinderMob, mutableBlockPos.move(direction))) continue;
                mutableBlockPos.move(direction.getOpposite());
                break;
            }
            if (mutableBlockPos.distManhattan(blockPos2) < this.minRamDistance) continue;
            list.add(mutableBlockPos.immutable());
        }
        PathNavigation pathNavigation = pathfinderMob.getNavigation();
        return list.stream().sorted(Comparator.comparingDouble(pathfinderMob.blockPosition()::distSqr)).filter(blockPos -> {
            Path path = pathNavigation.createPath((BlockPos)blockPos, 0);
            return path != null && path.canReach();
        }).findFirst();
    }

    private boolean isWalkableBlock(PathfinderMob pathfinderMob, BlockPos blockPos) {
        return pathfinderMob.getNavigation().isStableDestination(blockPos) && pathfinderMob.getPathfindingMalus(WalkNodeEvaluator.getBlockPathTypeStatic(pathfinderMob.level, blockPos.mutable())) == 0.0f;
    }

    private void chooseRamPosition(PathfinderMob pathfinderMob, LivingEntity livingEntity) {
        this.reachedRamPositionTimestamp = Optional.empty();
        this.ramCandidate = this.calculateRammingStartPosition(pathfinderMob, livingEntity).map(blockPos -> new RamCandidate((BlockPos)blockPos, livingEntity.blockPosition(), livingEntity));
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (PathfinderMob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (E)((PathfinderMob)livingEntity), l);
    }

    public static class RamCandidate {
        private final BlockPos startPosition;
        private final BlockPos targetPosition;
        final LivingEntity target;

        public RamCandidate(BlockPos blockPos, BlockPos blockPos2, LivingEntity livingEntity) {
            this.startPosition = blockPos;
            this.targetPosition = blockPos2;
            this.target = livingEntity;
        }

        public BlockPos getStartPosition() {
            return this.startPosition;
        }

        public BlockPos getTargetPosition() {
            return this.targetPosition;
        }

        public LivingEntity getTarget() {
            return this.target;
        }
    }
}

