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
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public class PlayerCapeModel<T extends PlayerRenderState> extends HumanoidModel<T> {
	private static final String CAPE = "cape";
	private final ModelPart cape = this.body.getChild("cape");

	public PlayerCapeModel(ModelPart modelPart) {
		super(modelPart);
	}

	public static LayerDefinition createCapeLayer() {
		MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.clearChild("head");
		partDefinition2.clearChild("hat");
		PartDefinition partDefinition3 = partDefinition.clearChild("body");
		partDefinition.clearChild("left_arm");
		partDefinition.clearChild("right_arm");
		partDefinition.clearChild("left_leg");
		partDefinition.clearChild("right_leg");
		partDefinition3.addOrReplaceChild(
			"cape",
			CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, CubeDeformation.NONE, 1.0F, 0.5F),
			PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 0.0F, (float) Math.PI, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	public void setupAnim(T playerRenderState) {
		super.setupAnim(playerRenderState);
		this.cape
			.rotateBy(
				new Quaternionf()
					.rotateY((float) -Math.PI)
					.rotateX((6.0F + playerRenderState.capeLean / 2.0F + playerRenderState.capeFlap) * (float) (Math.PI / 180.0))
					.rotateZ(playerRenderState.capeLean2 / 2.0F * (float) (Math.PI / 180.0))
					.rotateY((180.0F - playerRenderState.capeLean2 / 2.0F) * (float) (Math.PI / 180.0))
			);
	}
}
