/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

public class SmoothSwimmingMoveControl
extends MoveControl {
    private final int maxTurnX;
    private final int maxTurnY;
    private final float inWaterSpeedModifier;
    private final float outsideWaterSpeedModifier;
    private final boolean applyGravity;

    public SmoothSwimmingMoveControl(Mob mob, int i, int j, float f, float g, boolean bl) {
        super(mob);
        this.maxTurnX = i;
        this.maxTurnY = j;
        this.inWaterSpeedModifier = f;
        this.outsideWaterSpeedModifier = g;
        this.applyGravity = bl;
    }

    @Override
    public void tick() {
        double f;
        double e;
        if (this.applyGravity && this.mob.isInWater()) {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0.0, 0.005, 0.0));
        }
        if (this.operation != MoveControl.Operation.MOVE_TO || this.mob.getNavigation().isDone()) {
            this.mob.setSpeed(0.0f);
            this.mob.setXxa(0.0f);
            this.mob.setYya(0.0f);
            this.mob.setZza(0.0f);
            return;
        }
        double d = this.wantedX - this.mob.getX();
        double g = d * d + (e = this.wantedY - this.mob.getY()) * e + (f = this.wantedZ - this.mob.getZ()) * f;
        if (g < 2.500000277905201E-7) {
            this.mob.setZza(0.0f);
            return;
        }
        float h = (float)(Mth.atan2(f, d) * 57.2957763671875) - 90.0f;
        this.mob.setYRot(this.rotlerp(this.mob.getYRot(), h, this.maxTurnY));
        this.mob.yBodyRot = this.mob.getYRot();
        this.mob.yHeadRot = this.mob.getYRot();
        float i = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
        if (this.mob.isInWater()) {
            this.mob.setSpeed(i * this.inWaterSpeedModifier);
            float j = -((float)(Mth.atan2(e, Mth.sqrt(d * d + f * f)) * 57.2957763671875));
            j = Mth.clamp(Mth.wrapDegrees(j), (float)(-this.maxTurnX), (float)this.maxTurnX);
            this.mob.setXRot(this.rotlerp(this.mob.getXRot(), j, 5.0f));
            float k = Mth.cos(this.mob.getXRot() * ((float)Math.PI / 180));
            float l = Mth.sin(this.mob.getXRot() * ((float)Math.PI / 180));
            this.mob.zza = k * i;
            this.mob.yya = -l * i;
        } else {
            this.mob.setSpeed(i * this.outsideWaterSpeedModifier);
        }
    }
}

