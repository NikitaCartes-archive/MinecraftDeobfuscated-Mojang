package net.minecraft.client.model;

import java.util.Set;
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
import net.minecraft.client.renderer.entity.state.SheepRenderState;

@Environment(EnvType.CLIENT)
public class SheepModel extends QuadrupedModel<SheepRenderState> {
	public static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(false, 8.0F, 4.0F, 2.0F, 2.0F, 24.0F, Set.of("head"));

	public SheepModel(ModelPart modelPart) {
		super(modelPart);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = QuadrupedModel.createBodyMesh(12, CubeDeformation.NONE);
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -4.0F, -6.0F, 6.0F, 6.0F, 8.0F), PartPose.offset(0.0F, 6.0F, -8.0F)
		);
		partDefinition.addOrReplaceChild(
			"body",
			CubeListBuilder.create().texOffs(28, 8).addBox(-4.0F, -10.0F, -7.0F, 8.0F, 16.0F, 6.0F),
			PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	public void setupAnim(SheepRenderState sheepRenderState) {
		super.setupAnim(sheepRenderState);
		this.head.y = this.head.y + sheepRenderState.headEatPositionScale * 9.0F * sheepRenderState.ageScale;
		this.head.xRot = sheepRenderState.headEatAngleScale;
	}
}
