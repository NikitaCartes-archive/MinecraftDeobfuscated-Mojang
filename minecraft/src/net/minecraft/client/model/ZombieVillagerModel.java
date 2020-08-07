package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
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
		AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, zombie.isAggressive(), this.attackTime, h);
	}

	@Override
	public void hatVisible(boolean bl) {
		this.head.visible = bl;
		this.hat.visible = bl;
		this.hatRim.visible = bl;
	}
}
