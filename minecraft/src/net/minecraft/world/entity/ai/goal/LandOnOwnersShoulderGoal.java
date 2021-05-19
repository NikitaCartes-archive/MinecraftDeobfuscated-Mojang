package net.minecraft.world.entity.ai.goal;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;

public class LandOnOwnersShoulderGoal extends Goal {
	private final ShoulderRidingEntity entity;
	private ServerPlayer owner;
	private boolean isSittingOnShoulder;

	public LandOnOwnersShoulderGoal(ShoulderRidingEntity shoulderRidingEntity) {
		this.entity = shoulderRidingEntity;
	}

	@Override
	public boolean canUse() {
		ServerPlayer serverPlayer = (ServerPlayer)this.entity.getOwner();
		boolean bl = serverPlayer != null
			&& !serverPlayer.isSpectator()
			&& !serverPlayer.getAbilities().flying
			&& !serverPlayer.isInWater()
			&& !serverPlayer.isInPowderSnow;
		return !this.entity.isOrderedToSit() && bl && this.entity.canSitOnShoulder();
	}

	@Override
	public boolean isInterruptable() {
		return !this.isSittingOnShoulder;
	}

	@Override
	public void start() {
		this.owner = (ServerPlayer)this.entity.getOwner();
		this.isSittingOnShoulder = false;
	}

	@Override
	public void tick() {
		if (!this.isSittingOnShoulder && !this.entity.isInSittingPose() && !this.entity.isLeashed()) {
			if (this.entity.getBoundingBox().intersects(this.owner.getBoundingBox())) {
				this.isSittingOnShoulder = this.entity.setEntityOnShoulder(this.owner);
			}
		}
	}
}
