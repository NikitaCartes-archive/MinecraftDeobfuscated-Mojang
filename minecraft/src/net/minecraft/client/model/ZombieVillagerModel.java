package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Zombie;

@Environment(EnvType.CLIENT)
public class ZombieVillagerModel<T extends Zombie> extends HumanoidModel<T> implements VillagerHeadModel {
	private ModelPart hatRim;

	public ZombieVillagerModel(float f, boolean bl) {
		super(f, 0.0F, 64, bl ? 32 : 64);
		if (bl) {
			this.head = new ModelPart(this, 0, 0);
			this.head.addBox(-4.0F, -10.0F, -4.0F, 8.0F, 8.0F, 8.0F, f);
			this.body = new ModelPart(this, 16, 16);
			this.body.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, f + 0.1F);
			this.rightLeg = new ModelPart(this, 0, 16);
			this.rightLeg.setPos(-2.0F, 12.0F, 0.0F);
			this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, f + 0.1F);
			this.leftLeg = new ModelPart(this, 0, 16);
			this.leftLeg.mirror = true;
			this.leftLeg.setPos(2.0F, 12.0F, 0.0F);
			this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, f + 0.1F);
		} else {
			this.head = new ModelPart(this, 0, 0);
			this.head.texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, f);
			this.head.texOffs(24, 0).addBox(-1.0F, -3.0F, -6.0F, 2.0F, 4.0F, 2.0F, f);
			this.hat = new ModelPart(this, 32, 0);
			this.hat.addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, f + 0.5F);
			this.hatRim = new ModelPart(this);
			this.hatRim.texOffs(30, 47).addBox(-8.0F, -8.0F, -6.0F, 16.0F, 16.0F, 1.0F, f);
			this.hatRim.xRot = (float) (-Math.PI / 2);
			this.hat.addChild(this.hatRim);
			this.body = new ModelPart(this, 16, 20);
			this.body.addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F, f);
			this.body.texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 18.0F, 6.0F, f + 0.05F);
			this.rightArm = new ModelPart(this, 44, 22);
			this.rightArm.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
			this.rightArm.setPos(-5.0F, 2.0F, 0.0F);
			this.leftArm = new ModelPart(this, 44, 22);
			this.leftArm.mirror = true;
			this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
			this.leftArm.setPos(5.0F, 2.0F, 0.0F);
			this.rightLeg = new ModelPart(this, 0, 22);
			this.rightLeg.setPos(-2.0F, 12.0F, 0.0F);
			this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
			this.leftLeg = new ModelPart(this, 0, 22);
			this.leftLeg.mirror = true;
			this.leftLeg.setPos(2.0F, 12.0F, 0.0F);
			this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
		}
	}

	public void setupAnim(T zombie, float f, float g, float h, float i, float j) {
		super.setupAnim(zombie, f, g, h, i, j);
		float k = Mth.sin(this.attackTime * (float) Math.PI);
		float l = Mth.sin((1.0F - (1.0F - this.attackTime) * (1.0F - this.attackTime)) * (float) Math.PI);
		this.rightArm.zRot = 0.0F;
		this.leftArm.zRot = 0.0F;
		this.rightArm.yRot = -(0.1F - k * 0.6F);
		this.leftArm.yRot = 0.1F - k * 0.6F;
		float m = (float) -Math.PI / (zombie.isAggressive() ? 1.5F : 2.25F);
		this.rightArm.xRot = m;
		this.leftArm.xRot = m;
		this.rightArm.xRot += k * 1.2F - l * 0.4F;
		this.leftArm.xRot += k * 1.2F - l * 0.4F;
		this.rightArm.zRot = this.rightArm.zRot + Mth.cos(h * 0.09F) * 0.05F + 0.05F;
		this.leftArm.zRot = this.leftArm.zRot - (Mth.cos(h * 0.09F) * 0.05F + 0.05F);
		this.rightArm.xRot = this.rightArm.xRot + Mth.sin(h * 0.067F) * 0.05F;
		this.leftArm.xRot = this.leftArm.xRot - Mth.sin(h * 0.067F) * 0.05F;
	}

	@Override
	public void hatVisible(boolean bl) {
		this.head.visible = bl;
		this.hat.visible = bl;
		this.hatRim.visible = bl;
	}
}
