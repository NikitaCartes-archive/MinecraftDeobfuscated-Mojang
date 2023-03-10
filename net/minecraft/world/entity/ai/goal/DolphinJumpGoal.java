/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.JumpGoal;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public class DolphinJumpGoal
extends JumpGoal {
    private static final int[] STEPS_TO_CHECK = new int[]{0, 1, 4, 5, 6, 7};
    private final Dolphin dolphin;
    private final int interval;
    private boolean breached;

    public DolphinJumpGoal(Dolphin dolphin, int i) {
        this.dolphin = dolphin;
        this.interval = DolphinJumpGoal.reducedTickDelay(i);
    }

    @Override
    public boolean canUse() {
        if (this.dolphin.getRandom().nextInt(this.interval) != 0) {
            return false;
        }
        Direction direction = this.dolphin.getMotionDirection();
        int i = direction.getStepX();
        int j = direction.getStepZ();
        BlockPos blockPos = this.dolphin.blockPosition();
        for (int k : STEPS_TO_CHECK) {
            if (this.waterIsClear(blockPos, i, j, k) && this.surfaceIsClear(blockPos, i, j, k)) continue;
            return false;
        }
        return true;
    }

    private boolean waterIsClear(BlockPos blockPos, int i, int j, int k) {
        BlockPos blockPos2 = blockPos.offset(i * k, 0, j * k);
        return this.dolphin.level.getFluidState(blockPos2).is(FluidTags.WATER) && !this.dolphin.level.getBlockState(blockPos2).getMaterial().blocksMotion();
    }

    private boolean surfaceIsClear(BlockPos blockPos, int i, int j, int k) {
        return this.dolphin.level.getBlockState(blockPos.offset(i * k, 1, j * k)).isAir() && this.dolphin.level.getBlockState(blockPos.offset(i * k, 2, j * k)).isAir();
    }

    @Override
    public boolean canContinueToUse() {
        double d = this.dolphin.getDeltaMovement().y;
        return !(d * d < (double)0.03f && this.dolphin.getXRot() != 0.0f && Math.abs(this.dolphin.getXRot()) < 10.0f && this.dolphin.isInWater() || this.dolphin.isOnGround());
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        Direction direction = this.dolphin.getMotionDirection();
        this.dolphin.setDeltaMovement(this.dolphin.getDeltaMovement().add((double)direction.getStepX() * 0.6, 0.7, (double)direction.getStepZ() * 0.6));
        this.dolphin.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.dolphin.setXRot(0.0f);
    }

    @Override
    public void tick() {
        boolean bl = this.breached;
        if (!bl) {
            FluidState fluidState = this.dolphin.level.getFluidState(this.dolphin.blockPosition());
            this.breached = fluidState.is(FluidTags.WATER);
        }
        if (this.breached && !bl) {
            this.dolphin.playSound(SoundEvents.DOLPHIN_JUMP, 1.0f, 1.0f);
        }
        Vec3 vec3 = this.dolphin.getDeltaMovement();
        if (vec3.y * vec3.y < (double)0.03f && this.dolphin.getXRot() != 0.0f) {
            this.dolphin.setXRot(Mth.rotLerp(0.2f, this.dolphin.getXRot(), 0.0f));
        } else if (vec3.length() > (double)1.0E-5f) {
            double d = vec3.horizontalDistance();
            double e = Math.atan2(-vec3.y, d) * 57.2957763671875;
            this.dolphin.setXRot((float)e);
        }
    }
}

