package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class EvokerFangsModel<T extends Entity> extends HierarchicalModel<T> {
	private final ModelPart root;
	private final ModelPart base;
	private final ModelPart upperJaw;
	private final ModelPart lowerJaw;

	public EvokerFangsModel(ModelPart modelPart) {
		this.root = modelPart;
		this.base = modelPart.getChild("base");
		this.upperJaw = modelPart.getChild("upper_jaw");
		this.lowerJaw = modelPart.getChild("lower_jaw");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"base", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 10.0F, 12.0F, 10.0F), PartPose.offset(-5.0F, 24.0F, -5.0F)
		);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(40, 0).addBox(0.0F, 0.0F, 0.0F, 4.0F, 14.0F, 8.0F);
		partDefinition.addOrReplaceChild("upper_jaw", cubeListBuilder, PartPose.offset(1.5F, 24.0F, -4.0F));
		partDefinition.addOrReplaceChild("lower_jaw", cubeListBuilder, PartPose.offsetAndRotation(-1.5F, 24.0F, 4.0F, 0.0F, (float) Math.PI, 0.0F));
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j) {
		float k = f * 2.0F;
		if (k > 1.0F) {
			k = 1.0F;
		}

		k = 1.0F - k * k * k;
		this.upperJaw.zRot = (float) Math.PI - k * 0.35F * (float) Math.PI;
		this.lowerJaw.zRot = (float) Math.PI + k * 0.35F * (float) Math.PI;
		float l = (f + Mth.sin(f * 2.7F)) * 0.6F * 12.0F;
		this.upperJaw.y = 24.0F - l;
		this.lowerJaw.y = this.upperJaw.y;
		this.base.y = this.upperJaw.y;
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}
