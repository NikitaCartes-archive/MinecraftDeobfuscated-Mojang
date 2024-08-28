package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.DolphinRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class DolphinModel extends EntityModel<DolphinRenderState> {
	public static final MeshTransformer BABY_TRANSFORMER = MeshTransformer.scaling(0.5F);
	private final ModelPart body;
	private final ModelPart tail;
	private final ModelPart tailFin;

	public DolphinModel(ModelPart modelPart) {
		super(modelPart);
		this.body = modelPart.getChild("body");
		this.tail = this.body.getChild("tail");
		this.tailFin = this.tail.getChild("tail_fin");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		float f = 18.0F;
		float g = -8.0F;
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(22, 0).addBox(-4.0F, -7.0F, 0.0F, 8.0F, 7.0F, 13.0F), PartPose.offset(0.0F, 22.0F, -5.0F)
		);
		partDefinition2.addOrReplaceChild(
			"back_fin", CubeListBuilder.create().texOffs(51, 0).addBox(-0.5F, 0.0F, 8.0F, 1.0F, 4.0F, 5.0F), PartPose.rotation((float) (Math.PI / 3), 0.0F, 0.0F)
		);
		partDefinition2.addOrReplaceChild(
			"left_fin",
			CubeListBuilder.create().texOffs(48, 20).mirror().addBox(-0.5F, -4.0F, 0.0F, 1.0F, 4.0F, 7.0F),
			PartPose.offsetAndRotation(2.0F, -2.0F, 4.0F, (float) (Math.PI / 3), 0.0F, (float) (Math.PI * 2.0 / 3.0))
		);
		partDefinition2.addOrReplaceChild(
			"right_fin",
			CubeListBuilder.create().texOffs(48, 20).addBox(-0.5F, -4.0F, 0.0F, 1.0F, 4.0F, 7.0F),
			PartPose.offsetAndRotation(-2.0F, -2.0F, 4.0F, (float) (Math.PI / 3), 0.0F, (float) (-Math.PI * 2.0 / 3.0))
		);
		PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild(
			"tail",
			CubeListBuilder.create().texOffs(0, 19).addBox(-2.0F, -2.5F, 0.0F, 4.0F, 5.0F, 11.0F),
			PartPose.offsetAndRotation(0.0F, -2.5F, 11.0F, -0.10471976F, 0.0F, 0.0F)
		);
		partDefinition3.addOrReplaceChild(
			"tail_fin", CubeListBuilder.create().texOffs(19, 20).addBox(-5.0F, -0.5F, 0.0F, 10.0F, 1.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 9.0F)
		);
		PartDefinition partDefinition4 = partDefinition2.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -3.0F, -3.0F, 8.0F, 7.0F, 6.0F), PartPose.offset(0.0F, -4.0F, -3.0F)
		);
		partDefinition4.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 2.0F, -7.0F, 2.0F, 2.0F, 4.0F), PartPose.ZERO);
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	public void setupAnim(DolphinRenderState dolphinRenderState) {
		super.setupAnim(dolphinRenderState);
		this.body.xRot = dolphinRenderState.xRot * (float) (Math.PI / 180.0);
		this.body.yRot = dolphinRenderState.yRot * (float) (Math.PI / 180.0);
		if (dolphinRenderState.isMoving) {
			this.body.xRot = this.body.xRot + (-0.05F - 0.05F * Mth.cos(dolphinRenderState.ageInTicks * 0.3F));
			this.tail.xRot = -0.1F * Mth.cos(dolphinRenderState.ageInTicks * 0.3F);
			this.tailFin.xRot = -0.2F * Mth.cos(dolphinRenderState.ageInTicks * 0.3F);
		}
	}
}
