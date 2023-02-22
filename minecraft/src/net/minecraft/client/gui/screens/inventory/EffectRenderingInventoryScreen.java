package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
	public void render(PoseStack poseStack, int i, int j, float f) {
		super.render(poseStack, i, j, f);
		this.renderEffects(poseStack, i, j);
	}

	public boolean canSeeEffects() {
		int i = this.leftPos + this.imageWidth + 2;
		int j = this.width - i;
		return j >= 32;
	}

	private void renderEffects(PoseStack poseStack, int i, int j) {
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
			this.renderBackgrounds(poseStack, k, m, iterable, bl);
			this.renderIcons(poseStack, k, m, iterable, bl);
			if (bl) {
				this.renderLabels(poseStack, k, m, iterable);
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
					this.renderTooltip(poseStack, list, Optional.empty(), i, j);
				}
			}
		}
	}

	private void renderBackgrounds(PoseStack poseStack, int i, int j, Iterable<MobEffectInstance> iterable, boolean bl) {
		RenderSystem.setShaderTexture(0, INVENTORY_LOCATION);
		int k = this.topPos;

		for (MobEffectInstance mobEffectInstance : iterable) {
			if (bl) {
				blit(poseStack, i, k, 0, 166, 120, 32);
			} else {
				blit(poseStack, i, k, 0, 198, 32, 32);
			}

			k += j;
		}
	}

	private void renderIcons(PoseStack poseStack, int i, int j, Iterable<MobEffectInstance> iterable, boolean bl) {
		MobEffectTextureManager mobEffectTextureManager = this.minecraft.getMobEffectTextures();
		int k = this.topPos;

		for (MobEffectInstance mobEffectInstance : iterable) {
			MobEffect mobEffect = mobEffectInstance.getEffect();
			TextureAtlasSprite textureAtlasSprite = mobEffectTextureManager.get(mobEffect);
			RenderSystem.setShaderTexture(0, textureAtlasSprite.atlasLocation());
			blit(poseStack, i + (bl ? 6 : 7), k + 7, 0, 18, 18, textureAtlasSprite);
			k += j;
		}
	}

	private void renderLabels(PoseStack poseStack, int i, int j, Iterable<MobEffectInstance> iterable) {
		int k = this.topPos;

		for (MobEffectInstance mobEffectInstance : iterable) {
			Component component = this.getEffectName(mobEffectInstance);
			this.font.drawShadow(poseStack, component, (float)(i + 10 + 18), (float)(k + 6), 16777215);
			Component component2 = MobEffectUtil.formatDuration(mobEffectInstance, 1.0F);
			this.font.drawShadow(poseStack, component2, (float)(i + 10 + 18), (float)(k + 6 + 10), 8355711);
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
