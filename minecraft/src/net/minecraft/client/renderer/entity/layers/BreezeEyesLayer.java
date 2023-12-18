package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BreezeModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.BreezeRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.breeze.Breeze;

@Environment(EnvType.CLIENT)
public class BreezeEyesLayer extends RenderLayer<Breeze, BreezeModel<Breeze>> {
	private static final RenderType BREEZE_EYES = RenderType.breezeEyes(new ResourceLocation("textures/entity/breeze/breeze_eyes.png"));

	public BreezeEyesLayer(RenderLayerParent<Breeze, BreezeModel<Breeze>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Breeze breeze, float f, float g, float h, float j, float k, float l) {
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(BREEZE_EYES);
		BreezeModel<Breeze> breezeModel = this.getParentModel();
		BreezeRenderer.enable(breezeModel, breezeModel.head(), breezeModel.eyes())
			.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
	}
}
