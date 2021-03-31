package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class ShulkerBulletModel<T extends Entity> extends HierarchicalModel<T> {
	private static final String MAIN = "main";
	private final ModelPart root;
	private final ModelPart main;

	public ShulkerBulletModel(ModelPart modelPart) {
		this.root = modelPart;
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

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j) {
		this.main.yRot = i * (float) (Math.PI / 180.0);
		this.main.xRot = j * (float) (Math.PI / 180.0);
	}
}
