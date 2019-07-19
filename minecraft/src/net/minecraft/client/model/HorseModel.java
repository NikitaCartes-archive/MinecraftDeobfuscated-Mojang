package net.minecraft.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

@Environment(EnvType.CLIENT)
public class HorseModel<T extends AbstractHorse> extends EntityModel<T> {
	protected final ModelPart body;
	protected final ModelPart headParts;
	private final ModelPart leg1A;
	private final ModelPart leg2A;
	private final ModelPart leg3A;
	private final ModelPart leg4A;
	private final ModelPart tail;
	private final ModelPart[] saddleParts;
	private final ModelPart[] ridingParts;

	public HorseModel(float f) {
		this.texWidth = 64;
		this.texHeight = 64;
		this.body = new ModelPart(this, 0, 32);
		this.body.addBox(-5.0F, -8.0F, -17.0F, 10, 10, 22, 0.05F);
		this.body.setPos(0.0F, 11.0F, 5.0F);
		this.headParts = new ModelPart(this, 0, 35);
		this.headParts.addBox(-2.05F, -6.0F, -2.0F, 4, 12, 7);
		this.headParts.xRot = (float) (Math.PI / 6);
		ModelPart modelPart = new ModelPart(this, 0, 13);
		modelPart.addBox(-3.0F, -11.0F, -2.0F, 6, 5, 7, f);
		ModelPart modelPart2 = new ModelPart(this, 56, 36);
		modelPart2.addBox(-1.0F, -11.0F, 5.01F, 2, 16, 2, f);
		ModelPart modelPart3 = new ModelPart(this, 0, 25);
		modelPart3.addBox(-2.0F, -11.0F, -7.0F, 4, 5, 5, f);
		this.headParts.addChild(modelPart);
		this.headParts.addChild(modelPart2);
		this.headParts.addChild(modelPart3);
		this.addEarModels(this.headParts);
		this.leg1A = new ModelPart(this, 48, 21);
		this.leg1A.mirror = true;
		this.leg1A.addBox(-3.0F, -1.01F, -1.0F, 4, 11, 4, f);
		this.leg1A.setPos(4.0F, 14.0F, 7.0F);
		this.leg2A = new ModelPart(this, 48, 21);
		this.leg2A.addBox(-1.0F, -1.01F, -1.0F, 4, 11, 4, f);
		this.leg2A.setPos(-4.0F, 14.0F, 7.0F);
		this.leg3A = new ModelPart(this, 48, 21);
		this.leg3A.mirror = true;
		this.leg3A.addBox(-3.0F, -1.01F, -1.9F, 4, 11, 4, f);
		this.leg3A.setPos(4.0F, 6.0F, -12.0F);
		this.leg4A = new ModelPart(this, 48, 21);
		this.leg4A.addBox(-1.0F, -1.01F, -1.9F, 4, 11, 4, f);
		this.leg4A.setPos(-4.0F, 6.0F, -12.0F);
		this.tail = new ModelPart(this, 42, 36);
		this.tail.addBox(-1.5F, 0.0F, 0.0F, 3, 14, 4, f);
		this.tail.setPos(0.0F, -5.0F, 2.0F);
		this.tail.xRot = (float) (Math.PI / 6);
		this.body.addChild(this.tail);
		ModelPart modelPart4 = new ModelPart(this, 26, 0);
		modelPart4.addBox(-5.0F, -8.0F, -9.0F, 10, 9, 9, 0.5F);
		this.body.addChild(modelPart4);
		ModelPart modelPart5 = new ModelPart(this, 29, 5);
		modelPart5.addBox(2.0F, -9.0F, -6.0F, 1, 2, 2, f);
		this.headParts.addChild(modelPart5);
		ModelPart modelPart6 = new ModelPart(this, 29, 5);
		modelPart6.addBox(-3.0F, -9.0F, -6.0F, 1, 2, 2, f);
		this.headParts.addChild(modelPart6);
		ModelPart modelPart7 = new ModelPart(this, 32, 2);
		modelPart7.addBox(3.1F, -6.0F, -8.0F, 0, 3, 16, f);
		modelPart7.xRot = (float) (-Math.PI / 6);
		this.headParts.addChild(modelPart7);
		ModelPart modelPart8 = new ModelPart(this, 32, 2);
		modelPart8.addBox(-3.1F, -6.0F, -8.0F, 0, 3, 16, f);
		modelPart8.xRot = (float) (-Math.PI / 6);
		this.headParts.addChild(modelPart8);
		ModelPart modelPart9 = new ModelPart(this, 1, 1);
		modelPart9.addBox(-3.0F, -11.0F, -1.9F, 6, 5, 6, 0.2F);
		this.headParts.addChild(modelPart9);
		ModelPart modelPart10 = new ModelPart(this, 19, 0);
		modelPart10.addBox(-2.0F, -11.0F, -4.0F, 4, 5, 2, 0.2F);
		this.headParts.addChild(modelPart10);
		this.saddleParts = new ModelPart[]{modelPart4, modelPart5, modelPart6, modelPart9, modelPart10};
		this.ridingParts = new ModelPart[]{modelPart7, modelPart8};
	}

