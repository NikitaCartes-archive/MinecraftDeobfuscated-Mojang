/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class MoveTowardsRestrictionGoal
extends Goal {
    private final PathfinderMob mob;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    private final double speedModifier;

    public MoveTowardsRestrictionGoal(PathfinderMob pathfinderMob, double d) {
        this.mob = pathfinderMob;
        this.speedModifier = d;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isWithinRestriction()) {
            return false;
        }
        Vec3 vec3 = DefaultRandomPos.getPosTowards(this.mob, 16, 7, Vec3.atBottomCenterOf(this.mob.getRestrictCenter()), 1.5707963705062866);
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
        return !this.mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }
}

