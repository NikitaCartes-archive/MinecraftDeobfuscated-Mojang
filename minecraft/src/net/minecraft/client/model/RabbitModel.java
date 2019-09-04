package net.minecraft.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Rabbit;

@Environment(EnvType.CLIENT)
public class RabbitModel<T extends Rabbit> extends EntityModel<T> {
	private final ModelPart rearFootLeft = new ModelPart(this, 26, 24);
	private final ModelPart rearFootRight;
	private final ModelPart haunchLeft;
	private final ModelPart haunchRight;
	private final ModelPart body;
	private final ModelPart frontLegLeft;
	private final ModelPart frontLegRight;
	private final ModelPart head;
	private final ModelPart earRight;
	private final ModelPart earLeft;
	private final ModelPart tail;
	private final ModelPart nose;
	private float jumpRotation;

	public RabbitModel() {
		this.rearFootLeft.addBox(-1.0F, 5.5F, -3.7F, 2, 1, 7);
		this.rearFootLeft.setPos(3.0F, 17.5F, 3.7F);
		this.rearFootLeft.mirror = true;
		this.setRotation(this.rearFootLeft, 0.0F, 0.0F, 0.0F);
		this.rearFootRight = new ModelPart(this, 8, 24);
		this.rearFootRight.addBox(-1.0F, 5.5F, -3.7F, 2, 1, 7);
		this.rearFootRight.setPos(-3.0F, 17.5F, 3.7F);
		this.rearFootRight.mirror = true;
		this.setRotation(this.rearFootRight, 0.0F, 0.0F, 0.0F);
		this.haunchLeft = new ModelPart(this, 30, 15);
		this.haunchLeft.addBox(-1.0F, 0.0F, 0.0F, 2, 4, 5);
		this.haunchLeft.setPos(3.0F, 17.5F, 3.7F);
		this.haunchLeft.mirror = true;
		this.setRotation(this.haunchLeft, (float) (-Math.PI / 9), 0.0F, 0.0F);
		this.haunchRight = new ModelPart(this, 16, 15);
		this.haunchRight.addBox(-1.0F, 0.0F, 0.0F, 2, 4, 5);
		this.haunchRight.setPos(-3.0F, 17.5F, 3.7F);
		this.haunchRight.mirror = true;
		this.setRotation(this.haunchRight, (float) (-Math.PI / 9), 0.0F, 0.0F);
		this.body = new ModelPart(this, 0, 0);
		this.body.addBox(-3.0F, -2.0F, -10.0F, 6, 5, 10);
		this.body.setPos(0.0F, 19.0F, 8.0F);
		this.body.mirror = true;
		this.setRotation(this.body, (float) (-Math.PI / 9), 0.0F, 0.0F);
		this.frontLegLeft = new ModelPart(this, 8, 15);
		this.frontLegLeft.addBox(-1.0F, 0.0F, -1.0F, 2, 7, 2);
		this.frontLegLeft.setPos(3.0F, 17.0F, -1.0F);
		this.frontLegLeft.mirror = true;
		this.setRotation(this.frontLegLeft, (float) (-Math.PI / 18), 0.0F, 0.0F);
		this.frontLegRight = new ModelPart(this, 0, 15);
		this.frontLegRight.addBox(-1.0F, 0.0F, -1.0F, 2, 7, 2);
		this.frontLegRight.setPos(-3.0F, 17.0F, -1.0F);
		this.frontLegRight.mirror = true;
		this.setRotation(this.frontLegRight, (float) (-Math.PI / 18), 0.0F, 0.0F);
		this.head = new ModelPart(this, 32, 0);
		this.head.addBox(-2.5F, -4.0F, -5.0F, 5, 4, 5);
		this.head.setPos(0.0F, 16.0F, -1.0F);
		this.head.mirror = true;
		this.setRotation(this.head, 0.0F, 0.0F, 0.0F);
		this.earRight = new ModelPart(this, 52, 0);
		this.earRight.addBox(-2.5F, -9.0F, -1.0F, 2, 5, 1);
		this.earRight.setPos(0.0F, 16.0F, -1.0F);
		this.earRight.mirror = true;
		this.setRotation(this.earRight, 0.0F, (float) (-Math.PI / 12), 0.0F);
		this.earLeft = new ModelPart(this, 58, 0);
		this.earLeft.addBox(0.5F, -9.0F, -1.0F, 2, 5, 1);
		this.earLeft.setPos(0.0F, 16.0F, -1.0F);
		this.earLeft.mirror = true;
		this.setRotation(this.earLeft, 0.0F, (float) (Math.PI / 12), 0.0F);
		this.tail = new ModelPart(this, 52, 6);
		this.tail.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 2);
		this.tail.setPos(0.0F, 20.0F, 7.0F);
		this.tail.mirror = true;
		this.setRotation(this.tail, -0.3490659F, 0.0F, 0.0F);
		this.nose = new ModelPart(this, 32, 9);
		this.nose.addBox(-0.5F, -2.5F, -5.5F, 1, 1, 1);
		this.nose.setPos(0.0F, 16.0F, -1.0F);
		this.nose.mirror = true;
		this.setRotation(this.nose, 0.0F, 0.0F, 0.0F);
	}

	private void setRotation(ModelPart modelPart, float f, float g, float h) {
		modelPart.xRot = f;
		modelPart.yRot = g;
		modelPart.zRot = h;
	}

	public void render(T rabbit, float f, float g, float h, float i, float j, float k) {
		this.setupAnim(rabbit, f, g, h, i, j, k);
		if (this.young) {
			float l = 1.5F;
			RenderSystem.pushMatrix();
			RenderSystem.scalef(0.56666666F, 0.56666666F, 0.56666666F);
			RenderSystem.translatef(0.0F, 22.0F * k, 2.0F * k);
			this.head.render(k);
			this.earLeft.render(k);
			this.earRight.render(k);
			this.nose.render(k);
			RenderSystem.popMatrix();
			RenderSystem.pushMatrix();
			RenderSystem.scalef(0.4F, 0.4F, 0.4F);
			RenderSystem.translatef(0.0F, 36.0F * k, 0.0F);
			this.rearFootLeft.render(k);
			this.rearFootRight.render(k);
			this.haunchLeft.render(k);
			this.haunchRight.render(k);
			this.body.render(k);
			this.frontLegLeft.render(k);
			this.frontLegRight.render(k);
			this.tail.render(k);
			RenderSystem.popMatrix();
		} else {
			RenderSystem.pushMatrix();
			RenderSystem.scalef(0.6F, 0.6F, 0.6F);
			RenderSystem.translatef(0.0F, 16.0F * k, 0.0F);
			this.rearFootLeft.render(k);
			this.rearFootRight.render(k);
			this.haunchLeft.render(k);
			this.haunchRight.render(k);
			this.body.render(k);
			this.frontLegLeft.render(k);
			this.frontLegRight.render(k);
			this.head.render(k);
			this.earRight.render(k);
			this.earLeft.render(k);
			this.tail.render(k);
			this.nose.render(k);
			RenderSystem.popMatrix();
		}
	}

	public void setupAnim(T rabbit, float f, float g, float h, float i, float j, float k) {
		float l = h - (float)rabbit.tickCount;
		this.nose.xRot = j * (float) (Math.PI / 180.0);
		this.head.xRot = j * (float) (Math.PI / 180.0);
		this.earRight.xRot = j * (float) (Math.PI / 180.0);
		this.earLeft.xRot = j * (float) (Math.PI / 180.0);
		this.nose.yRot = i * (float) (Math.PI / 180.0);
		this.head.yRot = i * (float) (Math.PI / 180.0);
		this.earRight.yRot = this.nose.yRot - (float) (Math.PI / 12);
		this.earLeft.yRot = this.nose.yRot + (float) (Math.PI / 12);
		this.jumpRotation = Mth.sin(rabbit.getJumpCompletion(l) * (float) Math.PI);
		this.haunchLeft.xRot = (this.jumpRotation * 50.0F - 21.0F) * (float) (Math.PI / 180.0);
		this.haunchRight.xRot = (this.jumpRotation * 50.0F - 21.0F) * (float) (Math.PI / 180.0);
		this.rearFootLeft.xRot = this.jumpRotation * 50.0F * (float) (Math.PI / 180.0);
		this.rearFootRight.xRot = this.jumpRotation * 50.0F * (float) (Math.PI / 180.0);
		this.frontLegLeft.xRot = (this.jumpRotation * -40.0F - 11.0F) * (float) (Math.PI / 180.0);
		this.frontLegRight.xRot = (this.jumpRotation * -40.0F - 11.0F) * (float) (Math.PI / 180.0);
	}

	public void prepareMobModel(T rabbit, float f, float g, float h) {
		super.prepareMobModel(rabbit, f, g, h);
		this.jumpRotation = Mth.sin(rabbit.getJumpCompletion(h) * (float) Math.PI);
	}
}
