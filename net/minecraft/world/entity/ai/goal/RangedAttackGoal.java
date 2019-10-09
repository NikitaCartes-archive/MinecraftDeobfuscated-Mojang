/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

public class RangedAttackGoal
extends Goal {
    private final Mob mob;
    private final RangedAttackMob rangedAttackMob;
    private LivingEntity target;
    private int attackTime = -1;
    private final double speedModifier;
    private int seeTime;
    private final int attackIntervalMin;
    private final int attackIntervalMax;
    private final float attackRadius;
    private final float attackRadiusSqr;

    public RangedAttackGoal(RangedAttackMob rangedAttackMob, double d, int i, float f) {
        this(rangedAttackMob, d, i, i, f);
    }

    public RangedAttackGoal(RangedAttackMob rangedAttackMob, double d, int i, int j, float f) {
        if (!(rangedAttackMob instanceof LivingEntity)) {
            throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
        }
        this.rangedAttackMob = rangedAttackMob;
        this.mob = (Mob)((Object)rangedAttackMob);
        this.speedModifier = d;
        this.attackIntervalMin = i;
        this.attackIntervalMax = j;
        this.attackRadius = f;
        this.attackRadiusSqr = f * f;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null || !livingEntity.isAlive()) {
            return false;
        }
        this.target = livingEntity;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() || !this.mob.getNavigation().isDone();
    }

    @Override
    public void stop() {
        this.target = null;
        this.seeTime = 0;
        this.attackTime = -1;
    }

    @Override
    public void tick() {
        double d = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        boolean bl = this.mob.getSensing().canSee(this.target);
        this.seeTime = bl ? ++this.seeTime : 0;
        if (d > (double)this.attackRadiusSqr || this.seeTime < 5) {
            this.mob.getNavigation().moveTo(this.target, this.speedModifier);
        } else {
            this.mob.getNavigation().stop();
        }
        this.mob.getLookControl().setLookAt(this.target, 30.0f, 30.0f);
        if (--this.attackTime == 0) {
            float f;
            if (!bl) {
                return;
            }
            float g = f = Mth.sqrt(d) / this.attackRadius;
            g = Mth.clamp(g, 0.1f, 1.0f);
            this.rangedAttackMob.performRangedAttack(this.target, g);
            this.attackTime = Mth.floor(f * (float)(this.attackIntervalMax - this.attackIntervalMin) + (float)this.attackIntervalMin);
        } else if (this.attackTime < 0) {
            float f = Mth.sqrt(d) / this.attackRadius;
            this.attackTime = Mth.floor(f * (float)(this.attackIntervalMax - this.attackIntervalMin) + (float)this.attackIntervalMin);
        }
    }
}

