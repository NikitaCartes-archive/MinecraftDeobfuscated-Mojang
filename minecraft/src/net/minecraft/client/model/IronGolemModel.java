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
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class IronGolemModel extends EntityModel<IronGolemRenderState> {
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;

	public IronGolemModel(ModelPart modelPart) {
		this.root = modelPart;
		this.head = modelPart.getChild("head");
		this.rightArm = modelPart.getChild("right_arm");
		this.leftArm = modelPart.getChild("left_arm");
		this.rightLeg = modelPart.getChild("right_leg");
		this.leftLeg = modelPart.getChild("left_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"head",
			CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -12.0F, -5.5F, 8.0F, 10.0F, 8.0F).texOffs(24, 0).addBox(-1.0F, -5.0F, -7.5F, 2.0F, 4.0F, 2.0F),
			PartPose.offset(0.0F, -7.0F, -2.0F)
		);
		partDefinition.addOrReplaceChild(
			"body",
			CubeListBuilder.create()
				.texOffs(0, 40)
				.addBox(-9.0F, -2.0F, -6.0F, 18.0F, 12.0F, 11.0F)
				.texOffs(0, 70)
				.addBox(-4.5F, 10.0F, -3.0F, 9.0F, 5.0F, 6.0F, new CubeDeformation(0.5F)),
			PartPose.offset(0.0F, -7.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"right_arm", CubeListBuilder.create().texOffs(60, 21).addBox(-13.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F), PartPose.offset(0.0F, -7.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_arm", CubeListBuilder.create().texOffs(60, 58).addBox(9.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F), PartPose.offset(0.0F, -7.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"right_leg", CubeListBuilder.create().texOffs(37, 0).addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F), PartPose.offset(-4.0F, 11.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_leg", CubeListBuilder.create().texOffs(60, 0).mirror().addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F), PartPose.offset(5.0F, 11.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 128, 128);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	public void setupAnim(IronGolemRenderState ironGolemRenderState) {
		float f = ironGolemRenderState.attackTicksRemaining;
		float g = ironGolemRenderState.walkAnimationSpeed;
		float h = ironGolemRenderState.walkAnimationPos;
		if (f > 0.0F) {
			this.rightArm.xRot = -2.0F + 1.5F * Mth.triangleWave(f, 10.0F);
			this.leftArm.xRot = -2.0F + 1.5F * Mth.triangleWave(f, 10.0F);
		} else {
			int i = ironGolemRenderState.offerFlowerTick;
			if (i > 0) {
				this.rightArm.xRot = -0.8F + 0.025F * Mth.triangleWave((float)i, 70.0F);
				this.leftArm.xRot = 0.0F;
			} else {
				this.rightArm.xRot = (-0.2F + 1.5F * Mth.triangleWave(h, 13.0F)) * g;
				this.leftArm.xRot = (-0.2F - 1.5F * Mth.triangleWave(h, 13.0F)) * g;
			}
		}

		this.head.yRot = ironGolemRenderState.yRot * (float) (Math.PI / 180.0);
		this.head.xRot = ironGolemRenderState.xRot * (float) (Math.PI / 180.0);
		this.rightLeg.xRot = -1.5F * Mth.triangleWave(h, 13.0F) * g;
		this.leftLeg.xRot = 1.5F * Mth.triangleWave(h, 13.0F) * g;
		this.rightLeg.yRot = 0.0F;
		this.leftLeg.yRot = 0.0F;
	}

	public ModelPart getFlowerHoldingArm() {
		return this.rightArm;
	}
}
