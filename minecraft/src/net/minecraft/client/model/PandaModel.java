package net.minecraft.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Panda;

@Environment(EnvType.CLIENT)
public class PandaModel<T extends Panda> extends QuadrupedModel<T> {
	private float sitAmount;
	private float lieOnBackAmount;
	private float rollAmount;

	public PandaModel(int i, float f) {
		super(i, f);
		this.texWidth = 64;
		this.texHeight = 64;
		this.head = new ModelPart(this, 0, 6);
		this.head.addBox(-6.5F, -5.0F, -4.0F, 13, 10, 9);
		this.head.setPos(0.0F, 11.5F, -17.0F);
		this.head.texOffs(45, 16).addBox(-3.5F, 0.0F, -6.0F, 7, 5, 2);
		this.head.texOffs(52, 25).addBox(-8.5F, -8.0F, -1.0F, 5, 4, 1);
		this.head.texOffs(52, 25).addBox(3.5F, -8.0F, -1.0F, 5, 4, 1);
		this.body = new ModelPart(this, 0, 25);
		this.body.addBox(-9.5F, -13.0F, -6.5F, 19, 26, 13);
		this.body.setPos(0.0F, 10.0F, 0.0F);
		int j = 9;
		int k = 6;
		this.leg0 = new ModelPart(this, 40, 0);
		this.leg0.addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6);
		this.leg0.setPos(-5.5F, 15.0F, 9.0F);
		this.leg1 = new ModelPart(this, 40, 0);
		this.leg1.addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6);
		this.leg1.setPos(5.5F, 15.0F, 9.0F);
		this.leg2 = new ModelPart(this, 40, 0);
		this.leg2.addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6);
		this.leg2.setPos(-5.5F, 15.0F, -9.0F);
		this.leg3 = new ModelPart(this, 40, 0);
		this.leg3.addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6);
		this.leg3.setPos(5.5F, 15.0F, -9.0F);
	}

	public void prepareMobModel(T panda, float f, float g, float h) {
		super.prepareMobModel(panda, f, g, h);
		this.sitAmount = panda.getSitAmount(h);
		this.lieOnBackAmount = panda.getLieOnBackAmount(h);
		this.rollAmount = panda.isBaby() ? 0.0F : panda.getRollAmount(h);
	}

	public void setupAnim(T panda, float f, float g, float h, float i, float j, float k) {
		super.setupAnim(panda, f, g, h, i, j, k);
		boolean bl = panda.getUnhappyCounter() > 0;
		boolean bl2 = panda.isSneezing();
		int l = panda.getSneezeCounter();
		boolean bl3 = panda.isEating();
		boolean bl4 = panda.isScared();
		if (bl) {
			this.head.yRot = 0.35F * Mth.sin(0.6F * h);
			this.head.zRot = 0.35F * Mth.sin(0.6F * h);
			this.leg2.xRot = -0.75F * Mth.sin(0.3F * h);
			this.leg3.xRot = 0.75F * Mth.sin(0.3F * h);
		} else {
			this.head.zRot = 0.0F;
		}

		if (bl2) {
			if (l < 15) {
				this.head.xRot = (float) (-Math.PI / 4) * (float)l / 14.0F;
			} else if (l < 20) {
				float m = (float)((l - 15) / 5);
				this.head.xRot = (float) (-Math.PI / 4) + (float) (Math.PI / 4) * m;
			}
		}

		if (this.sitAmount > 0.0F) {
			this.body.xRot = ModelUtils.rotlerpRad(this.body.xRot, 1.7407963F, this.sitAmount);
			this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, (float) (Math.PI / 2), this.sitAmount);
			this.leg2.zRot = -0.27079642F;
			this.leg3.zRot = 0.27079642F;
			this.leg0.zRot = 0.5707964F;
			this.leg1.zRot = -0.5707964F;
			if (bl3) {
				this.head.xRot = (float) (Math.PI / 2) + 0.2F * Mth.sin(h * 0.6F);
				this.leg2.xRot = -0.4F - 0.2F * Mth.sin(h * 0.6F);
				this.leg3.xRot = -0.4F - 0.2F * Mth.sin(h * 0.6F);
			}

			if (bl4) {
				this.head.xRot = 2.1707964F;
				this.leg2.xRot = -0.9F;
				this.leg3.xRot = -0.9F;
			}
		} else {
			this.leg0.zRot = 0.0F;
			this.leg1.zRot = 0.0F;
			this.leg2.zRot = 0.0F;
			this.leg3.zRot = 0.0F;
		}

		if (this.lieOnBackAmount > 0.0F) {
			this.leg0.xRot = -0.6F * Mth.sin(h * 0.15F);
			this.leg1.xRot = 0.6F * Mth.sin(h * 0.15F);
			this.leg2.xRot = 0.3F * Mth.sin(h * 0.25F);
			this.leg3.xRot = -0.3F * Mth.sin(h * 0.25F);
			this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, (float) (Math.PI / 2), this.lieOnBackAmount);
		}

		if (this.rollAmount > 0.0F) {
			this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, 2.0561945F, this.rollAmount);
			this.leg0.xRot = -0.5F * Mth.sin(h * 0.5F);
			this.leg1.xRot = 0.5F * Mth.sin(h * 0.5F);
			this.leg2.xRot = 0.5F * Mth.sin(h * 0.5F);
			this.leg3.xRot = -0.5F * Mth.sin(h * 0.5F);
		}
	}

	public void render(T panda, float f, float g, float h, float i, float j, float k) {
		this.setupAnim(panda, f, g, h, i, j, k);
		if (this.young) {
			float l = 3.0F;
			RenderSystem.pushMatrix();
			RenderSystem.translatef(0.0F, this.yHeadOffs * k, this.zHeadOffs * k);
			RenderSystem.popMatrix();
			RenderSystem.pushMatrix();
			float m = 0.6F;
			RenderSystem.scalef(0.5555555F, 0.5555555F, 0.5555555F);
			RenderSystem.translatef(0.0F, 23.0F * k, 0.3F);
			this.head.render(k);
			RenderSystem.popMatrix();
			RenderSystem.pushMatrix();
			RenderSystem.scalef(0.33333334F, 0.33333334F, 0.33333334F);
			RenderSystem.translatef(0.0F, 49.0F * k, 0.0F);
			this.body.render(k);
			this.leg0.render(k);
			this.leg1.render(k);
			this.leg2.render(k);
			this.leg3.render(k);
			RenderSystem.popMatrix();
		} else {
			this.head.render(k);
			this.body.render(k);
			this.leg0.render(k);
			this.leg1.render(k);
			this.leg2.render(k);
			this.leg3.render(k);
		}
	}
}
