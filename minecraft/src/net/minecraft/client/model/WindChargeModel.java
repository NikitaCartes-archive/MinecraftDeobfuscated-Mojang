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
import net.minecraft.world.entity.projectile.WindCharge;

@Environment(EnvType.CLIENT)
public class WindChargeModel extends HierarchicalModel<WindCharge> {
	private final ModelPart bone;

	public WindChargeModel(ModelPart modelPart) {
		super(RenderType::entityTranslucent);
		this.bone = modelPart.getChild("bone");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("projectile", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition partDefinition4 = partDefinition3.addOrReplaceChild(
			"wind",
			CubeListBuilder.create()
				.texOffs(20, 112)
				.addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(0, 8)
				.addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, 0.0F, 0.0F)
		);
		partDefinition4.addOrReplaceChild(
			"cube_r1",
			CubeListBuilder.create().texOffs(32, 24).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-0.6F)),
			PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F)
		);
		partDefinition4.addOrReplaceChild(
			"cube_r2",
			CubeListBuilder.create().texOffs(16, 40).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-0.3F)),
			PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F)
		);
		partDefinition3.addOrReplaceChild(
			"wind_charge",
			CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, 0.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	public void setupAnim(WindCharge windCharge, float f, float g, float h, float i, float j) {
	}

	@Override
	public ModelPart root() {
		return this.bone;
	}
}
