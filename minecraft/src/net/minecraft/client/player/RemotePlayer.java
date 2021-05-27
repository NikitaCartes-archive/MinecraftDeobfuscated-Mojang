package net.minecraft.client.player;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;

@Environment(EnvType.CLIENT)
public class RemotePlayer extends AbstractClientPlayer {
	public RemotePlayer(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
		this.maxUpStep = 1.0F;
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
		this.calculateEntityAnimation(this, false);
	}

	@Override
	public void aiStep() {
		if (this.lerpSteps > 0) {
			double d = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
			double e = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
			double f = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
			this.setYRot(this.getYRot() + (float)Mth.wrapDegrees(this.lerpYRot - (double)this.getYRot()) / (float)this.lerpSteps);
			this.setXRot(this.getXRot() + (float)(this.lerpXRot - (double)this.getXRot()) / (float)this.lerpSteps);
			this.lerpSteps--;
			this.setPos(d, e, f);
			this.setRot(this.getYRot(), this.getXRot());
		}

		if (this.lerpHeadSteps > 0) {
			this.yHeadRot = (float)((double)this.yHeadRot + Mth.wrapDegrees(this.lyHeadRot - (double)this.yHeadRot) / (double)this.lerpHeadSteps);
			this.lerpHeadSteps--;
		}

		this.oBob = this.bob;
		this.updateSwingTime();
		float g;
		if (this.onGround && !this.isDeadOrDying()) {
			g = (float)Math.min(0.1, this.getDeltaMovement().horizontalDistance());
		} else {
			g = 0.0F;
		}

		this.bob = this.bob + (g - this.bob) * 0.4F;
		this.level.getProfiler().push("push");
		this.pushEntities();
		this.level.getProfiler().pop();
	}

	@Override
	protected void updatePlayerPose() {
	}

	@Override
	public void sendMessage(Component component, UUID uUID) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!minecraft.isBlocked(uUID)) {
			minecraft.gui.getChat().addMessage(component);
		}
	}
}
