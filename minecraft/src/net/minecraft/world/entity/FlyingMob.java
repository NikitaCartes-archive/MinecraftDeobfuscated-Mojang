package net.minecraft.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class FlyingMob extends Mob {
	protected FlyingMob(EntityType<? extends FlyingMob> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public void causeFallDamage(float f, float g) {
	}

	@Override
	protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
	}

	@Override
	public void travel(Vec3 vec3) {
		if (this.isInWater()) {
			this.moveRelative(0.02F, vec3);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.8F));
		} else if (this.isInLava()) {
			this.moveRelative(0.02F, vec3);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
		} else {
			float f = 0.91F;
			if (this.onGround) {
				f = this.level.getBlockState(new BlockPos(this.x, this.getBoundingBox().minY - 1.0, this.z)).getBlock().getFriction() * 0.91F;
			}

			float g = 0.16277137F / (f * f * f);
			f = 0.91F;
			if (this.onGround) {
				f = this.level.getBlockState(new BlockPos(this.x, this.getBoundingBox().minY - 1.0, this.z)).getBlock().getFriction() * 0.91F;
			}

			this.moveRelative(this.onGround ? 0.1F * g : 0.02F, vec3);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale((double)f));
		}

		this.animationSpeedOld = this.animationSpeed;
		double d = this.x - this.xo;
		double e = this.z - this.zo;
		float h = Mth.sqrt(d * d + e * e) * 4.0F;
		if (h > 1.0F) {
			h = 1.0F;
		}

		this.animationSpeed = this.animationSpeed + (h - this.animationSpeed) * 0.4F;
		this.animationPosition = this.animationPosition + this.animationSpeed;
	}

	@Override
	public boolean onLadder() {
		return false;
	}
}
