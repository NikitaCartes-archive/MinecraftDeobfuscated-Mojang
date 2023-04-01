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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class TailModel<T extends LivingEntity> extends EntityModel<T> {
	public final ModelPart root;
	public final ModelPart tail;

	public TailModel(ModelPart modelPart) {
		super(RenderType::entityCutoutNoCull);
		this.root = modelPart;
		this.tail = modelPart.getChild("tail");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"tail",
			CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 4.0F, 9.0F, 5.0F),
			PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5184364F, 0.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 18, 14);
	}

	public void setupAnim(T livingEntity, float f, float g, float h, float i, float j) {
		float k = Mth.cos(h) * 0.01F + Mth.cos(f) * 0.1F * g;
		this.tail.yRot = k;
		if (livingEntity.isCrouching()) {
			this.tail.y = 15.0F;
			this.tail.x = -2.0F;
			this.tail.z = 4.0F;
		} else {
			this.tail.y = 14.0F;
			this.tail.x = -2.0F;
			this.tail.z = 2.0F;
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
		this.root.render(poseStack, vertexConsumer, i, j, f, g, h, k);
	}
}
