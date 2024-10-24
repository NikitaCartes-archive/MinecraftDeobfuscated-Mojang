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
import net.minecraft.client.renderer.entity.state.TropicalFishRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class TropicalFishModelB extends EntityModel<TropicalFishRenderState> {
	private final ModelPart tail;

	public TropicalFishModelB(ModelPart modelPart) {
		super(modelPart);
		this.tail = modelPart.getChild("tail");
	}

	public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		int i = 19;
		partDefinition.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(0, 20).addBox(-1.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, cubeDeformation), PartPose.offset(0.0F, 19.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"tail", CubeListBuilder.create().texOffs(21, 16).addBox(0.0F, -3.0F, 0.0F, 0.0F, 6.0F, 5.0F, cubeDeformation), PartPose.offset(0.0F, 19.0F, 3.0F)
		);
		partDefinition.addOrReplaceChild(
			"right_fin",
			CubeListBuilder.create().texOffs(2, 16).addBox(-2.0F, 0.0F, 0.0F, 2.0F, 2.0F, 0.0F, cubeDeformation),
			PartPose.offsetAndRotation(-1.0F, 20.0F, 0.0F, 0.0F, (float) (Math.PI / 4), 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_fin",
			CubeListBuilder.create().texOffs(2, 12).addBox(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 0.0F, cubeDeformation),
			PartPose.offsetAndRotation(1.0F, 20.0F, 0.0F, 0.0F, (float) (-Math.PI / 4), 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"top_fin", CubeListBuilder.create().texOffs(20, 11).addBox(0.0F, -4.0F, 0.0F, 0.0F, 4.0F, 6.0F, cubeDeformation), PartPose.offset(0.0F, 16.0F, -3.0F)
		);
		partDefinition.addOrReplaceChild(
			"bottom_fin", CubeListBuilder.create().texOffs(20, 21).addBox(0.0F, 0.0F, 0.0F, 0.0F, 4.0F, 6.0F, cubeDeformation), PartPose.offset(0.0F, 22.0F, -3.0F)
		);
		return LayerDefinition.create(meshDefinition, 32, 32);
	}

	public void setupAnim(TropicalFishRenderState tropicalFishRenderState) {
		super.setupAnim(tropicalFishRenderState);
		float f = tropicalFishRenderState.isInWater ? 1.0F : 1.5F;
		this.tail.yRot = -f * 0.45F * Mth.sin(0.6F * tropicalFishRenderState.ageInTicks);
	}
}
