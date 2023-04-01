package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class CrownModel<T extends LivingEntity> extends EntityModel<T> implements HeadedModel {
	public final ModelPart root;

	public CrownModel(ModelPart modelPart) {
		this.root = modelPart;
	}

	public static LayerDefinition createLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"hat", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -9.0F, -2.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.1F, 0.1F, 0.1F)), PartPose.ZERO
		);
		return LayerDefinition.create(meshDefinition, 32, 32);
	}

	public void setupAnim(T livingEntity, float f, float g, float h, float i, float j) {
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
		this.root.render(poseStack, vertexConsumer, i, j, f, g, h, k);
	}

	@Override
	public ModelPart getHead() {
		return this.root;
	}
}
