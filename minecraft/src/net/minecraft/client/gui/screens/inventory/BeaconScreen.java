package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

@Environment(EnvType.CLIENT)
public class BeaconScreen extends AbstractContainerScreen<BeaconMenu> {
	private static final ResourceLocation BEACON_LOCATION = new ResourceLocation("textures/gui/container/beacon.png");
	private BeaconScreen.BeaconConfirmButton confirmButton;
	private boolean initPowerButtons;
	private MobEffect primary;
	private MobEffect secondary;

	public BeaconScreen(BeaconMenu beaconMenu, Inventory inventory, Component component) {
		super(beaconMenu, inventory, component);
		this.imageWidth = 230;
		this.imageHeight = 219;
		beaconMenu.addSlotListener(new ContainerListener() {
			@Override
			public void refreshContainer(AbstractContainerMenu abstractContainerMenu, NonNullList<ItemStack> nonNullList) {
			}

			@Override
			public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
			}

			@Override
			public void setContainerData(AbstractContainerMenu abstractContainerMenu, int i, int j) {
				BeaconScreen.this.primary = beaconMenu.getPrimaryEffect();
				BeaconScreen.this.secondary = beaconMenu.getSecondaryEffect();
				BeaconScreen.this.initPowerButtons = true;
			}
		});
	}

	@Override
	protected void init() {
		super.init();
		this.confirmButton = this.addButton(new BeaconScreen.BeaconConfirmButton(this.leftPos + 164, this.topPos + 107));
		this.addButton(new BeaconScreen.BeaconCancelButton(this.leftPos + 190, this.topPos + 107));
		this.initPowerButtons = true;
		this.confirmButton.active = false;
	}

	@Override
	public void tick() {
		super.tick();
		int i = this.menu.getLevels();
		if (this.initPowerButtons && i >= 0) {
			this.initPowerButtons = false;

			for (int j = 0; j <= 2; j++) {
				int k = BeaconBlockEntity.BEACON_EFFECTS[j].length;
				int l = k * 22 + (k - 1) * 2;

				for (int m = 0; m < k; m++) {
					MobEffect mobEffect = BeaconBlockEntity.BEACON_EFFECTS[j][m];
					BeaconScreen.BeaconPowerButton beaconPowerButton = new BeaconScreen.BeaconPowerButton(
						this.leftPos + 76 + m * 24 - l / 2, this.topPos + 22 + j * 25, mobEffect, true
					);
					this.addButton(beaconPowerButton);
					if (j >= i) {
						beaconPowerButton.active = false;
					} else if (mobEffect == this.primary) {
						beaconPowerButton.setSelected(true);
					}
				}
			}

			int j = 3;
			int k = BeaconBlockEntity.BEACON_EFFECTS[3].length + 1;
			int l = k * 22 + (k - 1) * 2;

			for (int mx = 0; mx < k - 1; mx++) {
				MobEffect mobEffect = BeaconBlockEntity.BEACON_EFFECTS[3][mx];
				BeaconScreen.BeaconPowerButton beaconPowerButton = new BeaconScreen.BeaconPowerButton(
					this.leftPos + 167 + mx * 24 - l / 2, this.topPos + 47, mobEffect, false
				);
				this.addButton(beaconPowerButton);
				if (3 >= i) {
					beaconPowerButton.active = false;
				} else if (mobEffect == this.secondary) {
					beaconPowerButton.setSelected(true);
				}
			}

			if (this.primary != null) {
				BeaconScreen.BeaconPowerButton beaconPowerButton2 = new BeaconScreen.BeaconPowerButton(
					this.leftPos + 167 + (k - 1) * 24 - l / 2, this.topPos + 47, this.primary, false
				);
				this.addButton(beaconPowerButton2);
				if (3 >= i) {
					beaconPowerButton2.active = false;
				} else if (this.primary == this.secondary) {
					beaconPowerButton2.setSelected(true);
				}
			}
		}

		this.confirmButton.active = this.menu.hasPayment() && this.primary != null;
	}

	@Override
	protected void renderLabels(int i, int j) {
		this.drawCenteredString(this.font, I18n.get("block.minecraft.beacon.primary"), 62, 10, 14737632);
		this.drawCenteredString(this.font, I18n.get("block.minecraft.beacon.secondary"), 169, 10, 14737632);

		for (AbstractWidget abstractWidget : this.buttons) {
			if (abstractWidget.isHovered()) {
				abstractWidget.renderToolTip(i - this.leftPos, j - this.topPos);
				break;
			}
		}
	}

	@Override
	protected void renderBg(float f, int i, int j) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(BEACON_LOCATION);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		this.blit(k, l, 0, 0, this.imageWidth, this.imageHeight);
		this.itemRenderer.blitOffset = 100.0F;
		this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.EMERALD), k + 42, l + 109);
		this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.DIAMOND), k + 42 + 22, l + 109);
		this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.GOLD_INGOT), k + 42 + 44, l + 109);
		this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.IRON_INGOT), k + 42 + 66, l + 109);
		this.itemRenderer.blitOffset = 0.0F;
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		super.render(i, j, f);
		this.renderTooltip(i, j);
	}

	@Environment(EnvType.CLIENT)
	class BeaconCancelButton extends BeaconScreen.BeaconSpriteScreenButton {
		public BeaconCancelButton(int i, int j) {
			super(i, j, 112, 220);
		}

		@Override
		public void onPress() {
			BeaconScreen.this.minecraft.player.connection.send(new ServerboundContainerClosePacket(BeaconScreen.this.minecraft.player.containerMenu.containerId));
			BeaconScreen.this.minecraft.setScreen(null);
		}

		@Override
		public void renderToolTip(int i, int j) {
			BeaconScreen.this.renderTooltip(I18n.get("gui.cancel"), i, j);
		}
	}

	@Environment(EnvType.CLIENT)
	class BeaconConfirmButton extends BeaconScreen.BeaconSpriteScreenButton {
		public BeaconConfirmButton(int i, int j) {
			super(i, j, 90, 220);
		}

		@Override
		public void onPress() {
			BeaconScreen.this.minecraft
				.getConnection()
				.send(new ServerboundSetBeaconPacket(MobEffect.getId(BeaconScreen.this.primary), MobEffect.getId(BeaconScreen.this.secondary)));
			BeaconScreen.this.minecraft.player.connection.send(new ServerboundContainerClosePacket(BeaconScreen.this.minecraft.player.containerMenu.containerId));
			BeaconScreen.this.minecraft.setScreen(null);
		}

		@Override
		public void renderToolTip(int i, int j) {
			BeaconScreen.this.renderTooltip(I18n.get("gui.done"), i, j);
		}
	}

	@Environment(EnvType.CLIENT)
	class BeaconPowerButton extends BeaconScreen.BeaconScreenButton {
		private final MobEffect effect;
		private final TextureAtlasSprite sprite;
		private final boolean isPrimary;

		public BeaconPowerButton(int i, int j, MobEffect mobEffect, boolean bl) {
			super(i, j);
			this.effect = mobEffect;
			this.sprite = Minecraft.getInstance().getMobEffectTextures().get(mobEffect);
			this.isPrimary = bl;
		}

		@Override
		public void onPress() {
			if (!this.isSelected()) {
				if (this.isPrimary) {
					BeaconScreen.this.primary = this.effect;
				} else {
					BeaconScreen.this.secondary = this.effect;
				}

				BeaconScreen.this.buttons.clear();
				BeaconScreen.this.children.clear();
				BeaconScreen.this.init();
				BeaconScreen.this.tick();
			}
		}

		@Override
		public void renderToolTip(int i, int j) {
			String string = I18n.get(this.effect.getDescriptionId());
			if (!this.isPrimary && this.effect != MobEffects.REGENERATION) {
				string = string + " II";
			}

			BeaconScreen.this.renderTooltip(string, i, j);
		}

		@Override
		protected void renderIcon() {
			Minecraft.getInstance().getTextureManager().bind(TextureAtlas.LOCATION_MOB_EFFECTS);
			blit(this.x + 2, this.y + 2, this.getBlitOffset(), 18, 18, this.sprite);
		}
	}

	@Environment(EnvType.CLIENT)
	abstract static class BeaconScreenButton extends AbstractButton {
		private boolean selected;

		protected BeaconScreenButton(int i, int j) {
			super(i, j, 22, 22, "");
		}

		@Override
		public void renderButton(int i, int j, float f) {
			Minecraft.getInstance().getTextureManager().bind(BeaconScreen.BEACON_LOCATION);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			int k = 219;
			int l = 0;
			if (!this.active) {
				l += this.width * 2;
			} else if (this.selected) {
				l += this.width * 1;
			} else if (this.isHovered()) {
				l += this.width * 3;
			}

			this.blit(this.x, this.y, l, 219, this.width, this.height);
			this.renderIcon();
		}

		protected abstract void renderIcon();

		public boolean isSelected() {
			return this.selected;
		}

		public void setSelected(boolean bl) {
			this.selected = bl;
		}
	}

	@Environment(EnvType.CLIENT)
	abstract static class BeaconSpriteScreenButton extends BeaconScreen.BeaconScreenButton {
		private final int iconX;
		private final int iconY;

		protected BeaconSpriteScreenButton(int i, int j, int k, int l) {
			super(i, j);
			this.iconX = k;
			this.iconY = l;
		}

		@Override
		protected void renderIcon() {
			this.blit(this.x + 2, this.y + 2, this.iconX, this.iconY, 18, 18);
		}
	}
}
