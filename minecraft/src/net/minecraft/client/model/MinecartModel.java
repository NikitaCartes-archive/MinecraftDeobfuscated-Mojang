package net.minecraft.client.model;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class MinecartModel<T extends Entity> extends HierarchicalModel<T> {
	private final ModelPart root;
	private final ModelPart[] wheels = new ModelPart[4];

	public MinecartModel(ModelPart modelPart) {
		this.root = modelPart;
		Arrays.setAll(this.wheels, i -> modelPart.getChild(createWheelName(i)));
	}

	private static String createWheelName(int i) {
		return "wheel" + i;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		int i = 20;
		int j = 8;
		int k = 16;
		int l = 4;
		partDefinition.addOrReplaceChild(
			"bottom",
			CubeListBuilder.create().texOffs(0, 10).addBox(-10.0F, -8.0F, -1.0F, 20.0F, 16.0F, 2.0F),
			PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"front",
			CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -9.0F, -1.0F, 16.0F, 8.0F, 2.0F),
			PartPose.offsetAndRotation(-9.0F, 4.0F, 0.0F, 0.0F, (float) (Math.PI * 3.0 / 2.0), 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"back",
			CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -9.0F, -1.0F, 16.0F, 8.0F, 2.0F),
			PartPose.offsetAndRotation(9.0F, 4.0F, 0.0F, 0.0F, (float) (Math.PI / 2), 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left",
			CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -9.0F, -1.0F, 16.0F, 8.0F, 2.0F),
			PartPose.offsetAndRotation(0.0F, 4.0F, -7.0F, 0.0F, (float) Math.PI, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"right", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -9.0F, -1.0F, 16.0F, 8.0F, 2.0F), PartPose.offset(0.0F, 4.0F, 7.0F)
		);
		partDefinition.addOrReplaceChild(
			createWheelName(0), CubeListBuilder.create().texOffs(44, 25).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 2.0F), PartPose.offset(-5.0F, 5.0F, -7.0F)
		);
		partDefinition.addOrReplaceChild(
			createWheelName(1), CubeListBuilder.create().texOffs(44, 25).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 2.0F), PartPose.offset(5.0F, 5.0F, -7.0F)
		);
		partDefinition.addOrReplaceChild(
			createWheelName(2), CubeListBuilder.create().texOffs(44, 25).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 2.0F), PartPose.offset(-5.0F, 5.0F, 9.0F)
		);
		partDefinition.addOrReplaceChild(
			createWheelName(3), CubeListBuilder.create().texOffs(44, 25).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 2.0F), PartPose.offset(5.0F, 5.0F, 9.0F)
		);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j) {
		if (Rules.WHEELS_ON_MINECARTS.get()) {
			for (ModelPart modelPart : this.wheels) {
				modelPart.visible = true;
				modelPart.zRot = f;
			}
		} else {
			for (ModelPart modelPart : this.wheels) {
				modelPart.visible = false;
			}
		}
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}
