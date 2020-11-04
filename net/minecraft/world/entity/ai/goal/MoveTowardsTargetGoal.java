/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class MoveTowardsTargetGoal
extends Goal {
    private final PathfinderMob mob;
    private LivingEntity target;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    private final double speedModifier;
    private final float within;

    public MoveTowardsTargetGoal(PathfinderMob pathfinderMob, double d, float f) {
        this.mob = pathfinderMob;
        this.speedModifier = d;
        this.within = f;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        this.target = this.mob.getTarget();
        if (this.target == null) {
            return false;
        }
        if (this.target.distanceToSqr(this.mob) > (double)(this.within * this.within)) {
            return false;
        }
        Vec3 vec3 = DefaultRandomPos.getPosTowards(this.mob, 16, 7, this.target.position(), 1.5707963705062866);
        if (vec3 == null) {
            return false;
        }
        this.wantedX = vec3.x;
        this.wantedY = vec3.y;
        this.wantedZ = vec3.z;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone() && this.target.isAlive() && this.target.distanceToSqr(this.mob) < (double)(this.within * this.within);
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }
}

