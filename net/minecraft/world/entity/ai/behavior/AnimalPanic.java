/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class AnimalPanic
extends Behavior<PathfinderMob> {
    private static final int PANIC_MIN_DURATION = 100;
    private static final int PANIC_MAX_DURATION = 120;
    private static final int PANIC_DISTANCE_HORIZONTAL = 5;
    private static final int PANIC_DISTANCE_VERTICAL = 4;
    private final float speedMultiplier;

    public AnimalPanic(float f) {
        super(ImmutableMap.of(MemoryModuleType.HURT_BY, MemoryStatus.VALUE_PRESENT), 100, 120);
        this.speedMultiplier = f;
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        return true;
    }

    @Override
    protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        pathfinderMob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected void tick(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        Vec3 vec3;
        if (pathfinderMob.getNavigation().isDone() && (vec3 = this.getPanicPos(pathfinderMob, serverLevel)) != null) {
            pathfinderMob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3, this.speedMultiplier, 0));
        }
    }

    @Nullable
    private Vec3 getPanicPos(PathfinderMob pathfinderMob, ServerLevel serverLevel) {
        Optional<Vec3> optional;
        if (pathfinderMob.isOnFire() && (optional = this.lookForWater(serverLevel, pathfinderMob).map(Vec3::atBottomCenterOf)).isPresent()) {
            return optional.get();
        }
        return LandRandomPos.getPos(pathfinderMob, 5, 4);
    }

    private Optional<BlockPos> lookForWater(BlockGetter blockGetter, Entity entity) {
        BlockPos blockPos2 = entity.blockPosition();
        if (!blockGetter.getBlockState(blockPos2).getCollisionShape(blockGetter, blockPos2).isEmpty()) {
            return Optional.empty();
        }
        return BlockPos.findClosestMatch(blockPos2, 5, 1, blockPos -> blockGetter.getFluidState((BlockPos)blockPos).is(FluidTags.WATER));
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (PathfinderMob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (PathfinderMob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (PathfinderMob)livingEntity, l);
    }
}

