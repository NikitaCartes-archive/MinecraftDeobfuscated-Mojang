package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.LlamaSpitModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.LlamaSpit;

@Environment(EnvType.CLIENT)
public class LlamaSpitRenderer extends EntityRenderer<LlamaSpit> {
	private static final ResourceLocation LLAMA_SPIT_LOCATION = new ResourceLocation("textures/entity/llama/spit.png");
	private final LlamaSpitModel<LlamaSpit> model;

	public LlamaSpitRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new LlamaSpitModel<>(context.bakeLayer(ModelLayers.LLAMA_SPIT));
	}

	public void render(LlamaSpit llamaSpit, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		poseStack.translate(0.0, 0.15F, 0.0);
		poseStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(g, llamaSpit.yRotO, llamaSpit.yRot) - 90.0F));
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(g, llamaSpit.xRotO, llamaSpit.xRot)));
		this.model.setupAnim(llamaSpit, g, 0.0F, -0.1F, 0.0F, 0.0F);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(LLAMA_SPIT_LOCATION));
		this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		poseStack.popPose();
		super.render(llamaSpit, f, g, poseStack, multiBufferSource, i);
	}

	public ResourceLocation getTextureLocation(LlamaSpit llamaSpit) {
		return LLAMA_SPIT_LOCATION;
	}
}
