package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class LookControl implements Control {
	protected final Mob mob;
	protected float yMaxRotSpeed;
	protected float xMaxRotAngle;
	protected boolean hasWanted;
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
		this.setLookAt(entity.getX(), getWantedY(entity), entity.getZ());
	}

	public void setLookAt(Entity entity, float f, float g) {
		this.setLookAt(entity.getX(), getWantedY(entity), entity.getZ(), f, g);
	}

	public void setLookAt(double d, double e, double f) {
		this.setLookAt(d, e, f, (float)this.mob.getHeadRotSpeed(), (float)this.mob.getMaxHeadXRot());
	}

	public void setLookAt(double d, double e, double f, float g, float h) {
		this.wantedX = d;
		this.wantedY = e;
		this.wantedZ = f;
		this.yMaxRotSpeed = g;
		this.xMaxRotAngle = h;
		this.hasWanted = true;
	}

	public void tick() {
		if (this.resetXRotOnTick()) {
			this.mob.setXRot(0.0F);
		}

		if (this.hasWanted) {
			this.hasWanted = false;
			this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, this.getYRotD(), this.yMaxRotSpeed);
			this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), this.getXRotD(), this.xMaxRotAngle));
		} else {
			this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, this.mob.yBodyRot, 10.0F);
		}

		this.clampHeadRotationToBody();
	}

	protected void clampHeadRotationToBody() {
		if (!this.mob.getNavigation().isDone()) {
			this.mob.yHeadRot = Mth.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, (float)this.mob.getMaxHeadYRot());
		}
	}

	protected boolean resetXRotOnTick() {
		return true;
	}

	public boolean isHasWanted() {
		return this.hasWanted;
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

	protected float getXRotD() {
		double d = this.wantedX - this.mob.getX();
		double e = this.wantedY - this.mob.getEyeY();
		double f = this.wantedZ - this.mob.getZ();
		double g = Math.sqrt(d * d + f * f);
		return (float)(-(Mth.atan2(e, g) * 180.0F / (float)Math.PI));
	}

	protected float getYRotD() {
		double d = this.wantedX - this.mob.getX();
		double e = this.wantedZ - this.mob.getZ();
		return (float)(Mth.atan2(e, d) * 180.0F / (float)Math.PI) - 90.0F;
	}

	protected float rotateTowards(float f, float g, float h) {
		float i = Mth.degreesDifference(f, g);
		float j = Mth.clamp(i, -h, h);
		return f + j;
	}

	private static double getWantedY(Entity entity) {
		return entity instanceof LivingEntity ? entity.getEyeY() : (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0;
	}
}
