package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.LlamaSpitModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.LlamaSpitRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.LlamaSpit;

@Environment(EnvType.CLIENT)
public class LlamaSpitRenderer extends EntityRenderer<LlamaSpit, LlamaSpitRenderState> {
	private static final ResourceLocation LLAMA_SPIT_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/llama/spit.png");
	private final LlamaSpitModel model;

	public LlamaSpitRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new LlamaSpitModel(context.bakeLayer(ModelLayers.LLAMA_SPIT));
	}

	public void render(LlamaSpitRenderState llamaSpitRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		poseStack.translate(0.0F, 0.15F, 0.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(llamaSpitRenderState.yRot - 90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(llamaSpitRenderState.xRot));
		this.model.setupAnim(llamaSpitRenderState);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(LLAMA_SPIT_LOCATION));
		this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
		poseStack.popPose();
		super.render(llamaSpitRenderState, poseStack, multiBufferSource, i);
	}

	public LlamaSpitRenderState createRenderState() {
		return new LlamaSpitRenderState();
	}

	public void extractRenderState(LlamaSpit llamaSpit, LlamaSpitRenderState llamaSpitRenderState, float f) {
		super.extractRenderState(llamaSpit, llamaSpitRenderState, f);
		llamaSpitRenderState.xRot = llamaSpit.getXRot(f);
		llamaSpitRenderState.yRot = llamaSpit.getYRot(f);
	}
}
