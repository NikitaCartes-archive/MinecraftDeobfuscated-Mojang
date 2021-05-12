package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

@Environment(EnvType.CLIENT)
public abstract class EffectRenderingInventoryScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
	protected boolean doRenderEffects;

	public EffectRenderingInventoryScreen(T abstractContainerMenu, Inventory inventory, Component component) {
		super(abstractContainerMenu, inventory, component);
	}

	@Override
	protected void init() {
		super.init();
		this.checkEffectRendering();
	}

	protected void checkEffectRendering() {
		if (this.minecraft.player.getActiveEffects().isEmpty()) {
			this.leftPos = (this.width - this.imageWidth) / 2;
			this.doRenderEffects = false;
		} else {
			this.leftPos = 160 + (this.width - this.imageWidth - 200) / 2;
			this.doRenderEffects = true;
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		super.render(poseStack, i, j, f);
		if (this.doRenderEffects) {
			this.renderEffects(poseStack);
		}
	}

	private void renderEffects(PoseStack poseStack) {
		int i = this.leftPos - 124;
		Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
		if (!collection.isEmpty()) {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			int j = 33;
			if (collection.size() > 5) {
				j = 132 / (collection.size() - 1);
			}

			Iterable<MobEffectInstance> iterable = Ordering.natural().sortedCopy(collection);
			this.renderBackgrounds(poseStack, i, j, iterable);
			this.renderIcons(poseStack, i, j, iterable);
			this.renderLabels(poseStack, i, j, iterable);
		}
	}

	private void renderBackgrounds(PoseStack poseStack, int i, int j, Iterable<MobEffectInstance> iterable) {
		RenderSystem.setShaderTexture(0, INVENTORY_LOCATION);
		int k = this.topPos;

		for (MobEffectInstance mobEffectInstance : iterable) {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			this.blit(poseStack, i, k, 0, 166, 140, 32);
			k += j;
		}
	}

	private void renderIcons(PoseStack poseStack, int i, int j, Iterable<MobEffectInstance> iterable) {
		MobEffectTextureManager mobEffectTextureManager = this.minecraft.getMobEffectTextures();
		int k = this.topPos;

		for (MobEffectInstance mobEffectInstance : iterable) {
			MobEffect mobEffect = mobEffectInstance.getEffect();
			TextureAtlasSprite textureAtlasSprite = mobEffectTextureManager.get(mobEffect);
			RenderSystem.setShaderTexture(0, textureAtlasSprite.atlas().location());
			blit(poseStack, i + 6, k + 7, this.getBlitOffset(), 18, 18, textureAtlasSprite);
			k += j;
		}
	}

	private void renderLabels(PoseStack poseStack, int i, int j, Iterable<MobEffectInstance> iterable) {
		int k = this.topPos;

		for (MobEffectInstance mobEffectInstance : iterable) {
			String string = I18n.get(mobEffectInstance.getEffect().getDescriptionId());
			if (mobEffectInstance.getAmplifier() >= 1 && mobEffectInstance.getAmplifier() <= 9) {
				string = string + " " + I18n.get("enchantment.level." + (mobEffectInstance.getAmplifier() + 1));
			}

			this.font.drawShadow(poseStack, string, (float)(i + 10 + 18), (float)(k + 6), 16777215);
			String string2 = MobEffectUtil.formatDuration(mobEffectInstance, 1.0F);
			this.font.drawShadow(poseStack, string2, (float)(i + 10 + 18), (float)(k + 6 + 10), 8355711);
			k += j;
		}
	}
}
