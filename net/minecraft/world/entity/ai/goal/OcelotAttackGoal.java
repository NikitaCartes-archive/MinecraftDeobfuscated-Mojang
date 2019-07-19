/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.BlockGetter;

public class OcelotAttackGoal
extends Goal {
    private final BlockGetter level;
    private final Mob mob;
    private LivingEntity target;
    private int attackTime;

    public OcelotAttackGoal(Mob mob) {
        this.mob = mob;
        this.level = mob.level;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null) {
            return false;
        }
        this.target = livingEntity;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.target.isAlive()) {
            return false;
        }
        if (this.mob.distanceToSqr(this.target) > 225.0) {
            return false;
        }
        return !this.mob.getNavigation().isDone() || this.canUse();
    }

    @Override
    public void stop() {
        this.target = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.mob.getLookControl().setLookAt(this.target, 30.0f, 30.0f);
        double d = this.mob.getBbWidth() * 2.0f * (this.mob.getBbWidth() * 2.0f);
        double e = this.mob.distanceToSqr(this.target.x, this.target.getBoundingBox().minY, this.target.z);
        double f = 0.8;
        if (e > d && e < 16.0) {
            f = 1.33;
        } else if (e < 225.0) {
            f = 0.6;
        }
        this.mob.getNavigation().moveTo(this.target, f);
        this.attackTime = Math.max(this.attackTime - 1, 0);
        if (e > d) {
            return;
        }
        if (this.attackTime > 0) {
            return;
        }
        this.attackTime = 20;
        this.mob.doHurtTarget(this.target);
    }
}

