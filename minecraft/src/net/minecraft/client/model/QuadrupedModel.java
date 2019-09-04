package net.minecraft.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class QuadrupedModel<T extends Entity> extends EntityModel<T> {
	protected ModelPart head;
	protected ModelPart body;
	protected ModelPart leg0;
	protected ModelPart leg1;
	protected ModelPart leg2;
	protected ModelPart leg3;
	protected float yHeadOffs = 8.0F;
	protected float zHeadOffs = 4.0F;

	public QuadrupedModel(int i, float f) {
		this.head = new ModelPart(this, 0, 0);
		this.head.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8, f);
		this.head.setPos(0.0F, (float)(18 - i), -6.0F);
		this.body = new ModelPart(this, 28, 8);
		this.body.addBox(-5.0F, -10.0F, -7.0F, 10, 16, 8, f);
		this.body.setPos(0.0F, (float)(17 - i), 2.0F);
		this.leg0 = new ModelPart(this, 0, 16);
		this.leg0.addBox(-2.0F, 0.0F, -2.0F, 4, i, 4, f);
		this.leg0.setPos(-3.0F, (float)(24 - i), 7.0F);
		this.leg1 = new ModelPart(this, 0, 16);
		this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4, i, 4, f);
		this.leg1.setPos(3.0F, (float)(24 - i), 7.0F);
		this.leg2 = new ModelPart(this, 0, 16);
		this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4, i, 4, f);
		this.leg2.setPos(-3.0F, (float)(24 - i), -5.0F);
		this.leg3 = new ModelPart(this, 0, 16);
		this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4, i, 4, f);
		this.leg3.setPos(3.0F, (float)(24 - i), -5.0F);
	}

	@Override
	public void render(T entity, float f, float g, float h, float i, float j, float k) {
		this.setupAnim(entity, f, g, h, i, j, k);
		if (this.young) {
			float l = 2.0F;
			RenderSystem.pushMatrix();
			RenderSystem.translatef(0.0F, this.yHeadOffs * k, this.zHeadOffs * k);
			this.head.render(k);
			RenderSystem.popMatrix();
			RenderSystem.pushMatrix();
			RenderSystem.scalef(0.5F, 0.5F, 0.5F);
			RenderSystem.translatef(0.0F, 24.0F * k, 0.0F);
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

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
		this.head.xRot = j * (float) (Math.PI / 180.0);
		this.head.yRot = i * (float) (Math.PI / 180.0);
		this.body.xRot = (float) (Math.PI / 2);
		this.leg0.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
		this.leg1.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g;
		this.leg2.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g;
		this.leg3.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
	}
}
