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
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class CreeperModel extends EntityModel<CreeperRenderState> {
	private final ModelPart head;
	private final ModelPart rightHindLeg;
	private final ModelPart leftHindLeg;
	private final ModelPart rightFrontLeg;
	private final ModelPart leftFrontLeg;
	private static final int Y_OFFSET = 6;

	public CreeperModel(ModelPart modelPart) {
		super(modelPart);
		this.head = modelPart.getChild("head");
		this.leftHindLeg = modelPart.getChild("right_hind_leg");
		this.rightHindLeg = modelPart.getChild("left_hind_leg");
		this.leftFrontLeg = modelPart.getChild("right_front_leg");
		this.rightFrontLeg = modelPart.getChild("left_front_leg");
	}

	public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDeformation), PartPose.offset(0.0F, 6.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, cubeDeformation), PartPose.offset(0.0F, 6.0F, 0.0F)
		);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, cubeDeformation);
		partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-2.0F, 18.0F, 4.0F));
		partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(2.0F, 18.0F, 4.0F));
		partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offset(-2.0F, 18.0F, -4.0F));
		partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(2.0F, 18.0F, -4.0F));
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	public void setupAnim(CreeperRenderState creeperRenderState) {
		super.setupAnim(creeperRenderState);
		this.head.yRot = creeperRenderState.yRot * (float) (Math.PI / 180.0);
		this.head.xRot = creeperRenderState.xRot * (float) (Math.PI / 180.0);
		float f = creeperRenderState.walkAnimationSpeed;
		float g = creeperRenderState.walkAnimationPos;
		this.rightHindLeg.xRot = Mth.cos(g * 0.6662F) * 1.4F * f;
		this.leftHindLeg.xRot = Mth.cos(g * 0.6662F + (float) Math.PI) * 1.4F * f;
		this.rightFrontLeg.xRot = Mth.cos(g * 0.6662F + (float) Math.PI) * 1.4F * f;
		this.leftFrontLeg.xRot = Mth.cos(g * 0.6662F) * 1.4F * f;
	}
}
