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
import net.minecraft.world.entity.animal.Sheep;

@Environment(EnvType.CLIENT)
public class SheepFurModel<T extends Sheep> extends QuadrupedModel<T> {
	private float headXRot;

	public SheepFurModel(ModelPart modelPart) {
		super(modelPart, false, 8.0F, 4.0F, 2.0F, 2.0F, 24);
	}

	public static LayerDefinition createFurLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.6F)), PartPose.offset(0.0F, 6.0F, -8.0F)
		);
		partDefinition.addOrReplaceChild(
			"body",
			CubeListBuilder.create().texOffs(28, 8).addBox(-4.0F, -10.0F, -7.0F, 8.0F, 16.0F, 6.0F, new CubeDeformation(1.75F)),
			PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
		);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.5F));
		partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-3.0F, 12.0F, 7.0F));
		partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(3.0F, 12.0F, 7.0F));
		partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offset(-3.0F, 12.0F, -5.0F));
		partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(3.0F, 12.0F, -5.0F));
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	public void prepareMobModel(T sheep, float f, float g, float h) {
		super.prepareMobModel(sheep, f, g, h);
		this.head.y = 6.0F + sheep.getHeadEatPositionScale(h) * 9.0F;
		this.headXRot = sheep.getHeadEatAngleScale(h);
	}

	public void setupAnim(T sheep, float f, float g, float h, float i, float j) {
		super.setupAnim(sheep, f, g, h, i, j);
		this.head.xRot = this.headXRot;
	}
}
