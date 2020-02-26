/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;

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
            this.mob.yRot = this.rotlerp(this.mob.yRot, h, 90.0f);
            float i = this.mob.isOnGround() ? (float)(this.speedModifier * this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue()) : (float)(this.speedModifier * this.mob.getAttribute(SharedMonsterAttributes.FLYING_SPEED).getValue());
            this.mob.setSpeed(i);
            double j = Mth.sqrt(d * d + f * f);
            float k = (float)(-(Mth.atan2(e, j) * 57.2957763671875));
            this.mob.xRot = this.rotlerp(this.mob.xRot, k, this.maxTurn);
            this.mob.setYya(e > 0.0 ? i : -i);
        } else {
            if (!this.hoversInPlace) {
                this.mob.setNoGravity(false);
            }
            this.mob.setYya(0.0f);
            this.mob.setZza(0.0f);
        }
    }
}

