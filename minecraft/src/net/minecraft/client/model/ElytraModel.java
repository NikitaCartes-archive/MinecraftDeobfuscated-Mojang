package net.minecraft.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ElytraModel<T extends LivingEntity> extends EntityModel<T> {
	private final ModelPart rightWing;
	private final ModelPart leftWing = new ModelPart(this, 22, 0);

	public ElytraModel() {
		this.leftWing.addBox(-10.0F, 0.0F, 0.0F, 10, 20, 2, 1.0F);
		this.rightWing = new ModelPart(this, 22, 0);
		this.rightWing.mirror = true;
		this.rightWing.addBox(0.0F, 0.0F, 0.0F, 10, 20, 2, 1.0F);
	}

	public void render(T livingEntity, float f, float g, float h, float i, float j, float k) {
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableCull();
		if (livingEntity.isBaby()) {
			GlStateManager.pushMatrix();
			GlStateManager.scalef(0.5F, 0.5F, 0.5F);
			GlStateManager.translatef(0.0F, 1.5F, -0.1F);
			this.leftWing.render(k);
			this.rightWing.render(k);
			GlStateManager.popMatrix();
		} else {
			this.leftWing.render(k);
			this.rightWing.render(k);
		}
	}

	public void setupAnim(T livingEntity, float f, float g, float h, float i, float j, float k) {
		super.setupAnim(livingEntity, f, g, h, i, j, k);
		float l = (float) (Math.PI / 12);
		float m = (float) (-Math.PI / 12);
		float n = 0.0F;
		float o = 0.0F;
		if (livingEntity.isFallFlying()) {
			float p = 1.0F;
			Vec3 vec3 = livingEntity.getDeltaMovement();
			if (vec3.y < 0.0) {
				Vec3 vec32 = vec3.normalize();
				p = 1.0F - (float)Math.pow(-vec32.y, 1.5);
			}

			l = p * (float) (Math.PI / 9) + (1.0F - p) * l;
			m = p * (float) (-Math.PI / 2) + (1.0F - p) * m;
		} else if (livingEntity.isVisuallySneaking()) {
			l = (float) (Math.PI * 2.0 / 9.0);
			m = (float) (-Math.PI / 4);
			n = 3.0F;
			o = 0.08726646F;
		}

		this.leftWing.x = 5.0F;
		this.leftWing.y = n;
		if (livingEntity instanceof AbstractClientPlayer) {
			AbstractClientPlayer abstractClientPlayer = (AbstractClientPlayer)livingEntity;
			abstractClientPlayer.elytraRotX = (float)((double)abstractClientPlayer.elytraRotX + (double)(l - abstractClientPlayer.elytraRotX) * 0.1);
			abstractClientPlayer.elytraRotY = (float)((double)abstractClientPlayer.elytraRotY + (double)(o - abstractClientPlayer.elytraRotY) * 0.1);
			abstractClientPlayer.elytraRotZ = (float)((double)abstractClientPlayer.elytraRotZ + (double)(m - abstractClientPlayer.elytraRotZ) * 0.1);
			this.leftWing.xRot = abstractClientPlayer.elytraRotX;
			this.leftWing.yRot = abstractClientPlayer.elytraRotY;
			this.leftWing.zRot = abstractClientPlayer.elytraRotZ;
		} else {
			this.leftWing.xRot = l;
			this.leftWing.zRot = m;
			this.leftWing.yRot = o;
		}

		this.rightWing.x = -this.leftWing.x;
		this.rightWing.yRot = -this.leftWing.yRot;
		this.rightWing.y = this.leftWing.y;
		this.rightWing.xRot = this.leftWing.xRot;
		this.rightWing.zRot = -this.leftWing.zRot;
	}
}
