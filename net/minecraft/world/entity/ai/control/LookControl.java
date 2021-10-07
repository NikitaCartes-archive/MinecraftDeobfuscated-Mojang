/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.control;

import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.Control;
import net.minecraft.world.phys.Vec3;

public class LookControl
implements Control {
    protected final Mob mob;
    protected float yMaxRotSpeed;
    protected float xMaxRotAngle;
    protected int lookAtCooldown;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;

    public LookControl(Mob mob) {
        this.mob = mob;
    }

    public void setLookAt(Vec3 vec3) {
        this.setLookAt(vec3.x, vec3.y, vec3.z);
    }

    public void setLookAt(Entity entity) {
        this.setLookAt(entity.getX(), LookControl.getWantedY(entity), entity.getZ());
    }

    public void setLookAt(Entity entity, float f, float g) {
        this.setLookAt(entity.getX(), LookControl.getWantedY(entity), entity.getZ(), f, g);
    }

    public void setLookAt(double d, double e, double f) {
        this.setLookAt(d, e, f, this.mob.getHeadRotSpeed(), this.mob.getMaxHeadXRot());
    }

    public void setLookAt(double d, double e, double f, float g, float h) {
        this.wantedX = d;
        this.wantedY = e;
        this.wantedZ = f;
        this.yMaxRotSpeed = g;
        this.xMaxRotAngle = h;
        this.lookAtCooldown = 2;
    }

    public void tick() {
        if (this.resetXRotOnTick()) {
            this.mob.setXRot(0.0f);
        }
        if (this.lookAtCooldown > 0) {
            --this.lookAtCooldown;
            this.getYRotD().ifPresent(float_ -> {
                this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, float_.floatValue(), this.yMaxRotSpeed);
            });
            this.getXRotD().ifPresent(float_ -> this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), float_.floatValue(), this.xMaxRotAngle)));
        } else {
            this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, this.mob.yBodyRot, 10.0f);
        }
        this.clampHeadRotationToBody();
    }

    protected void clampHeadRotationToBody() {
        if (!this.mob.getNavigation().isDone()) {
            this.mob.yHeadRot = Mth.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, this.mob.getMaxHeadYRot());
        }
    }

    protected boolean resetXRotOnTick() {
        return true;
    }

    public boolean isLookingAtTarget() {
        return this.lookAtCooldown > 0;
    }

    public double getWantedX() {
        return this.wantedX;
    }

    public double getWantedY() {
        return this.wantedY;
    }

    public double getWantedZ() {
        return this.wantedZ;
    }

    protected Optional<Float> getXRotD() {
        double d = this.wantedX - this.mob.getX();
        double e = this.wantedY - this.mob.getEyeY();
        double f = this.wantedZ - this.mob.getZ();
        double g = Math.sqrt(d * d + f * f);
        return Math.abs(e) > (double)1.0E-5f || Math.abs(g) > (double)1.0E-5f ? Optional.of(Float.valueOf((float)(-(Mth.atan2(e, g) * 57.2957763671875)))) : Optional.empty();
    }

    protected Optional<Float> getYRotD() {
        double d = this.wantedX - this.mob.getX();
        double e = this.wantedZ - this.mob.getZ();
        return Math.abs(e) > (double)1.0E-5f || Math.abs(d) > (double)1.0E-5f ? Optional.of(Float.valueOf((float)(Mth.atan2(e, d) * 57.2957763671875) - 90.0f)) : Optional.empty();
    }

    protected float rotateTowards(float f, float g, float h) {
        float i = Mth.degreesDifference(f, g);
        float j = Mth.clamp(i, -h, h);
        return f + j;
    }

    private static double getWantedY(Entity entity) {
        if (entity instanceof LivingEntity) {
            return entity.getEyeY();
        }
        return (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0;
    }
}

