/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

public class MeleeAttackGoal
extends Goal {
    protected final PathfinderMob mob;
    protected int attackTime;
    private final double speedModifier;
    private final boolean trackTarget;
    private Path path;
    private int timeToRecalcPath;
    private double pathedTargetX;
    private double pathedTargetY;
    private double pathedTargetZ;
    protected final int attackInterval = 20;
    private long lastUpdate;

    public MeleeAttackGoal(PathfinderMob pathfinderMob, double d, boolean bl) {
        this.mob = pathfinderMob;
        this.speedModifier = d;
        this.trackTarget = bl;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        long l = this.mob.level.getGameTime();
        if (l - this.lastUpdate < 20L) {
            return false;
        }
        this.lastUpdate = l;
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null) {
            return false;
        }
        if (!livingEntity.isAlive()) {
            return false;
        }
        this.path = this.mob.getNavigation().createPath(livingEntity, 0);
        if (this.path != null) {
            return true;
        }
        return this.getAttackReachSqr(livingEntity) >= this.mob.distanceToSqr(livingEntity.x, livingEntity.getBoundingBox().minY, livingEntity.z);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null) {
            return false;
        }
        if (!livingEntity.isAlive()) {
            return false;
        }
        if (!this.trackTarget) {
            return !this.mob.getNavigation().isDone();
        }
        if (!this.mob.isWithinRestriction(new BlockPos(livingEntity))) {
            return false;
        }
        return !(livingEntity instanceof Player) || !livingEntity.isSpectator() && !((Player)livingEntity).isCreative();
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.path, this.speedModifier);
        this.mob.setAggressive(true);
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingEntity)) {
            this.mob.setTarget(null);
        }
        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity livingEntity = this.mob.getTarget();
        this.mob.getLookControl().setLookAt(livingEntity, 30.0f, 30.0f);
        double d = this.mob.distanceToSqr(livingEntity.x, livingEntity.getBoundingBox().minY, livingEntity.z);
        --this.timeToRecalcPath;
        if ((this.trackTarget || this.mob.getSensing().canSee(livingEntity)) && this.timeToRecalcPath <= 0 && (this.pathedTargetX == 0.0 && this.pathedTargetY == 0.0 && this.pathedTargetZ == 0.0 || livingEntity.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0 || this.mob.getRandom().nextFloat() < 0.05f)) {
            this.pathedTargetX = livingEntity.x;
            this.pathedTargetY = livingEntity.getBoundingBox().minY;
            this.pathedTargetZ = livingEntity.z;
            this.timeToRecalcPath = 4 + this.mob.getRandom().nextInt(7);
            if (d > 1024.0) {
                this.timeToRecalcPath += 10;
            } else if (d > 256.0) {
                this.timeToRecalcPath += 5;
            }
            if (!this.mob.getNavigation().moveTo(livingEntity, this.speedModifier)) {
                this.timeToRecalcPath += 15;
            }
        }
        this.attackTime = Math.max(this.attackTime - 1, 0);
        this.checkAndPerformAttack(livingEntity, d);
    }

    protected void checkAndPerformAttack(LivingEntity livingEntity, double d) {
        double e = this.getAttackReachSqr(livingEntity);
        if (d <= e && this.attackTime <= 0) {
            this.attackTime = 20;
            this.mob.swing(InteractionHand.MAIN_HAND);
            this.mob.doHurtTarget(livingEntity);
        }
    }

    protected double getAttackReachSqr(LivingEntity livingEntity) {
        return this.mob.getBbWidth() * 2.0f * (this.mob.getBbWidth() * 2.0f) + livingEntity.getBbWidth();
    }
}

