package net.minecraft.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Turtle;

@Environment(EnvType.CLIENT)
public class TurtleModel<T extends Turtle> extends QuadrupedModel<T> {
	private final ModelPart eggBelly;

	public TurtleModel(float f) {
		super(12, f);
		this.texWidth = 128;
		this.texHeight = 64;
		this.head = new ModelPart(this, 3, 0);
		this.head.addBox(-3.0F, -1.0F, -3.0F, 6, 5, 6, 0.0F);
		this.head.setPos(0.0F, 19.0F, -10.0F);
		this.body = new ModelPart(this);
		this.body.texOffs(7, 37).addBox(-9.5F, 3.0F, -10.0F, 19, 20, 6, 0.0F);
		this.body.texOffs(31, 1).addBox(-5.5F, 3.0F, -13.0F, 11, 18, 3, 0.0F);
		this.body.setPos(0.0F, 11.0F, -10.0F);
		this.eggBelly = new ModelPart(this);
		this.eggBelly.texOffs(70, 33).addBox(-4.5F, 3.0F, -14.0F, 9, 18, 1, 0.0F);
		this.eggBelly.setPos(0.0F, 11.0F, -10.0F);
		int i = 1;
		this.leg0 = new ModelPart(this, 1, 23);
		this.leg0.addBox(-2.0F, 0.0F, 0.0F, 4, 1, 10, 0.0F);
		this.leg0.setPos(-3.5F, 22.0F, 11.0F);
		this.leg1 = new ModelPart(this, 1, 12);
		this.leg1.addBox(-2.0F, 0.0F, 0.0F, 4, 1, 10, 0.0F);
		this.leg1.setPos(3.5F, 22.0F, 11.0F);
		this.leg2 = new ModelPart(this, 27, 30);
		this.leg2.addBox(-13.0F, 0.0F, -2.0F, 13, 1, 5, 0.0F);
		this.leg2.setPos(-5.0F, 21.0F, -4.0F);
		this.leg3 = new ModelPart(this, 27, 24);
		this.leg3.addBox(0.0F, 0.0F, -2.0F, 13, 1, 5, 0.0F);
		this.leg3.setPos(5.0F, 21.0F, -4.0F);
	}

	public void render(T turtle, float f, float g, float h, float i, float j, float k) {
		this.setupAnim(turtle, f, g, h, i, j, k);
		if (this.young) {
			float l = 6.0F;
			GlStateManager.pushMatrix();
			GlStateManager.scalef(0.16666667F, 0.16666667F, 0.16666667F);
			GlStateManager.translatef(0.0F, 120.0F * k, 0.0F);
			this.head.render(k);
			this.body.render(k);
			this.leg0.render(k);
			this.leg1.render(k);
			this.leg2.render(k);
			this.leg3.render(k);
			GlStateManager.popMatrix();
		} else {
			GlStateManager.pushMatrix();
			if (turtle.hasEgg()) {
				GlStateManager.translatef(0.0F, -0.08F, 0.0F);
			}

			this.head.render(k);
			this.body.render(k);
			GlStateManager.pushMatrix();
			this.leg0.render(k);
			this.leg1.render(k);
			GlStateManager.popMatrix();
			this.leg2.render(k);
			this.leg3.render(k);
			if (turtle.hasEgg()) {
				this.eggBelly.render(k);
			}

			GlStateManager.popMatrix();
		}
	}

	public void setupAnim(T turtle, float f, float g, float h, float i, float j, float k) {
		super.setupAnim(turtle, f, g, h, i, j, k);
		this.leg0.xRot = Mth.cos(f * 0.6662F * 0.6F) * 0.5F * g;
		this.leg1.xRot = Mth.cos(f * 0.6662F * 0.6F + (float) Math.PI) * 0.5F * g;
		this.leg2.zRot = Mth.cos(f * 0.6662F * 0.6F + (float) Math.PI) * 0.5F * g;
		this.leg3.zRot = Mth.cos(f * 0.6662F * 0.6F) * 0.5F * g;
		this.leg2.xRot = 0.0F;
		this.leg3.xRot = 0.0F;
		this.leg2.yRot = 0.0F;
		this.leg3.yRot = 0.0F;
		this.leg0.yRot = 0.0F;
		this.leg1.yRot = 0.0F;
		this.eggBelly.xRot = (float) (Math.PI / 2);
		if (!turtle.isInWater() && turtle.onGround) {
			float l = turtle.isLayingEgg() ? 4.0F : 1.0F;
			float m = turtle.isLayingEgg() ? 2.0F : 1.0F;
			float n = 5.0F;
			this.leg2.yRot = Mth.cos(l * f * 5.0F + (float) Math.PI) * 8.0F * g * m;
			this.leg2.zRot = 0.0F;
			this.leg3.yRot = Mth.cos(l * f * 5.0F) * 8.0F * g * m;
			this.leg3.zRot = 0.0F;
			this.leg0.yRot = Mth.cos(f * 5.0F + (float) Math.PI) * 3.0F * g;
			this.leg0.xRot = 0.0F;
			this.leg1.yRot = Mth.cos(f * 5.0F) * 3.0F * g;
			this.leg1.xRot = 0.0F;
		}
	}
}
