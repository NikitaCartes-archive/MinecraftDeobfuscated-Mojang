package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.IronGolem;

@Environment(EnvType.CLIENT)
public class IronGolemModel<T extends IronGolem> extends EntityModel<T> {
	private final ModelPart head;
	private final ModelPart body;
	public final ModelPart arm0;
	private final ModelPart arm1;
	private final ModelPart leg0;
	private final ModelPart leg1;

	public IronGolemModel() {
		this(0.0F);
	}

	public IronGolemModel(float f) {
		this(f, -7.0F);
	}

	public IronGolemModel(float f, float g) {
		int i = 128;
		int j = 128;
		this.head = new ModelPart(this).setTexSize(128, 128);
		this.head.setPos(0.0F, 0.0F + g, -2.0F);
		this.head.texOffs(0, 0).addBox(-4.0F, -12.0F, -5.5F, 8, 10, 8, f);
		this.head.texOffs(24, 0).addBox(-1.0F, -5.0F, -7.5F, 2, 4, 2, f);
		this.body = new ModelPart(this).setTexSize(128, 128);
		this.body.setPos(0.0F, 0.0F + g, 0.0F);
		this.body.texOffs(0, 40).addBox(-9.0F, -2.0F, -6.0F, 18, 12, 11, f);
		this.body.texOffs(0, 70).addBox(-4.5F, 10.0F, -3.0F, 9, 5, 6, f + 0.5F);
		this.arm0 = new ModelPart(this).setTexSize(128, 128);
		this.arm0.setPos(0.0F, -7.0F, 0.0F);
		this.arm0.texOffs(60, 21).addBox(-13.0F, -2.5F, -3.0F, 4, 30, 6, f);
		this.arm1 = new ModelPart(this).setTexSize(128, 128);
		this.arm1.setPos(0.0F, -7.0F, 0.0F);
		this.arm1.texOffs(60, 58).addBox(9.0F, -2.5F, -3.0F, 4, 30, 6, f);
		this.leg0 = new ModelPart(this, 0, 22).setTexSize(128, 128);
		this.leg0.setPos(-4.0F, 18.0F + g, 0.0F);
		this.leg0.texOffs(37, 0).addBox(-3.5F, -3.0F, -3.0F, 6, 16, 5, f);
		this.leg1 = new ModelPart(this, 0, 22).setTexSize(128, 128);
		this.leg1.mirror = true;
		this.leg1.texOffs(60, 0).setPos(5.0F, 18.0F + g, 0.0F);
		this.leg1.addBox(-3.5F, -3.0F, -3.0F, 6, 16, 5, f);
	}

	public void render(T ironGolem, float f, float g, float h, float i, float j, float k) {
		this.setupAnim(ironGolem, f, g, h, i, j, k);
		this.head.render(k);
		this.body.render(k);
		this.leg0.render(k);
		this.leg1.render(k);
		this.arm0.render(k);
		this.arm1.render(k);
	}

	public void setupAnim(T ironGolem, float f, float g, float h, float i, float j, float k) {
		this.head.yRot = i * (float) (Math.PI / 180.0);
		this.head.xRot = j * (float) (Math.PI / 180.0);
		this.leg0.xRot = -1.5F * this.triangleWave(f, 13.0F) * g;
		this.leg1.xRot = 1.5F * this.triangleWave(f, 13.0F) * g;
		this.leg0.yRot = 0.0F;
		this.leg1.yRot = 0.0F;
	}

	public void prepareMobModel(T ironGolem, float f, float g, float h) {
		int i = ironGolem.getAttackAnimationTick();
		if (i > 0) {
			this.arm0.xRot = -2.0F + 1.5F * this.triangleWave((float)i - h, 10.0F);
			this.arm1.xRot = -2.0F + 1.5F * this.triangleWave((float)i - h, 10.0F);
		} else {
			int j = ironGolem.getOfferFlowerTick();
			if (j > 0) {
				this.arm0.xRot = -0.8F + 0.025F * this.triangleWave((float)j, 70.0F);
				this.arm1.xRot = 0.0F;
			} else {
				this.arm0.xRot = (-0.2F + 1.5F * this.triangleWave(f, 13.0F)) * g;
				this.arm1.xRot = (-0.2F - 1.5F * this.triangleWave(f, 13.0F)) * g;
			}
		}
	}

	private float triangleWave(float f, float g) {
		return (Math.abs(f % g - g * 0.5F) - g * 0.25F) / (g * 0.25F);
	}

	public ModelPart getFlowerHoldingArm() {
		return this.arm0;
	}
}
