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
public class BreezeWindLayer extends RenderLayer<Breeze, BreezeModel<Breeze>> {
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/breeze/breeze_wind.png");
	private static final BreezeModel<Breeze> MODEL = new BreezeModel<>(BreezeModel.createBodyLayer(128, 128).bakeRoot());

	public BreezeWindLayer(RenderLayerParent<Breeze, BreezeModel<Breeze>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Breeze breeze, float f, float g, float h, float j, float k, float l) {
		float m = (float)breeze.tickCount + h;
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.breezeWind(TEXTURE_LOCATION, this.xOffset(m) % 1.0F, 0.0F));
		MODEL.setupAnim(breeze, f, g, j, k, l);
		BreezeRenderer.enable(MODEL, MODEL.wind()).renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
	}

	private float xOffset(float f) {
		return f * 0.02F;
	}
}
