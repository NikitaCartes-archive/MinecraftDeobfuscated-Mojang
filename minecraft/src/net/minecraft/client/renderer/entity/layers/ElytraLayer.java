package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public class ElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
	private static final ResourceLocation WINGS_LOCATION = new ResourceLocation("textures/entity/elytra.png");
	private final ElytraModel<T> elytraModel = new ElytraModel<>();

	public ElytraLayer(RenderLayerParent<T, M> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
		ItemStack itemStack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
		if (itemStack.getItem() == Items.ELYTRA) {
			ResourceLocation resourceLocation;
			if (livingEntity instanceof AbstractClientPlayer) {
				AbstractClientPlayer abstractClientPlayer = (AbstractClientPlayer)livingEntity;
				if (abstractClientPlayer.isElytraLoaded() && abstractClientPlayer.getElytraTextureLocation() != null) {
					resourceLocation = abstractClientPlayer.getElytraTextureLocation();
				} else if (abstractClientPlayer.isCapeLoaded()
					&& abstractClientPlayer.getCloakTextureLocation() != null
					&& abstractClientPlayer.isModelPartShown(PlayerModelPart.CAPE)) {
					resourceLocation = abstractClientPlayer.getCloakTextureLocation();
				} else {
					resourceLocation = WINGS_LOCATION;
				}
			} else {
				resourceLocation = WINGS_LOCATION;
			}

			poseStack.pushPose();
			poseStack.translate(0.0, 0.0, 0.125);
			this.getParentModel().copyPropertiesTo(this.elytraModel);
			this.elytraModel.setupAnim(livingEntity, f, g, j, k, l);
			VertexConsumer vertexConsumer = ItemRenderer.getFoilBuffer(multiBufferSource, this.elytraModel.renderType(resourceLocation), false, itemStack.hasFoil());
			this.elytraModel.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			poseStack.popPose();
		}
	}
}
