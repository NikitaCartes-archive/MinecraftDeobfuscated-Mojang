package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class SmoothSwimmingMoveControl extends MoveControl {
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
		if (this.applyGravity && this.mob.isInWater()) {
			this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0.0, 0.005, 0.0));
		}

		if (this.operation == MoveControl.Operation.MOVE_TO && !this.mob.getNavigation().isDone()) {
			double d = this.wantedX - this.mob.getX();
			double e = this.wantedY - this.mob.getY();
			double f = this.wantedZ - this.mob.getZ();
			double g = d * d + e * e + f * f;
			if (g < 2.5000003E-7F) {
				this.mob.setZza(0.0F);
			} else {
				float h = (float)(Mth.atan2(f, d) * 180.0F / (float)Math.PI) - 90.0F;
				this.mob.yRot = this.rotlerp(this.mob.yRot, h, (float)this.maxTurnY);
				this.mob.yBodyRot = this.mob.yRot;
				this.mob.yHeadRot = this.mob.yRot;
				float i = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
				if (this.mob.isInWater()) {
					this.mob.setSpeed(i * this.inWaterSpeedModifier);
					float j = -((float)(Mth.atan2(e, (double)Mth.sqrt(d * d + f * f)) * 180.0F / (float)Math.PI));
					j = Mth.clamp(Mth.wrapDegrees(j), (float)(-this.maxTurnX), (float)this.maxTurnX);
					this.mob.xRot = this.rotlerp(this.mob.xRot, j, 5.0F);
					float k = Mth.cos(this.mob.xRot * (float) (Math.PI / 180.0));
					float l = Mth.sin(this.mob.xRot * (float) (Math.PI / 180.0));
					this.mob.zza = k * i;
					this.mob.yya = -l * i;
				} else {
					this.mob.setSpeed(i * this.outsideWaterSpeedModifier);
				}
			}
		} else {
			this.mob.setSpeed(0.0F);
			this.mob.setXxa(0.0F);
			this.mob.setYya(0.0F);
			this.mob.setZza(0.0F);
		}
	}
}
