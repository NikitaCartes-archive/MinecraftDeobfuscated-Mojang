package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;

@Environment(EnvType.CLIENT)
public class SkullModel extends Model {
	protected final ModelPart head;

	public SkullModel() {
		this(0, 35, 64, 64);
	}

	public SkullModel(int i, int j, int k, int l) {
		super(RenderType::entityTranslucent);
		this.texWidth = k;
		this.texHeight = l;
		this.head = new ModelPart(this, i, j);
		this.head.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F);
		this.head.setPos(0.0F, 0.0F, 0.0F);
	}

	public void setupAnim(float f, float g, float h) {
		this.head.yRot = g * (float) (Math.PI / 180.0);
		this.head.xRot = h * (float) (Math.PI / 180.0);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
		this.head.render(poseStack, vertexConsumer, i, j, f, g, h, k);
	}
}
