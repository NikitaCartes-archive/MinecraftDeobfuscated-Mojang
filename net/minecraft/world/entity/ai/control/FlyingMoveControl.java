/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

public class FlyingMoveControl
extends MoveControl {
    private final int maxTurn;
    private final boolean hoversInPlace;

    public FlyingMoveControl(Mob mob, int i, boolean bl) {
        super(mob);
        this.maxTurn = i;
        this.hoversInPlace = bl;
    }

    @Override
    public void tick() {
        if (this.operation == MoveControl.Operation.MOVE_TO) {
            this.operation = MoveControl.Operation.WAIT;
            this.mob.setNoGravity(true);
            double d = this.wantedX - this.mob.getX();
            double e = this.wantedY - this.mob.getY();
            double f = this.wantedZ - this.mob.getZ();
            double g = d * d + e * e + f * f;
            if (g < 2.500000277905201E-7) {
                this.mob.setYya(0.0f);
                this.mob.setZza(0.0f);
                return;
            }
            float h = (float)(Mth.atan2(f, d) * 57.2957763671875) - 90.0f;
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), h, 90.0f));
            float i = this.mob.isOnGround() ? (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)) : (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.FLYING_SPEED));
            this.mob.setSpeed(i);
            double j = Math.sqrt(d * d + f * f);
            if (Math.abs(e) > (double)1.0E-5f || Math.abs(j) > (double)1.0E-5f) {
                float k = (float)(-(Mth.atan2(e, j) * 57.2957763671875));
                this.mob.setXRot(this.rotlerp(this.mob.getXRot(), k, this.maxTurn));
                this.mob.setYya(e > 0.0 ? i : -i);
            }
        } else {
            if (!this.hoversInPlace) {
                this.mob.setNoGravity(false);
            }
            this.mob.setYya(0.0f);
            this.mob.setZza(0.0f);
        }
    }
}

