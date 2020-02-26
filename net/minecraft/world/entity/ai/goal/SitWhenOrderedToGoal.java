/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;

public class SitWhenOrderedToGoal
extends Goal {
    private final TamableAnimal mob;

    public SitWhenOrderedToGoal(TamableAnimal tamableAnimal) {
        this.mob = tamableAnimal;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.isOrderedToSit();
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isTame()) {
            return false;
        }
        if (this.mob.isInWaterOrBubble()) {
            return false;
        }
        if (!this.mob.isOnGround()) {
            return false;
        }
        LivingEntity livingEntity = this.mob.getOwner();
        if (livingEntity == null) {
            return true;
        }
        if (this.mob.distanceToSqr(livingEntity) < 144.0 && livingEntity.getLastHurtByMob() != null) {
            return false;
        }
        return this.mob.isOrderedToSit();
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
        this.mob.setInSittingPose(true);
    }

    @Override
    public void stop() {
        this.mob.setInSittingPose(false);
    }
}

