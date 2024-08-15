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
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class EndermanModel<T extends EndermanRenderState> extends HumanoidModel<T> {
	public EndermanModel(ModelPart modelPart) {
		super(modelPart);
	}

	public static LayerDefinition createBodyLayer() {
		float f = -14.0F;
		MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, -14.0F);
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, -13.0F, 0.0F)
		);
		partDefinition2.addOrReplaceChild(
			"hat", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-0.5F)), PartPose.ZERO
		);
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

	public void setupAnim(T endermanRenderState) {
		super.setupAnim(endermanRenderState);
		this.head.visible = true;
		this.rightArm.xRot *= 0.5F;
		this.leftArm.xRot *= 0.5F;
		this.rightLeg.xRot *= 0.5F;
		this.leftLeg.xRot *= 0.5F;
		float f = 0.4F;
		this.rightArm.xRot = Mth.clamp(this.rightArm.xRot, -0.4F, 0.4F);
		this.leftArm.xRot = Mth.clamp(this.leftArm.xRot, -0.4F, 0.4F);
		this.rightLeg.xRot = Mth.clamp(this.rightLeg.xRot, -0.4F, 0.4F);
		this.leftLeg.xRot = Mth.clamp(this.leftLeg.xRot, -0.4F, 0.4F);
		if (endermanRenderState.carriedBlock != null) {
			this.rightArm.xRot = -0.5F;
			this.leftArm.xRot = -0.5F;
			this.rightArm.zRot = 0.05F;
			this.leftArm.zRot = -0.05F;
		}

		if (endermanRenderState.isCreepy) {
			float g = 5.0F;
			this.head.y -= 5.0F;
			this.hat.y += 5.0F;
		}
	}
}
