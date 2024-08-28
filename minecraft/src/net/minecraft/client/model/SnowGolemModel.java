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
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class SnowGolemModel extends EntityModel<LivingEntityRenderState> {
	private static final String UPPER_BODY = "upper_body";
	private final ModelPart upperBody;
	private final ModelPart head;
	private final ModelPart leftArm;
	private final ModelPart rightArm;

	public SnowGolemModel(ModelPart modelPart) {
		super(modelPart);
		this.head = modelPart.getChild("head");
		this.leftArm = modelPart.getChild("left_arm");
		this.rightArm = modelPart.getChild("right_arm");
		this.upperBody = modelPart.getChild("upper_body");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		float f = 4.0F;
		CubeDeformation cubeDeformation = new CubeDeformation(-0.5F);
		partDefinition.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDeformation), PartPose.offset(0.0F, 4.0F, 0.0F)
		);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, 0.0F, -1.0F, 12.0F, 2.0F, 2.0F, cubeDeformation);
		partDefinition.addOrReplaceChild("left_arm", cubeListBuilder, PartPose.offsetAndRotation(5.0F, 6.0F, 1.0F, 0.0F, 0.0F, 1.0F));
		partDefinition.addOrReplaceChild("right_arm", cubeListBuilder, PartPose.offsetAndRotation(-5.0F, 6.0F, -1.0F, 0.0F, (float) Math.PI, -1.0F));
		partDefinition.addOrReplaceChild(
			"upper_body", CubeListBuilder.create().texOffs(0, 16).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F, cubeDeformation), PartPose.offset(0.0F, 13.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"lower_body", CubeListBuilder.create().texOffs(0, 36).addBox(-6.0F, -12.0F, -6.0F, 12.0F, 12.0F, 12.0F, cubeDeformation), PartPose.offset(0.0F, 24.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	public void setupAnim(LivingEntityRenderState livingEntityRenderState) {
		super.setupAnim(livingEntityRenderState);
		this.head.yRot = livingEntityRenderState.yRot * (float) (Math.PI / 180.0);
		this.head.xRot = livingEntityRenderState.xRot * (float) (Math.PI / 180.0);
		this.upperBody.yRot = livingEntityRenderState.yRot * (float) (Math.PI / 180.0) * 0.25F;
		float f = Mth.sin(this.upperBody.yRot);
		float g = Mth.cos(this.upperBody.yRot);
		this.leftArm.yRot = this.upperBody.yRot;
		this.rightArm.yRot = this.upperBody.yRot + (float) Math.PI;
		this.leftArm.x = g * 5.0F;
		this.leftArm.z = -f * 5.0F;
		this.rightArm.x = -g * 5.0F;
		this.rightArm.z = f * 5.0F;
	}

	public ModelPart getHead() {
		return this.head;
	}
}
