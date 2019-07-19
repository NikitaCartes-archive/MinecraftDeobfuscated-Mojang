/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public class FollowOwnerGoal
extends Goal {
    protected final TamableAnimal tamable;
    private LivingEntity owner;
    protected final LevelReader level;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;
    private float oldWaterCost;

    public FollowOwnerGoal(TamableAnimal tamableAnimal, double d, float f, float g) {
        this.tamable = tamableAnimal;
        this.level = tamableAnimal.level;
        this.speedModifier = d;
        this.navigation = tamableAnimal.getNavigation();
        this.startDistance = f;
        this.stopDistance = g;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        if (!(tamableAnimal.getNavigation() instanceof GroundPathNavigation) && !(tamableAnimal.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    @Override
    public boolean canUse() {
        LivingEntity livingEntity = this.tamable.getOwner();
        if (livingEntity == null) {
            return false;
        }
        if (livingEntity instanceof Player && ((Player)livingEntity).isSpectator()) {
            return false;
        }
        if (this.tamable.isSitting()) {
            return false;
        }
        if (this.tamable.distanceToSqr(livingEntity) < (double)(this.startDistance * this.startDistance)) {
            return false;
        }
        this.owner = livingEntity;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.navigation.isDone() && this.tamable.distanceToSqr(this.owner) > (double)(this.stopDistance * this.stopDistance) && !this.tamable.isSitting();
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.tamable.getPathfindingMalus(BlockPathTypes.WATER);
        this.tamable.setPathfindingMalus(BlockPathTypes.WATER, 0.0f);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.tamable.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        this.tamable.getLookControl().setLookAt(this.owner, 10.0f, this.tamable.getMaxHeadXRot());
        if (this.tamable.isSitting()) {
            return;
        }
        if (--this.timeToRecalcPath > 0) {
            return;
        }
        this.timeToRecalcPath = 10;
        if (this.navigation.moveTo(this.owner, this.speedModifier)) {
            return;
        }
        if (this.tamable.isLeashed() || this.tamable.isPassenger()) {
            return;
        }
        if (this.tamable.distanceToSqr(this.owner) < 144.0) {
            return;
        }
        int i = Mth.floor(this.owner.x) - 2;
        int j = Mth.floor(this.owner.z) - 2;
        int k = Mth.floor(this.owner.getBoundingBox().minY);
        for (int l = 0; l <= 4; ++l) {
            for (int m = 0; m <= 4; ++m) {
                if (l >= 1 && m >= 1 && l <= 3 && m <= 3 || !this.isTeleportFriendlyBlock(new BlockPos(i + l, k - 1, j + m))) continue;
                this.tamable.moveTo((float)(i + l) + 0.5f, k, (float)(j + m) + 0.5f, this.tamable.yRot, this.tamable.xRot);
                this.navigation.stop();
                return;
            }
        }
    }

    protected boolean isTeleportFriendlyBlock(BlockPos blockPos) {
        BlockState blockState = this.level.getBlockState(blockPos);
        return blockState.isValidSpawn(this.level, blockPos, this.tamable.getType()) && this.level.isEmptyBlock(blockPos.above()) && this.level.isEmptyBlock(blockPos.above(2));
    }
}

