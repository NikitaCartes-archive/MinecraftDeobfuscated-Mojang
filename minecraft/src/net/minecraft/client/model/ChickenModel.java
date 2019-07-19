package net.minecraft.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class ChickenModel<T extends Entity> extends EntityModel<T> {
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart leg0;
	private final ModelPart leg1;
	private final ModelPart wing0;
	private final ModelPart wing1;
	private final ModelPart beak;
	private final ModelPart redThing;

	public ChickenModel() {
		int i = 16;
		this.head = new ModelPart(this, 0, 0);
		this.head.addBox(-2.0F, -6.0F, -2.0F, 4, 6, 3, 0.0F);
		this.head.setPos(0.0F, 15.0F, -4.0F);
		this.beak = new ModelPart(this, 14, 0);
		this.beak.addBox(-2.0F, -4.0F, -4.0F, 4, 2, 2, 0.0F);
		this.beak.setPos(0.0F, 15.0F, -4.0F);
		this.redThing = new ModelPart(this, 14, 4);
		this.redThing.addBox(-1.0F, -2.0F, -3.0F, 2, 2, 2, 0.0F);
		this.redThing.setPos(0.0F, 15.0F, -4.0F);
		this.body = new ModelPart(this, 0, 9);
		this.body.addBox(-3.0F, -4.0F, -3.0F, 6, 8, 6, 0.0F);
		this.body.setPos(0.0F, 16.0F, 0.0F);
		this.leg0 = new ModelPart(this, 26, 0);
		this.leg0.addBox(-1.0F, 0.0F, -3.0F, 3, 5, 3);
		this.leg0.setPos(-2.0F, 19.0F, 1.0F);
		this.leg1 = new ModelPart(this, 26, 0);
		this.leg1.addBox(-1.0F, 0.0F, -3.0F, 3, 5, 3);
		this.leg1.setPos(1.0F, 19.0F, 1.0F);
		this.wing0 = new ModelPart(this, 24, 13);
		this.wing0.addBox(0.0F, 0.0F, -3.0F, 1, 4, 6);
		this.wing0.setPos(-4.0F, 13.0F, 0.0F);
		this.wing1 = new ModelPart(this, 24, 13);
		this.wing1.addBox(-1.0F, 0.0F, -3.0F, 1, 4, 6);
		this.wing1.setPos(4.0F, 13.0F, 0.0F);
	}

	@Override
	public void render(T entity, float f, float g, float h, float i, float j, float k) {
		this.setupAnim(entity, f, g, h, i, j, k);
		if (this.young) {
			float l = 2.0F;
			GlStateManager.pushMatrix();
			GlStateManager.translatef(0.0F, 5.0F * k, 2.0F * k);
			this.head.render(k);
			this.beak.render(k);
			this.redThing.render(k);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.scalef(0.5F, 0.5F, 0.5F);
			GlStateManager.translatef(0.0F, 24.0F * k, 0.0F);
			this.body.render(k);
			this.leg0.render(k);
			this.leg1.render(k);
			this.wing0.render(k);
			this.wing1.render(k);
			GlStateManager.popMatrix();
		} else {
			this.head.render(k);
			this.beak.render(k);
			this.redThing.render(k);
			this.body.render(k);
			this.leg0.render(k);
			this.leg1.render(k);
			this.wing0.render(k);
			this.wing1.render(k);
		}
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
		this.head.xRot = j * (float) (Math.PI / 180.0);
		this.head.yRot = i * (float) (Math.PI / 180.0);
		this.beak.xRot = this.head.xRot;
		this.beak.yRot = this.head.yRot;
		this.redThing.xRot = this.head.xRot;
		this.redThing.yRot = this.head.yRot;
		this.body.xRot = (float) (Math.PI / 2);
		this.leg0.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
		this.leg1.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g;
		this.wing0.zRot = h;
		this.wing1.zRot = -h;
	}
}
