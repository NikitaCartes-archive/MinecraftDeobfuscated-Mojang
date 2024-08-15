package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class RaftModel extends EntityModel<BoatRenderState> {
	private final ModelPart root;
	private final ModelPart leftPaddle;
	private final ModelPart rightPaddle;

	public RaftModel(ModelPart modelPart) {
		this.root = modelPart;
		this.leftPaddle = modelPart.getChild("left_paddle");
		this.rightPaddle = modelPart.getChild("right_paddle");
	}

	public static void addCommonParts(PartDefinition partDefinition) {
		partDefinition.addOrReplaceChild(
			"bottom",
			CubeListBuilder.create().texOffs(0, 0).addBox(-14.0F, -11.0F, -4.0F, 28.0F, 20.0F, 4.0F).texOffs(0, 0).addBox(-14.0F, -9.0F, -8.0F, 28.0F, 16.0F, 4.0F),
			PartPose.offsetAndRotation(0.0F, -2.1F, 1.0F, 1.5708F, 0.0F, 0.0F)
		);
		int i = 20;
		int j = 7;
		int k = 6;
		float f = -5.0F;
		partDefinition.addOrReplaceChild(
			"left_paddle",
			CubeListBuilder.create().texOffs(0, 24).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F).addBox(-1.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F),
			PartPose.offsetAndRotation(3.0F, -4.0F, 9.0F, 0.0F, 0.0F, (float) (Math.PI / 16))
		);
		partDefinition.addOrReplaceChild(
			"right_paddle",
			CubeListBuilder.create().texOffs(40, 24).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F).addBox(0.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F),
			PartPose.offsetAndRotation(3.0F, -4.0F, -9.0F, 0.0F, (float) Math.PI, (float) (Math.PI / 16))
		);
	}

	public static LayerDefinition createRaftModel() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		addCommonParts(partDefinition);
		return LayerDefinition.create(meshDefinition, 128, 64);
	}

	public static LayerDefinition createChestRaftModel() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		addCommonParts(partDefinition);
		partDefinition.addOrReplaceChild(
			"chest_bottom",
			CubeListBuilder.create().texOffs(0, 76).addBox(0.0F, 0.0F, 0.0F, 12.0F, 8.0F, 12.0F),
			PartPose.offsetAndRotation(-2.0F, -10.1F, -6.0F, 0.0F, (float) (-Math.PI / 2), 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"chest_lid",
			CubeListBuilder.create().texOffs(0, 59).addBox(0.0F, 0.0F, 0.0F, 12.0F, 4.0F, 12.0F),
			PartPose.offsetAndRotation(-2.0F, -14.1F, -6.0F, 0.0F, (float) (-Math.PI / 2), 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"chest_lock",
			CubeListBuilder.create().texOffs(0, 59).addBox(0.0F, 0.0F, 0.0F, 2.0F, 4.0F, 1.0F),
			PartPose.offsetAndRotation(-1.0F, -11.1F, -1.0F, 0.0F, (float) (-Math.PI / 2), 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 128, 128);
	}

	public void setupAnim(BoatRenderState boatRenderState) {
		animatePaddle(boatRenderState.rowingTimeLeft, 0, this.leftPaddle);
		animatePaddle(boatRenderState.rowingTimeRight, 1, this.rightPaddle);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	private static void animatePaddle(float f, int i, ModelPart modelPart) {
		modelPart.xRot = Mth.clampedLerp((float) (-Math.PI / 3), (float) (-Math.PI / 12), (Mth.sin(-f) + 1.0F) / 2.0F);
		modelPart.yRot = Mth.clampedLerp((float) (-Math.PI / 4), (float) (Math.PI / 4), (Mth.sin(-f + 1.0F) + 1.0F) / 2.0F);
		if (i == 1) {
			modelPart.yRot = (float) Math.PI - modelPart.yRot;
		}
	}
}