	protected void addEarModels(ModelPart modelPart) {
		ModelPart modelPart2 = new ModelPart(this, 19, 16);
		modelPart2.addBox(0.55F, -13.0F, 4.0F, 2, 3, 1, -0.001F);
		ModelPart modelPart3 = new ModelPart(this, 19, 16);
		modelPart3.addBox(-2.55F, -13.0F, 4.0F, 2, 3, 1, -0.001F);
		modelPart.addChild(modelPart2);
		modelPart.addChild(modelPart3);
	}

	public void render(T abstractHorse, float f, float g, float h, float i, float j, float k) {
		boolean bl = abstractHorse.isBaby();
		float l = abstractHorse.getScale();
		boolean bl2 = abstractHorse.isSaddled();
		boolean bl3 = abstractHorse.isVehicle();

		for (ModelPart modelPart : this.saddleParts) {
			modelPart.visible = bl2;
		}

		for (ModelPart modelPart : this.ridingParts) {
			modelPart.visible = bl3 && bl2;
		}

		if (bl) {
			GlStateManager.pushMatrix();
			GlStateManager.scalef(l, 0.5F + l * 0.5F, l);
			GlStateManager.translatef(0.0F, 0.95F * (1.0F - l), 0.0F);
		}

		this.leg1A.render(k);
		this.leg2A.render(k);
		this.leg3A.render(k);
		this.leg4A.render(k);
		if (bl) {
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.scalef(l, l, l);
			GlStateManager.translatef(0.0F, 2.3F * (1.0F - l), 0.0F);
		}

		this.body.render(k);
		if (bl) {
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			float m = l + 0.1F * l;
			GlStateManager.scalef(m, m, m);
			GlStateManager.translatef(0.0F, 2.25F * (1.0F - m), 0.1F * (1.4F - m));
		}

		this.headParts.render(k);
		if (bl) {
			GlStateManager.popMatrix();
		}
	}

	public void prepareMobModel(T abstractHorse, float f, float g, float h) {
		super.prepareMobModel(abstractHorse, f, g, h);
		float i = this.rotlerp(abstractHorse.yBodyRotO, abstractHorse.yBodyRot, h);
		float j = this.rotlerp(abstractHorse.yHeadRotO, abstractHorse.yHeadRot, h);
		float k = Mth.lerp(h, abstractHorse.xRotO, abstractHorse.xRot);
		float l = j - i;
		float m = k * (float) (Math.PI / 180.0);
		if (l > 20.0F) {
			l = 20.0F;
		}

		if (l < -20.0F) {
			l = -20.0F;
		}

		if (g > 0.2F) {
			m += Mth.cos(f * 0.4F) * 0.15F * g;
		}

		float n = abstractHorse.getEatAnim(h);
		float o = abstractHorse.getStandAnim(h);
		float p = 1.0F - o;
		float q = abstractHorse.getMouthAnim(h);
		boolean bl = abstractHorse.tailCounter != 0;
		float r = (float)abstractHorse.tickCount + h;
		this.headParts.y = 4.0F;
		this.headParts.z = -12.0F;
		this.body.xRot = 0.0F;
		this.headParts.xRot = (float) (Math.PI / 6) + m;
		this.headParts.yRot = l * (float) (Math.PI / 180.0);
		float s = abstractHorse.isInWater() ? 0.2F : 1.0F;
		float t = Mth.cos(s * f * 0.6662F + (float) Math.PI);
		float u = t * 0.8F * g;
		float v = (1.0F - Math.max(o, n)) * ((float) (Math.PI / 6) + m + q * Mth.sin(r) * 0.05F);
		this.headParts.xRot = o * ((float) (Math.PI / 12) + m) + n * (2.1816616F + Mth.sin(r) * 0.05F) + v;
		this.headParts.yRot = o * l * (float) (Math.PI / 180.0) + (1.0F - Math.max(o, n)) * this.headParts.yRot;
		this.headParts.y = o * -4.0F + n * 11.0F + (1.0F - Math.max(o, n)) * this.headParts.y;
		this.headParts.z = o * -4.0F + n * -12.0F + (1.0F - Math.max(o, n)) * this.headParts.z;
		this.body.xRot = o * (float) (-Math.PI / 4) + p * this.body.xRot;
		float w = (float) (Math.PI / 12) * o;
		float x = Mth.cos(r * 0.6F + (float) Math.PI);
		this.leg3A.y = 2.0F * o + 14.0F * p;
		this.leg3A.z = -6.0F * o - 10.0F * p;
		this.leg4A.y = this.leg3A.y;
		this.leg4A.z = this.leg3A.z;
		float y = ((float) (-Math.PI / 3) + x) * o + u * p;
		float z = ((float) (-Math.PI / 3) - x) * o - u * p;
		this.leg1A.xRot = w - t * 0.5F * g * p;
		this.leg2A.xRot = w + t * 0.5F * g * p;
		this.leg3A.xRot = y;
		this.leg4A.xRot = z;
		this.tail.xRot = (float) (Math.PI / 6) + g * 0.75F;
		this.tail.y = -5.0F + g;
		this.tail.z = 2.0F + g * 2.0F;
		if (bl) {
			this.tail.yRot = Mth.cos(r * 0.7F);
		} else {
			this.tail.yRot = 0.0F;
		}
	}

	private float rotlerp(float f, float g, float h) {
		float i = g - f;

		while (i < -180.0F) {
			i += 360.0F;
		}

		while (i >= 180.0F) {
			i -= 360.0F;
		}

		return f + h * i;
	}
}
