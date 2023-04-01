package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class BeretModel<T extends LivingEntity> extends EntityModel<T> implements HeadedModel {
	public final ModelPart root;

	public BeretModel(ModelPart modelPart) {
		this.root = modelPart;
	}

	public static LayerDefinition createLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
				"hat",
				CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -9.0F, -5.0F, 10.0F, 3.0F, 10.0F),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, (float) (-Math.PI / 12))
			)
			.addOrReplaceChild(
				"innerhat",
				CubeListBuilder.create().texOffs(0, 13).addBox(-3.75F, -8.0F, -4.5F, 9.0F, 2.0F, 9.0F),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1308997F)
			)
			.addOrReplaceChild(
				"thing",
				CubeListBuilder.create().texOffs(0, 0).addBox(-4.8F, -8.0F, -5.1F, 2.0F, 3.0F, 0.0F),
				PartPose.offsetAndRotation(4.0F, -3.0F, 4.0F, (float) (-Math.PI / 18), (float) (Math.PI / 180.0), 0.0F)
			);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	public void setupAnim(T livingEntity, float f, float g, float h, float i, float j) {
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
		poseStack.scale(1.001F, 1.001F, 1.001F);
		this.root.render(poseStack, vertexConsumer, i, j, f, g, h, k);
	}

	@Override
	public ModelPart getHead() {
		return this.root;
	}
}
