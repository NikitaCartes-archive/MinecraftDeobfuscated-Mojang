package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;

@Environment(EnvType.CLIENT)
public class HumanoidHeadModel extends SkullModel {
	private final ModelPart hat = new ModelPart(this, 32, 0);

	public HumanoidHeadModel() {
		super(0, 0, 64, 64);
		this.hat.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.25F);
		this.hat.setPos(0.0F, 0.0F, 0.0F);
	}

	@Override
	public void setupAnim(float f, float g, float h) {
		super.setupAnim(f, g, h);
		this.hat.yRot = this.head.yRot;
		this.hat.xRot = this.head.xRot;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h) {
		super.renderToBuffer(poseStack, vertexConsumer, i, j, f, g, h);
		this.hat.render(poseStack, vertexConsumer, i, j, null, f, g, h);
	}
}
