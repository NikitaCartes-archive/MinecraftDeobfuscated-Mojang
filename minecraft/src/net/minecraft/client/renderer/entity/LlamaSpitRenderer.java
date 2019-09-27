package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.LlamaSpitModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.LlamaSpit;

@Environment(EnvType.CLIENT)
public class LlamaSpitRenderer extends EntityRenderer<LlamaSpit> {
	private static final ResourceLocation LLAMA_SPIT_LOCATION = new ResourceLocation("textures/entity/llama/spit.png");
	private final LlamaSpitModel<LlamaSpit> model = new LlamaSpitModel<>();

	public LlamaSpitRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	public void render(LlamaSpit llamaSpit, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		poseStack.pushPose();
		poseStack.translate(0.0, 0.15F, 0.0);
		poseStack.mulPose(Vector3f.YP.rotation(Mth.lerp(h, llamaSpit.yRotO, llamaSpit.yRot) - 90.0F, true));
		poseStack.mulPose(Vector3f.ZP.rotation(Mth.lerp(h, llamaSpit.xRotO, llamaSpit.xRot), true));
		int i = llamaSpit.getLightColor();
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(LLAMA_SPIT_LOCATION));
		OverlayTexture.setDefault(vertexConsumer);
		this.model.setupAnim(llamaSpit, h, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
		this.model.renderToBuffer(poseStack, vertexConsumer, i);
		vertexConsumer.unsetDefaultOverlayCoords();
		poseStack.popPose();
		super.render(llamaSpit, d, e, f, g, h, poseStack, multiBufferSource);
	}

	public ResourceLocation getTextureLocation(LlamaSpit llamaSpit) {
		return LLAMA_SPIT_LOCATION;
	}
}
