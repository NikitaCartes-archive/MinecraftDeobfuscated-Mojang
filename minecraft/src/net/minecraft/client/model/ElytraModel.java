package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ElytraModel<T extends LivingEntity> extends AgeableListModel<T> {
	private final ModelPart rightWing;
	private final ModelPart leftWing = new ModelPart(this, 22, 0);

	public ElytraModel() {
		this.leftWing.addBox(-10.0F, 0.0F, 0.0F, 10.0F, 20.0F, 2.0F, 1.0F);
		this.rightWing = new ModelPart(this, 22, 0);
		this.rightWing.mirror = true;
		this.rightWing.addBox(0.0F, 0.0F, 0.0F, 10.0F, 20.0F, 2.0F, 1.0F);
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.<ModelPart>of();
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.<ModelPart>of(this.leftWing, this.rightWing);
	}

	public void setupAnim(T livingEntity, float f, float g, float h, float i, float j) {
		float k = (float) (Math.PI / 12);
		float l = (float) (-Math.PI / 12);
		float m = 0.0F;
		float n = 0.0F;
		if (livingEntity.isFallFlying()) {
			float o = 1.0F;
			Vec3 vec3 = livingEntity.getDeltaMovement();
			if (vec3.y < 0.0) {
				Vec3 vec32 = vec3.normalize();
				o = 1.0F - (float)Math.pow(-vec32.y, 1.5);
			}

			k = o * (float) (Math.PI / 9) + (1.0F - o) * k;
			l = o * (float) (-Math.PI / 2) + (1.0F - o) * l;
		} else if (livingEntity.isCrouching()) {
			k = (float) (Math.PI * 2.0 / 9.0);
			l = (float) (-Math.PI / 4);
			m = 3.0F;
			n = 0.08726646F;
		}

		this.leftWing.x = 5.0F;
		this.leftWing.y = m;
		if (livingEntity instanceof AbstractClientPlayer) {
			AbstractClientPlayer abstractClientPlayer = (AbstractClientPlayer)livingEntity;
			abstractClientPlayer.elytraRotX = (float)((double)abstractClientPlayer.elytraRotX + (double)(k - abstractClientPlayer.elytraRotX) * 0.1);
			abstractClientPlayer.elytraRotY = (float)((double)abstractClientPlayer.elytraRotY + (double)(n - abstractClientPlayer.elytraRotY) * 0.1);
			abstractClientPlayer.elytraRotZ = (float)((double)abstractClientPlayer.elytraRotZ + (double)(l - abstractClientPlayer.elytraRotZ) * 0.1);
			this.leftWing.xRot = abstractClientPlayer.elytraRotX;
			this.leftWing.yRot = abstractClientPlayer.elytraRotY;
			this.leftWing.zRot = abstractClientPlayer.elytraRotZ;
		} else {
			this.leftWing.xRot = k;
			this.leftWing.zRot = l;
			this.leftWing.yRot = n;
		}

		this.rightWing.x = -this.leftWing.x;
		this.rightWing.yRot = -this.leftWing.yRot;
		this.rightWing.y = this.leftWing.y;
		this.rightWing.xRot = this.leftWing.xRot;
		this.rightWing.zRot = -this.leftWing.zRot;
	}
}
