/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface ItemSteerable {
    public boolean boost();

    public void travelWithInput(Vec3 var1);

    public float getSteeringSpeed();

    default public boolean travel(Mob mob, ItemBasedSteering itemBasedSteering, Vec3 vec3) {
        Entity entity;
        if (!mob.isAlive()) {
            return false;
        }
        Entity entity2 = entity = mob.getPassengers().isEmpty() ? null : mob.getPassengers().get(0);
        if (!(mob.isVehicle() && mob.canBeControlledByRider() && entity instanceof Player)) {
            mob.maxUpStep = 0.5f;
            mob.flyingSpeed = 0.02f;
            this.travelWithInput(vec3);
            return false;
        }
        mob.yRotO = mob.yRot = entity.yRot;
        mob.xRot = entity.xRot * 0.5f;
        mob.setRot(mob.yRot, mob.xRot);
        mob.yBodyRot = mob.yRot;
        mob.yHeadRot = mob.yRot;
        mob.maxUpStep = 1.0f;
        mob.flyingSpeed = mob.getSpeed() * 0.1f;
        if (itemBasedSteering.boosting && itemBasedSteering.boostTime++ > itemBasedSteering.boostTimeTotal) {
            itemBasedSteering.boosting = false;
        }
        if (mob.isControlledByLocalInstance()) {
            float f = this.getSteeringSpeed();
            if (itemBasedSteering.boosting) {
                f += f * 1.15f * Mth.sin((float)itemBasedSteering.boostTime / (float)itemBasedSteering.boostTimeTotal * (float)Math.PI);
            }
            mob.setSpeed(f);
            this.travelWithInput(new Vec3(0.0, 0.0, 1.0));
            mob.lerpSteps = 0;
        } else {
            mob.calculateEntityAnimation(mob, false);
            mob.setDeltaMovement(Vec3.ZERO);
        }
        return true;
    }
}

