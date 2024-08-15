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
import net.minecraft.client.renderer.entity.state.DonkeyRenderState;

@Environment(EnvType.CLIENT)
public class DonkeyModel extends AbstractEquineModel<DonkeyRenderState> {
	private final ModelPart leftChest = this.body.getChild("left_chest");
	private final ModelPart rightChest = this.body.getChild("right_chest");

	public DonkeyModel(ModelPart modelPart) {
		super(modelPart);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = AbstractEquineModel.createBodyMesh(CubeDeformation.NONE);
		modifyMesh(meshDefinition.getRoot());
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	public static LayerDefinition createBabyLayer() {
		MeshDefinition meshDefinition = AbstractEquineModel.createFullScaleBabyMesh(CubeDeformation.NONE);
		modifyMesh(meshDefinition.getRoot());
		return LayerDefinition.create(AbstractEquineModel.BABY_TRANSFORMER.apply(meshDefinition), 64, 64);
	}

	private static void modifyMesh(PartDefinition partDefinition) {
		PartDefinition partDefinition2 = partDefinition.getChild("body");
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(26, 21).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 8.0F, 3.0F);
		partDefinition2.addOrReplaceChild("left_chest", cubeListBuilder, PartPose.offsetAndRotation(6.0F, -8.0F, 0.0F, 0.0F, (float) (-Math.PI / 2), 0.0F));
		partDefinition2.addOrReplaceChild("right_chest", cubeListBuilder, PartPose.offsetAndRotation(-6.0F, -8.0F, 0.0F, 0.0F, (float) (Math.PI / 2), 0.0F));
		PartDefinition partDefinition3 = partDefinition.getChild("head_parts").getChild("head");
		CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(0, 12).addBox(-1.0F, -7.0F, 0.0F, 2.0F, 7.0F, 1.0F);
		partDefinition3.addOrReplaceChild(
			"left_ear", cubeListBuilder2, PartPose.offsetAndRotation(1.25F, -10.0F, 4.0F, (float) (Math.PI / 12), 0.0F, (float) (Math.PI / 12))
		);
		partDefinition3.addOrReplaceChild(
			"right_ear", cubeListBuilder2, PartPose.offsetAndRotation(-1.25F, -10.0F, 4.0F, (float) (Math.PI / 12), 0.0F, (float) (-Math.PI / 12))
		);
	}

	public void setupAnim(DonkeyRenderState donkeyRenderState) {
		super.setupAnim(donkeyRenderState);
		this.leftChest.visible = donkeyRenderState.hasChest;
		this.rightChest.visible = donkeyRenderState.hasChest;
	}
}
