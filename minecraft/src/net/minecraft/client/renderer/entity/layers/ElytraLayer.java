package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
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

	public void render(T livingEntity, float f, float g, float h, float i, float j, float k, float l) {
		ItemStack itemStack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
		if (itemStack.getItem() == Items.ELYTRA) {
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			if (livingEntity instanceof AbstractClientPlayer) {
				AbstractClientPlayer abstractClientPlayer = (AbstractClientPlayer)livingEntity;
				if (abstractClientPlayer.isElytraLoaded() && abstractClientPlayer.getElytraTextureLocation() != null) {
					this.bindTexture(abstractClientPlayer.getElytraTextureLocation());
				} else if (abstractClientPlayer.isCapeLoaded()
					&& abstractClientPlayer.getCloakTextureLocation() != null
					&& abstractClientPlayer.isModelPartShown(PlayerModelPart.CAPE)) {
					this.bindTexture(abstractClientPlayer.getCloakTextureLocation());
				} else {
					this.bindTexture(WINGS_LOCATION);
				}
			} else {
				this.bindTexture(WINGS_LOCATION);
			}

			RenderSystem.pushMatrix();
			RenderSystem.translatef(0.0F, 0.0F, 0.125F);
			this.elytraModel.setupAnim(livingEntity, f, g, i, j, k, l);
			this.elytraModel.render(livingEntity, f, g, i, j, k, l);
			if (itemStack.isEnchanted()) {
				AbstractArmorLayer.renderFoil(this::bindTexture, livingEntity, this.elytraModel, f, g, h, i, j, k, l);
			}

			RenderSystem.disableBlend();
			RenderSystem.popMatrix();
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
