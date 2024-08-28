package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class TadpoleModel extends EntityModel<LivingEntityRenderState> {
	private final ModelPart tail;

	public TadpoleModel(ModelPart modelPart) {
		super(modelPart, RenderType::entityCutoutNoCull);
		this.tail = modelPart.getChild("tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		float f = 0.0F;
		float g = 22.0F;
		float h = -3.0F;
		partDefinition.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 2.0F, 3.0F), PartPose.offset(0.0F, 22.0F, -3.0F)
		);
		partDefinition.addOrReplaceChild(
			"tail", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -1.0F, 0.0F, 0.0F, 2.0F, 7.0F), PartPose.offset(0.0F, 22.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 16, 16);
	}

	public void setupAnim(LivingEntityRenderState livingEntityRenderState) {
		super.setupAnim(livingEntityRenderState);
		float f = livingEntityRenderState.isInWater ? 1.0F : 1.5F;
		this.tail.yRot = -f * 0.25F * Mth.sin(0.3F * livingEntityRenderState.ageInTicks);
	}
}
