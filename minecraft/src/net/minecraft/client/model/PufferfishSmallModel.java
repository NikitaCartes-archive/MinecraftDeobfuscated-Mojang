package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class PufferfishSmallModel extends EntityModel<EntityRenderState> {
	private final ModelPart leftFin;
	private final ModelPart rightFin;

	public PufferfishSmallModel(ModelPart modelPart) {
		super(modelPart);
		this.leftFin = modelPart.getChild("left_fin");
		this.rightFin = modelPart.getChild("right_fin");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		int i = 23;
		partDefinition.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(0, 27).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(0.0F, 23.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"right_eye", CubeListBuilder.create().texOffs(24, 6).addBox(-1.5F, 0.0F, -1.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 20.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_eye", CubeListBuilder.create().texOffs(28, 6).addBox(0.5F, 0.0F, -1.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 20.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"back_fin", CubeListBuilder.create().texOffs(-3, 0).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 0.0F, 3.0F), PartPose.offset(0.0F, 22.0F, 1.5F)
		);
		partDefinition.addOrReplaceChild(
			"right_fin", CubeListBuilder.create().texOffs(25, 0).addBox(-1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 2.0F), PartPose.offset(-1.5F, 22.0F, -1.5F)
		);
		partDefinition.addOrReplaceChild(
			"left_fin", CubeListBuilder.create().texOffs(25, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 2.0F), PartPose.offset(1.5F, 22.0F, -1.5F)
		);
		return LayerDefinition.create(meshDefinition, 32, 32);
	}

	@Override
	public void setupAnim(EntityRenderState entityRenderState) {
		super.setupAnim(entityRenderState);
		this.rightFin.zRot = -0.2F + 0.4F * Mth.sin(entityRenderState.ageInTicks * 0.2F);
		this.leftFin.zRot = 0.2F - 0.4F * Mth.sin(entityRenderState.ageInTicks * 0.2F);
	}
}
