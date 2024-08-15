package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public class ElytraLayer<S extends HumanoidRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
	private static final ResourceLocation WINGS_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/elytra.png");
	private final ElytraModel elytraModel;
	private final ElytraModel elytraBabyModel;

	public ElytraLayer(RenderLayerParent<S, M> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.elytraModel = new ElytraModel(entityModelSet.bakeLayer(ModelLayers.ELYTRA));
		this.elytraBabyModel = new ElytraModel(entityModelSet.bakeLayer(ModelLayers.ELYTRA_BABY));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S humanoidRenderState, float f, float g) {
		if (humanoidRenderState.chestItem.is(Items.ELYTRA)) {
			ResourceLocation resourceLocation;
			if (humanoidRenderState instanceof PlayerRenderState playerRenderState) {
				PlayerSkin playerSkin = playerRenderState.skin;
				if (playerSkin.elytraTexture() != null) {
					resourceLocation = playerSkin.elytraTexture();
				} else if (playerSkin.capeTexture() != null && playerRenderState.showCape) {
					resourceLocation = playerSkin.capeTexture();
				} else {
					resourceLocation = WINGS_LOCATION;
				}
			} else {
				resourceLocation = WINGS_LOCATION;
			}

			ElytraModel elytraModel = humanoidRenderState.isBaby ? this.elytraBabyModel : this.elytraModel;
			poseStack.pushPose();
			poseStack.translate(0.0F, 0.0F, 0.125F);
			elytraModel.setupAnim(humanoidRenderState);
			VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(
				multiBufferSource, RenderType.armorCutoutNoCull(resourceLocation), humanoidRenderState.chestItem.hasFoil()
			);
			elytraModel.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
			poseStack.popPose();
		}
	}
}
