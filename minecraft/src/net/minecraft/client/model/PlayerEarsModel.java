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
import net.minecraft.client.renderer.entity.state.PlayerRenderState;

@Environment(EnvType.CLIENT)
public class PlayerEarsModel extends HumanoidModel<PlayerRenderState> {
	public PlayerEarsModel(ModelPart modelPart) {
		super(modelPart);
	}

	public static LayerDefinition createEarsLayer() {
		MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.clearChild("head");
		partDefinition2.clearChild("hat");
		partDefinition.clearChild("body");
		partDefinition.clearChild("left_arm");
		partDefinition.clearChild("right_arm");
		partDefinition.clearChild("left_leg");
		partDefinition.clearChild("right_leg");
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(24, 0).addBox(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(1.0F));
		partDefinition2.addOrReplaceChild("left_ear", cubeListBuilder, PartPose.offset(-6.0F, -6.0F, 0.0F));
		partDefinition2.addOrReplaceChild("right_ear", cubeListBuilder, PartPose.offset(6.0F, -6.0F, 0.0F));
		return LayerDefinition.create(meshDefinition, 64, 64);
	}
}
