package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class SpiderModel extends EntityModel<LivingEntityRenderState> {
	private static final String BODY_0 = "body0";
	private static final String BODY_1 = "body1";
	private static final String RIGHT_MIDDLE_FRONT_LEG = "right_middle_front_leg";
	private static final String LEFT_MIDDLE_FRONT_LEG = "left_middle_front_leg";
	private static final String RIGHT_MIDDLE_HIND_LEG = "right_middle_hind_leg";
	private static final String LEFT_MIDDLE_HIND_LEG = "left_middle_hind_leg";
	private final ModelPart head;
	private final ModelPart rightHindLeg;
	private final ModelPart leftHindLeg;
	private final ModelPart rightMiddleHindLeg;
	private final ModelPart leftMiddleHindLeg;
	private final ModelPart rightMiddleFrontLeg;
	private final ModelPart leftMiddleFrontLeg;
	private final ModelPart rightFrontLeg;
	private final ModelPart leftFrontLeg;

	public SpiderModel(ModelPart modelPart) {
		super(modelPart);
		this.head = modelPart.getChild("head");
		this.rightHindLeg = modelPart.getChild("right_hind_leg");
		this.leftHindLeg = modelPart.getChild("left_hind_leg");
		this.rightMiddleHindLeg = modelPart.getChild("right_middle_hind_leg");
		this.leftMiddleHindLeg = modelPart.getChild("left_middle_hind_leg");
		this.rightMiddleFrontLeg = modelPart.getChild("right_middle_front_leg");
		this.leftMiddleFrontLeg = modelPart.getChild("left_middle_front_leg");
		this.rightFrontLeg = modelPart.getChild("right_front_leg");
		this.leftFrontLeg = modelPart.getChild("left_front_leg");
	}

	public static LayerDefinition createSpiderBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		int i = 15;
		partDefinition.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(32, 4).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, 15.0F, -3.0F)
		);
		partDefinition.addOrReplaceChild(
			"body0", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 15.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"body1", CubeListBuilder.create().texOffs(0, 12).addBox(-5.0F, -4.0F, -6.0F, 10.0F, 8.0F, 12.0F), PartPose.offset(0.0F, 15.0F, 9.0F)
		);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(18, 0).addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F);
		CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(18, 0).mirror().addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F);
		float f = (float) (Math.PI / 4);
		float g = (float) (Math.PI / 8);
		partDefinition.addOrReplaceChild(
			"right_hind_leg", cubeListBuilder, PartPose.offsetAndRotation(-4.0F, 15.0F, 2.0F, 0.0F, (float) (Math.PI / 4), (float) (-Math.PI / 4))
		);
		partDefinition.addOrReplaceChild(
			"left_hind_leg", cubeListBuilder2, PartPose.offsetAndRotation(4.0F, 15.0F, 2.0F, 0.0F, (float) (-Math.PI / 4), (float) (Math.PI / 4))
		);
		partDefinition.addOrReplaceChild(
			"right_middle_hind_leg", cubeListBuilder, PartPose.offsetAndRotation(-4.0F, 15.0F, 1.0F, 0.0F, (float) (Math.PI / 8), -0.58119464F)
		);
		partDefinition.addOrReplaceChild(
			"left_middle_hind_leg", cubeListBuilder2, PartPose.offsetAndRotation(4.0F, 15.0F, 1.0F, 0.0F, (float) (-Math.PI / 8), 0.58119464F)
		);
		partDefinition.addOrReplaceChild(
			"right_middle_front_leg", cubeListBuilder, PartPose.offsetAndRotation(-4.0F, 15.0F, 0.0F, 0.0F, (float) (-Math.PI / 8), -0.58119464F)
		);
		partDefinition.addOrReplaceChild(
			"left_middle_front_leg", cubeListBuilder2, PartPose.offsetAndRotation(4.0F, 15.0F, 0.0F, 0.0F, (float) (Math.PI / 8), 0.58119464F)
		);
		partDefinition.addOrReplaceChild(
			"right_front_leg", cubeListBuilder, PartPose.offsetAndRotation(-4.0F, 15.0F, -1.0F, 0.0F, (float) (-Math.PI / 4), (float) (-Math.PI / 4))
		);
		partDefinition.addOrReplaceChild(
			"left_front_leg", cubeListBuilder2, PartPose.offsetAndRotation(4.0F, 15.0F, -1.0F, 0.0F, (float) (Math.PI / 4), (float) (Math.PI / 4))
		);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	public void setupAnim(LivingEntityRenderState livingEntityRenderState) {
		super.setupAnim(livingEntityRenderState);
		this.head.yRot = livingEntityRenderState.yRot * (float) (Math.PI / 180.0);
		this.head.xRot = livingEntityRenderState.xRot * (float) (Math.PI / 180.0);
		float f = livingEntityRenderState.walkAnimationPos * 0.6662F;
		float g = livingEntityRenderState.walkAnimationSpeed;
		float h = -(Mth.cos(f * 2.0F + 0.0F) * 0.4F) * g;
		float i = -(Mth.cos(f * 2.0F + (float) Math.PI) * 0.4F) * g;
		float j = -(Mth.cos(f * 2.0F + (float) (Math.PI / 2)) * 0.4F) * g;
		float k = -(Mth.cos(f * 2.0F + (float) (Math.PI * 3.0 / 2.0)) * 0.4F) * g;
		float l = Math.abs(Mth.sin(f + 0.0F) * 0.4F) * g;
		float m = Math.abs(Mth.sin(f + (float) Math.PI) * 0.4F) * g;
		float n = Math.abs(Mth.sin(f + (float) (Math.PI / 2)) * 0.4F) * g;
		float o = Math.abs(Mth.sin(f + (float) (Math.PI * 3.0 / 2.0)) * 0.4F) * g;
		this.rightHindLeg.yRot += h;
		this.leftHindLeg.yRot -= h;
		this.rightMiddleHindLeg.yRot += i;
		this.leftMiddleHindLeg.yRot -= i;
		this.rightMiddleFrontLeg.yRot += j;
		this.leftMiddleFrontLeg.yRot -= j;
		this.rightFrontLeg.yRot += k;
		this.leftFrontLeg.yRot -= k;
		this.rightHindLeg.zRot += l;
		this.leftHindLeg.zRot -= l;
		this.rightMiddleHindLeg.zRot += m;
		this.leftMiddleHindLeg.zRot -= m;
		this.rightMiddleFrontLeg.zRot += n;
		this.leftMiddleFrontLeg.zRot -= n;
		this.rightFrontLeg.zRot += o;
		this.leftFrontLeg.zRot -= o;
	}
}
