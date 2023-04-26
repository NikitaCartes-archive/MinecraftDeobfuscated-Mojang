package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class FlyingMoveControl extends MoveControl {
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
			if (g < 2.5000003E-7F) {
				this.mob.setYya(0.0F);
				this.mob.setZza(0.0F);
				return;
			}

			float h = (float)(Mth.atan2(f, d) * 180.0F / (float)Math.PI) - 90.0F;
			this.mob.setYRot(this.rotlerp(this.mob.getYRot(), h, 90.0F));
			float i;
			if (this.mob.onGround()) {
				i = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
			} else {
				i = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.FLYING_SPEED));
			}

			this.mob.setSpeed(i);
			double j = Math.sqrt(d * d + f * f);
			if (Math.abs(e) > 1.0E-5F || Math.abs(j) > 1.0E-5F) {
				float k = (float)(-(Mth.atan2(e, j) * 180.0F / (float)Math.PI));
				this.mob.setXRot(this.rotlerp(this.mob.getXRot(), k, (float)this.maxTurn));
				this.mob.setYya(e > 0.0 ? i : -i);
			}
		} else {
			if (!this.hoversInPlace) {
				this.mob.setNoGravity(false);
			}

			this.mob.setYya(0.0F);
			this.mob.setZza(0.0F);
		}
	}
}
