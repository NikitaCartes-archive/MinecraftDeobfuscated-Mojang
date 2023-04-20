package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Ordering;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

@Environment(EnvType.CLIENT)
public abstract class EffectRenderingInventoryScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
	public EffectRenderingInventoryScreen(T abstractContainerMenu, Inventory inventory, Component component) {
		super(abstractContainerMenu, inventory, component);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		this.renderEffects(guiGraphics, i, j);
	}

	public boolean canSeeEffects() {
		int i = this.leftPos + this.imageWidth + 2;
		int j = this.width - i;
		return j >= 32;
	}

	private void renderEffects(GuiGraphics guiGraphics, int i, int j) {
		int k = this.leftPos + this.imageWidth + 2;
		int l = this.width - k;
		Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
		if (!collection.isEmpty() && l >= 32) {
			boolean bl = l >= 120;
			int m = 33;
			if (collection.size() > 5) {
				m = 132 / (collection.size() - 1);
			}

			Iterable<MobEffectInstance> iterable = Ordering.natural().sortedCopy(collection);
			this.renderBackgrounds(guiGraphics, k, m, iterable, bl);
			this.renderIcons(guiGraphics, k, m, iterable, bl);
			if (bl) {
				this.renderLabels(guiGraphics, k, m, iterable);
			} else if (i >= k && i <= k + 33) {
				int n = this.topPos;
				MobEffectInstance mobEffectInstance = null;

				for (MobEffectInstance mobEffectInstance2 : iterable) {
					if (j >= n && j <= n + m) {
						mobEffectInstance = mobEffectInstance2;
					}

					n += m;
				}

				if (mobEffectInstance != null) {
					List<Component> list = List.of(this.getEffectName(mobEffectInstance), MobEffectUtil.formatDuration(mobEffectInstance, 1.0F));
					guiGraphics.renderTooltip(this.font, list, Optional.empty(), i, j);
				}
			}
		}
	}

	private void renderBackgrounds(GuiGraphics guiGraphics, int i, int j, Iterable<MobEffectInstance> iterable, boolean bl) {
		int k = this.topPos;

		for (MobEffectInstance mobEffectInstance : iterable) {
			if (bl) {
				guiGraphics.blit(INVENTORY_LOCATION, i, k, 0, 166, 120, 32);
			} else {
				guiGraphics.blit(INVENTORY_LOCATION, i, k, 0, 198, 32, 32);
			}

			k += j;
		}
	}

	private void renderIcons(GuiGraphics guiGraphics, int i, int j, Iterable<MobEffectInstance> iterable, boolean bl) {
		MobEffectTextureManager mobEffectTextureManager = this.minecraft.getMobEffectTextures();
		int k = this.topPos;

		for (MobEffectInstance mobEffectInstance : iterable) {
			MobEffect mobEffect = mobEffectInstance.getEffect();
			TextureAtlasSprite textureAtlasSprite = mobEffectTextureManager.get(mobEffect);
			guiGraphics.blit(i + (bl ? 6 : 7), k + 7, 0, 18, 18, textureAtlasSprite);
			k += j;
		}
	}

	private void renderLabels(GuiGraphics guiGraphics, int i, int j, Iterable<MobEffectInstance> iterable) {
		int k = this.topPos;

		for (MobEffectInstance mobEffectInstance : iterable) {
			Component component = this.getEffectName(mobEffectInstance);
			guiGraphics.drawString(this.font, component, i + 10 + 18, k + 6, 16777215);
			Component component2 = MobEffectUtil.formatDuration(mobEffectInstance, 1.0F);
			guiGraphics.drawString(this.font, component2, i + 10 + 18, k + 6 + 10, 8355711);
			k += j;
		}
	}

	private Component getEffectName(MobEffectInstance mobEffectInstance) {
		MutableComponent mutableComponent = mobEffectInstance.getEffect().getDisplayName().copy();
		if (mobEffectInstance.getAmplifier() >= 1 && mobEffectInstance.getAmplifier() <= 9) {
			mutableComponent.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + (mobEffectInstance.getAmplifier() + 1)));
		}

		return mutableComponent;
	}
}
