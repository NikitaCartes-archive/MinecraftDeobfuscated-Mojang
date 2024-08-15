package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.Parrot;

@Environment(EnvType.CLIENT)
public class ParrotOnShoulderLayer extends RenderLayer<PlayerRenderState, PlayerModel> {
	private final ParrotModel model;

	public ParrotOnShoulderLayer(RenderLayerParent<PlayerRenderState, PlayerModel> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.model = new ParrotModel(entityModelSet.bakeLayer(ModelLayers.PARROT));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, PlayerRenderState playerRenderState, float f, float g) {
		Parrot.Variant variant = playerRenderState.parrotOnLeftShoulder;
		if (variant != null) {
			this.renderOnShoulder(poseStack, multiBufferSource, i, playerRenderState, variant, f, g, true);
		}

		Parrot.Variant variant2 = playerRenderState.parrotOnRightShoulder;
		if (variant2 != null) {
			this.renderOnShoulder(poseStack, multiBufferSource, i, playerRenderState, variant2, f, g, false);
		}
	}

	private void renderOnShoulder(
		PoseStack poseStack, MultiBufferSource multiBufferSource, int i, PlayerRenderState playerRenderState, Parrot.Variant variant, float f, float g, boolean bl
	) {
		poseStack.pushPose();
		poseStack.translate(bl ? 0.4F : -0.4F, playerRenderState.isCrouching ? -1.3F : -1.5F, 0.0F);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(ParrotRenderer.getVariantTexture(variant)));
		this.model
			.renderOnShoulder(
				poseStack,
				vertexConsumer,
				i,
				OverlayTexture.NO_OVERLAY,
				playerRenderState.walkAnimationPos,
				playerRenderState.walkAnimationSpeed,
				f,
				g,
				playerRenderState.ageInTicks
			);
		poseStack.popPose();
	}
}
