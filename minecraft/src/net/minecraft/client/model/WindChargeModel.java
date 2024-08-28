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
import net.minecraft.client.renderer.entity.state.EntityRenderState;

@Environment(EnvType.CLIENT)
public class WindChargeModel extends EntityModel<EntityRenderState> {
	private static final int ROTATION_SPEED = 16;
	private final ModelPart bone;
	private final ModelPart windCharge;
	private final ModelPart wind;

	public WindChargeModel(ModelPart modelPart) {
		super(modelPart, RenderType::entityTranslucent);
		this.bone = modelPart.getChild("bone");
		this.wind = this.bone.getChild("wind");
		this.windCharge = this.bone.getChild("wind_charge");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		partDefinition2.addOrReplaceChild(
			"wind",
			CubeListBuilder.create()
				.texOffs(15, 20)
				.addBox(-4.0F, -1.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(0, 9)
				.addBox(-3.0F, -2.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F)
		);
		partDefinition2.addOrReplaceChild(
			"wind_charge",
			CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, 0.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	@Override
	public void setupAnim(EntityRenderState entityRenderState) {
		super.setupAnim(entityRenderState);
		this.windCharge.yRot = -entityRenderState.ageInTicks * 16.0F * (float) (Math.PI / 180.0);
		this.wind.yRot = entityRenderState.ageInTicks * 16.0F * (float) (Math.PI / 180.0);
	}
}
