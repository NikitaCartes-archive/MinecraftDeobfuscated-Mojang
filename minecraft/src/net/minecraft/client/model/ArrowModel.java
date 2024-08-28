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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class ArrowModel extends EntityModel<ArrowRenderState> {
	public ArrowModel(ModelPart modelPart) {
		super(modelPart, RenderType::entityCutout);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"back",
			CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -2.5F, -2.5F, 0.0F, 5.0F, 5.0F),
			PartPose.offsetAndRotation(-11.0F, 0.0F, 0.0F, (float) (Math.PI / 4), 0.0F, 0.0F).withScale(0.8F)
		);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -2.0F, 0.0F, 16.0F, 4.0F, 0.0F, CubeDeformation.NONE, 1.0F, 0.8F);
		partDefinition.addOrReplaceChild("cross_1", cubeListBuilder, PartPose.rotation((float) (Math.PI / 4), 0.0F, 0.0F));
		partDefinition.addOrReplaceChild("cross_2", cubeListBuilder, PartPose.rotation((float) (Math.PI * 3.0 / 4.0), 0.0F, 0.0F));
		return LayerDefinition.create(meshDefinition.transformed(partPose -> partPose.scaled(0.9F)), 32, 32);
	}

	public void setupAnim(ArrowRenderState arrowRenderState) {
		super.setupAnim(arrowRenderState);
		if (arrowRenderState.shake > 0.0F) {
			float f = -Mth.sin(arrowRenderState.shake * 3.0F) * arrowRenderState.shake;
			this.root.zRot += f * (float) (Math.PI / 180.0);
		}
	}
}
