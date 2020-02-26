package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;

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
			this.mob.yRot = this.rotlerp(this.mob.yRot, h, 90.0F);
			float i;
			if (this.mob.isOnGround()) {
				i = (float)(this.speedModifier * this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue());
			} else {
				i = (float)(this.speedModifier * this.mob.getAttribute(SharedMonsterAttributes.FLYING_SPEED).getValue());
			}

			this.mob.setSpeed(i);
			double j = (double)Mth.sqrt(d * d + f * f);
			float k = (float)(-(Mth.atan2(e, j) * 180.0F / (float)Math.PI));
			this.mob.xRot = this.rotlerp(this.mob.xRot, k, (float)this.maxTurn);
			this.mob.setYya(e > 0.0 ? i : -i);
		} else {
			if (!this.hoversInPlace) {
				this.mob.setNoGravity(false);
			}

			this.mob.setYya(0.0F);
			this.mob.setZza(0.0F);
		}
	}
}
