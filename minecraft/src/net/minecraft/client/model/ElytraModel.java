package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

@Environment(EnvType.CLIENT)
public class ElytraModel extends EntityModel<HumanoidRenderState> {
	public static final MeshTransformer BABY_TRANSFORMER = MeshTransformer.scaling(0.5F);
	private final ModelPart rightWing;
	private final ModelPart leftWing;

	public ElytraModel(ModelPart modelPart) {
		super(modelPart);
		this.leftWing = modelPart.getChild("left_wing");
		this.rightWing = modelPart.getChild("right_wing");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		CubeDeformation cubeDeformation = new CubeDeformation(1.0F);
		partDefinition.addOrReplaceChild(
			"left_wing",
			CubeListBuilder.create().texOffs(22, 0).addBox(-10.0F, 0.0F, 0.0F, 10.0F, 20.0F, 2.0F, cubeDeformation),
			PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, (float) (Math.PI / 12), 0.0F, (float) (-Math.PI / 12))
		);
		partDefinition.addOrReplaceChild(
			"right_wing",
			CubeListBuilder.create().texOffs(22, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 10.0F, 20.0F, 2.0F, cubeDeformation),
			PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, (float) (Math.PI / 12), 0.0F, (float) (Math.PI / 12))
		);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	public void setupAnim(HumanoidRenderState humanoidRenderState) {
		super.setupAnim(humanoidRenderState);
		this.leftWing.y = humanoidRenderState.isCrouching ? 3.0F : 0.0F;
		this.leftWing.xRot = humanoidRenderState.elytraRotX;
		this.leftWing.zRot = humanoidRenderState.elytraRotZ;
		this.leftWing.yRot = humanoidRenderState.elytraRotY;
		this.rightWing.yRot = -this.leftWing.yRot;
		this.rightWing.y = this.leftWing.y;
		this.rightWing.xRot = this.leftWing.xRot;
		this.rightWing.zRot = -this.leftWing.zRot;
	}
}
