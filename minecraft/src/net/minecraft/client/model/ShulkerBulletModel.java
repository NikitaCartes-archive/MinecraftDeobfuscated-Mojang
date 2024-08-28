package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.ShulkerBulletRenderState;

@Environment(EnvType.CLIENT)
public class ShulkerBulletModel extends EntityModel<ShulkerBulletRenderState> {
	private static final String MAIN = "main";
	private final ModelPart main;

	public ShulkerBulletModel(ModelPart modelPart) {
		super(modelPart);
		this.main = modelPart.getChild("main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"main",
			CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-4.0F, -4.0F, -1.0F, 8.0F, 8.0F, 2.0F)
				.texOffs(0, 10)
				.addBox(-1.0F, -4.0F, -4.0F, 2.0F, 8.0F, 8.0F)
				.texOffs(20, 0)
				.addBox(-4.0F, -1.0F, -4.0F, 8.0F, 2.0F, 8.0F),
			PartPose.ZERO
		);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	public void setupAnim(ShulkerBulletRenderState shulkerBulletRenderState) {
		super.setupAnim(shulkerBulletRenderState);
		this.main.yRot = shulkerBulletRenderState.yRot * (float) (Math.PI / 180.0);
		this.main.xRot = shulkerBulletRenderState.xRot * (float) (Math.PI / 180.0);
	}
}
