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
public class LlamaSpitModel<T extends Entity> extends HierarchicalModel<T> {
	private final ModelPart root;

	public LlamaSpitModel(ModelPart modelPart) {
		this.root = modelPart;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		int i = 2;
		partDefinition.addOrReplaceChild(
			"main",
			CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-4.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F)
				.addBox(0.0F, -4.0F, 0.0F, 2.0F, 2.0F, 2.0F)
				.addBox(0.0F, 0.0F, -4.0F, 2.0F, 2.0F, 2.0F)
				.addBox(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F)
				.addBox(2.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F)
				.addBox(0.0F, 2.0F, 0.0F, 2.0F, 2.0F, 2.0F)
				.addBox(0.0F, 0.0F, 2.0F, 2.0F, 2.0F, 2.0F),
			PartPose.ZERO
		);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j) {
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}
