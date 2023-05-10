package net.minecraft.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class FlyingMob extends Mob {
	protected FlyingMob(EntityType<? extends FlyingMob> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
	}

	@Override
	public void travel(Vec3 vec3) {
		if (this.isControlledByLocalInstance()) {
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
				if (this.onGround()) {
					f = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.91F;
				}

				float g = 0.16277137F / (f * f * f);
				f = 0.91F;
				if (this.onGround()) {
					f = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.91F;
				}

				this.moveRelative(this.onGround() ? 0.1F * g : 0.02F, vec3);
				this.move(MoverType.SELF, this.getDeltaMovement());
				this.setDeltaMovement(this.getDeltaMovement().scale((double)f));
			}
		}

		this.calculateEntityAnimation(false);
	}

	@Override
	public boolean onClimbable() {
		return false;
	}
}
