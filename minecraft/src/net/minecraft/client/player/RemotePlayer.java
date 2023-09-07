package net.minecraft.client.player;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class RemotePlayer extends AbstractClientPlayer {
	private Vec3 lerpDeltaMovement = Vec3.ZERO;
	private int lerpDeltaMovementSteps;

	public RemotePlayer(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
		this.setMaxUpStep(1.0F);
		this.noPhysics = true;
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		double e = this.getBoundingBox().getSize() * 10.0;
		if (Double.isNaN(e)) {
			e = 1.0;
		}

		e *= 64.0 * getViewScale();
		return d < e * e;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		return true;
	}

	@Override
	public void tick() {
		super.tick();
		this.calculateEntityAnimation(false);
	}

	@Override
	public void aiStep() {
		if (this.lerpSteps > 0) {
			this.lerpPositionAndRotationStep(this.lerpSteps, this.lerpX, this.lerpY, this.lerpZ, this.lerpYRot, this.lerpXRot);
			this.lerpSteps--;
		}

		if (this.lerpHeadSteps > 0) {
			this.lerpHeadRotationStep(this.lerpHeadSteps, this.lerpYHeadRot);
			this.lerpHeadSteps--;
		}

		if (this.lerpDeltaMovementSteps > 0) {
			this.addDeltaMovement(
				new Vec3(
					(this.lerpDeltaMovement.x - this.getDeltaMovement().x) / (double)this.lerpDeltaMovementSteps,
					(this.lerpDeltaMovement.y - this.getDeltaMovement().y) / (double)this.lerpDeltaMovementSteps,
					(this.lerpDeltaMovement.z - this.getDeltaMovement().z) / (double)this.lerpDeltaMovementSteps
				)
			);
			this.lerpDeltaMovementSteps--;
		}

		this.oBob = this.bob;
		this.updateSwingTime();
		float f;
		if (this.onGround() && !this.isDeadOrDying()) {
			f = (float)Math.min(0.1, this.getDeltaMovement().horizontalDistance());
		} else {
			f = 0.0F;
		}

		this.bob = this.bob + (f - this.bob) * 0.4F;
		this.level().getProfiler().push("push");
		this.pushEntities();
		this.level().getProfiler().pop();
	}

	@Override
	public void lerpMotion(double d, double e, double f) {
		this.lerpDeltaMovement = new Vec3(d, e, f);
		this.lerpDeltaMovementSteps = this.getType().updateInterval() + 1;
	}

	@Override
	protected void updatePlayerPose() {
	}

	@Override
	public void sendSystemMessage(Component component) {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.gui.getChat().addMessage(component);
	}

	@Override
	public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
		super.recreateFromPacket(clientboundAddEntityPacket);
		this.setOldPosAndRot();
	}
}
