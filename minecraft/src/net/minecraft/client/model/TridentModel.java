package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class TridentModel extends Model {
	public static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/trident.png");
	private final ModelPart pole = new ModelPart(32, 32, 0, 6);

	public TridentModel() {
		super(RenderType::entitySolid);
		this.pole.addBox(-0.5F, 2.0F, -0.5F, 1.0F, 25.0F, 1.0F, 0.0F);
		ModelPart modelPart = new ModelPart(32, 32, 4, 0);
		modelPart.addBox(-1.5F, 0.0F, -0.5F, 3.0F, 2.0F, 1.0F);
		this.pole.addChild(modelPart);
		ModelPart modelPart2 = new ModelPart(32, 32, 4, 3);
		modelPart2.addBox(-2.5F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F);
		this.pole.addChild(modelPart2);
		ModelPart modelPart3 = new ModelPart(32, 32, 0, 0);
		modelPart3.addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, 0.0F);
		this.pole.addChild(modelPart3);
		ModelPart modelPart4 = new ModelPart(32, 32, 4, 3);
		modelPart4.mirror = true;
		modelPart4.addBox(1.5F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F);
		this.pole.addChild(modelPart4);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h) {
		this.pole.render(poseStack, vertexConsumer, i, j, null, f, g, h);
	}
}
