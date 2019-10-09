/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FleeSunGoal
extends Goal {
    protected final PathfinderMob mob;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    private final double speedModifier;
    private final Level level;

    public FleeSunGoal(PathfinderMob pathfinderMob, double d) {
        this.mob = pathfinderMob;
        this.speedModifier = d;
        this.level = pathfinderMob.level;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.getTarget() != null) {
            return false;
        }
        if (!this.level.isDay()) {
            return false;
        }
        if (!this.mob.isOnFire()) {
            return false;
        }
        if (!this.level.canSeeSky(new BlockPos(this.mob))) {
            return false;
        }
        if (!this.mob.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            return false;
        }
        return this.setWantedPos();
    }

    protected boolean setWantedPos() {
        Vec3 vec3 = this.getHidePos();
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

    @Nullable
    protected Vec3 getHidePos() {
        Random random = this.mob.getRandom();
        BlockPos blockPos = new BlockPos(this.mob);
        for (int i = 0; i < 10; ++i) {
            BlockPos blockPos2 = blockPos.offset(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
            if (this.level.canSeeSky(blockPos2) || !(this.mob.getWalkTargetValue(blockPos2) < 0.0f)) continue;
            return new Vec3(blockPos2);
        }
        return null;
    }
}

