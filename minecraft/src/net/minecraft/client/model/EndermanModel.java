package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class EndermanModel<T extends LivingEntity> extends HumanoidModel<T> {
	public boolean carrying;
	public boolean creepy;

	public EndermanModel(ModelPart modelPart) {
		super(modelPart);
	}

	public static LayerDefinition createBodyLayer() {
		float f = -14.0F;
		MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, -14.0F);
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartPose partPose = PartPose.offset(0.0F, -13.0F, 0.0F);
		partDefinition.addOrReplaceChild(
			"hat", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-0.5F)), partPose
		);
		partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), partPose);
		partDefinition.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(32, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F), PartPose.offset(0.0F, -14.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"right_arm", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 30.0F, 2.0F), PartPose.offset(-5.0F, -12.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_arm", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 30.0F, 2.0F), PartPose.offset(5.0F, -12.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"right_leg", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 30.0F, 2.0F), PartPose.offset(-2.0F, -5.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_leg", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 30.0F, 2.0F), PartPose.offset(2.0F, -5.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	@Override
	public void setupAnim(T livingEntity, float f, float g, float h, float i, float j) {
		super.setupAnim(livingEntity, f, g, h, i, j);
		this.head.visible = true;
		int k = -14;
		this.body.xRot = 0.0F;
		this.body.y = -14.0F;
		this.body.z = -0.0F;
		this.rightLeg.xRot -= 0.0F;
		this.leftLeg.xRot -= 0.0F;
		this.rightArm.xRot = (float)((double)this.rightArm.xRot * 0.5);
		this.leftArm.xRot = (float)((double)this.leftArm.xRot * 0.5);
		this.rightLeg.xRot = (float)((double)this.rightLeg.xRot * 0.5);
		this.leftLeg.xRot = (float)((double)this.leftLeg.xRot * 0.5);
		float l = 0.4F;
		if (this.rightArm.xRot > 0.4F) {
			this.rightArm.xRot = 0.4F;
		}

		if (this.leftArm.xRot > 0.4F) {
			this.leftArm.xRot = 0.4F;
		}

		if (this.rightArm.xRot < -0.4F) {
			this.rightArm.xRot = -0.4F;
		}

		if (this.leftArm.xRot < -0.4F) {
			this.leftArm.xRot = -0.4F;
		}

		if (this.rightLeg.xRot > 0.4F) {
			this.rightLeg.xRot = 0.4F;
		}

		if (this.leftLeg.xRot > 0.4F) {
			this.leftLeg.xRot = 0.4F;
		}

		if (this.rightLeg.xRot < -0.4F) {
			this.rightLeg.xRot = -0.4F;
		}

		if (this.leftLeg.xRot < -0.4F) {
			this.leftLeg.xRot = -0.4F;
		}

		if (this.carrying) {
			this.rightArm.xRot = -0.5F;
			this.leftArm.xRot = -0.5F;
			this.rightArm.zRot = 0.05F;
			this.leftArm.zRot = -0.05F;
		}

		this.rightLeg.z = 0.0F;
		this.leftLeg.z = 0.0F;
		this.rightLeg.y = -5.0F;
		this.leftLeg.y = -5.0F;
		this.head.z = -0.0F;
		this.head.y = -13.0F;
		this.hat.x = this.head.x;
		this.hat.y = this.head.y;
		this.hat.z = this.head.z;
		this.hat.xRot = this.head.xRot;
		this.hat.yRot = this.head.yRot;
		this.hat.zRot = this.head.zRot;
		if (this.creepy) {
			float m = 1.0F;
			this.head.y -= 5.0F;
		}

		int n = -14;
		this.rightArm.setPos(-5.0F, -12.0F, 0.0F);
		this.leftArm.setPos(5.0F, -12.0F, 0.0F);
	}
}
