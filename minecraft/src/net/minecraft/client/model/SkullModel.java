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

@Environment(EnvType.CLIENT)
public class SkullModel extends SkullModelBase {
	protected final ModelPart head;

	public SkullModel(ModelPart modelPart) {
		super(modelPart);
		this.head = modelPart.getChild("head");
	}

	public static MeshDefinition createHeadModel() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
		return meshDefinition;
	}

	public static LayerDefinition createHumanoidHeadLayer() {
		MeshDefinition meshDefinition = createHeadModel();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.getChild("head")
			.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.25F)), PartPose.ZERO);
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	public static LayerDefinition createMobHeadLayer() {
		MeshDefinition meshDefinition = createHeadModel();
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	@Override
	public void setupAnim(float f, float g, float h) {
		this.head.yRot = g * (float) (Math.PI / 180.0);
		this.head.xRot = h * (float) (Math.PI / 180.0);
	}
}
