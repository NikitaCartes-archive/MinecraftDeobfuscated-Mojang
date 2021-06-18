package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

@Environment(EnvType.CLIENT)
public class BeaconScreen extends AbstractContainerScreen<BeaconMenu> {
	static final ResourceLocation BEACON_LOCATION = new ResourceLocation("textures/gui/container/beacon.png");
	private static final Component PRIMARY_EFFECT_LABEL = new TranslatableComponent("block.minecraft.beacon.primary");
	private static final Component SECONDARY_EFFECT_LABEL = new TranslatableComponent("block.minecraft.beacon.secondary");
	private final List<BeaconScreen.BeaconButton> beaconButtons = Lists.<BeaconScreen.BeaconButton>newArrayList();
	@Nullable
	MobEffect primary;
	@Nullable
	MobEffect secondary;

	public BeaconScreen(BeaconMenu beaconMenu, Inventory inventory, Component component) {
		super(beaconMenu, inventory, component);
		this.imageWidth = 230;
		this.imageHeight = 219;
		beaconMenu.addSlotListener(new ContainerListener() {
			@Override
			public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
			}

			@Override
			public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int j) {
				BeaconScreen.this.primary = beaconMenu.getPrimaryEffect();
				BeaconScreen.this.secondary = beaconMenu.getSecondaryEffect();
			}
		});
	}

	private <T extends AbstractWidget & BeaconScreen.BeaconButton> void addBeaconButton(T abstractWidget) {
		this.addRenderableWidget(abstractWidget);
		this.beaconButtons.add(abstractWidget);
	}

	@Override
	protected void init() {
		super.init();
		this.beaconButtons.clear();
		this.addBeaconButton(new BeaconScreen.BeaconConfirmButton(this.leftPos + 164, this.topPos + 107));
		this.addBeaconButton(new BeaconScreen.BeaconCancelButton(this.leftPos + 190, this.topPos + 107));

		for (int i = 0; i <= 2; i++) {
			int j = BeaconBlockEntity.BEACON_EFFECTS[i].length;
			int k = j * 22 + (j - 1) * 2;

			for (int l = 0; l < j; l++) {
				MobEffect mobEffect = BeaconBlockEntity.BEACON_EFFECTS[i][l];
				BeaconScreen.BeaconPowerButton beaconPowerButton = new BeaconScreen.BeaconPowerButton(
					this.leftPos + 76 + l * 24 - k / 2, this.topPos + 22 + i * 25, mobEffect, true, i
				);
				beaconPowerButton.active = false;
				this.addBeaconButton(beaconPowerButton);
			}
		}

		int i = 3;
		int j = BeaconBlockEntity.BEACON_EFFECTS[3].length + 1;
		int k = j * 22 + (j - 1) * 2;

		for (int l = 0; l < j - 1; l++) {
			MobEffect mobEffect = BeaconBlockEntity.BEACON_EFFECTS[3][l];
			BeaconScreen.BeaconPowerButton beaconPowerButton = new BeaconScreen.BeaconPowerButton(
				this.leftPos + 167 + l * 24 - k / 2, this.topPos + 47, mobEffect, false, 3
			);
			beaconPowerButton.active = false;
			this.addBeaconButton(beaconPowerButton);
		}

		BeaconScreen.BeaconPowerButton beaconPowerButton2 = new BeaconScreen.BeaconUpgradePowerButton(
			this.leftPos + 167 + (j - 1) * 24 - k / 2, this.topPos + 47, BeaconBlockEntity.BEACON_EFFECTS[0][0]
		);
		beaconPowerButton2.visible = false;
		this.addBeaconButton(beaconPowerButton2);
	}

	@Override
	public void containerTick() {
		super.containerTick();
		this.updateButtons();
	}

	void updateButtons() {
		int i = this.menu.getLevels();
		this.beaconButtons.forEach(beaconButton -> beaconButton.updateStatus(i));
	}

	@Override
	protected void renderLabels(PoseStack poseStack, int i, int j) {
		drawCenteredString(poseStack, this.font, PRIMARY_EFFECT_LABEL, 62, 10, 14737632);
		drawCenteredString(poseStack, this.font, SECONDARY_EFFECT_LABEL, 169, 10, 14737632);

		for (BeaconScreen.BeaconButton beaconButton : this.beaconButtons) {
			if (beaconButton.isShowingTooltip()) {
				beaconButton.renderToolTip(poseStack, i - this.leftPos, j - this.topPos);
				break;
			}
		}
	}

	@Override
	protected void renderBg(PoseStack poseStack, float f, int i, int j) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, BEACON_LOCATION);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		this.blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
		this.itemRenderer.blitOffset = 100.0F;
		this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.NETHERITE_INGOT), k + 20, l + 109);
		this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.EMERALD), k + 41, l + 109);
		this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.DIAMOND), k + 41 + 22, l + 109);
		this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.GOLD_INGOT), k + 42 + 44, l + 109);
		this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.IRON_INGOT), k + 42 + 66, l + 109);
		this.itemRenderer.blitOffset = 0.0F;
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		super.render(poseStack, i, j, f);
		this.renderTooltip(poseStack, i, j);
	}

	@Environment(EnvType.CLIENT)
	interface BeaconButton {
		boolean isShowingTooltip();

		void renderToolTip(PoseStack poseStack, int i, int j);

		void updateStatus(int i);
	}

	@Environment(EnvType.CLIENT)
	class BeaconCancelButton extends BeaconScreen.BeaconSpriteScreenButton {
		public BeaconCancelButton(int i, int j) {
			super(i, j, 112, 220, CommonComponents.GUI_CANCEL);
		}

		@Override
		public void onPress() {
			BeaconScreen.this.minecraft.player.closeContainer();
		}

		@Override
		public void updateStatus(int i) {
		}
	}

	@Environment(EnvType.CLIENT)
	class BeaconConfirmButton extends BeaconScreen.BeaconSpriteScreenButton {
		public BeaconConfirmButton(int i, int j) {
			super(i, j, 90, 220, CommonComponents.GUI_DONE);
		}

		@Override
		public void onPress() {
			BeaconScreen.this.minecraft
				.getConnection()
				.send(new ServerboundSetBeaconPacket(MobEffect.getId(BeaconScreen.this.primary), MobEffect.getId(BeaconScreen.this.secondary)));
			BeaconScreen.this.minecraft.player.closeContainer();
		}

		@Override
		public void updateStatus(int i) {
			this.active = BeaconScreen.this.menu.hasPayment() && BeaconScreen.this.primary != null;
		}
	}

	@Environment(EnvType.CLIENT)
	class BeaconPowerButton extends BeaconScreen.BeaconScreenButton {
		private final boolean isPrimary;
		protected final int tier;
		private MobEffect effect;
		private TextureAtlasSprite sprite;
		private Component tooltip;

		public BeaconPowerButton(int i, int j, MobEffect mobEffect, boolean bl, int k) {
			super(i, j);
			this.isPrimary = bl;
			this.tier = k;
			this.setEffect(mobEffect);
		}

		protected void setEffect(MobEffect mobEffect) {
			this.effect = mobEffect;
			this.sprite = Minecraft.getInstance().getMobEffectTextures().get(mobEffect);
			this.tooltip = this.createEffectDescription(mobEffect);
		}

		protected MutableComponent createEffectDescription(MobEffect mobEffect) {
			return new TranslatableComponent(mobEffect.getDescriptionId());
		}

		@Override
		public void onPress() {
			if (!this.isSelected()) {
				if (this.isPrimary) {
					BeaconScreen.this.primary = this.effect;
				} else {
					BeaconScreen.this.secondary = this.effect;
				}

				BeaconScreen.this.updateButtons();
			}
		}

		@Override
		public void renderToolTip(PoseStack poseStack, int i, int j) {
			BeaconScreen.this.renderTooltip(poseStack, this.tooltip, i, j);
		}

		@Override
		protected void renderIcon(PoseStack poseStack) {
			RenderSystem.setShaderTexture(0, this.sprite.atlas().location());
			blit(poseStack, this.x + 2, this.y + 2, this.getBlitOffset(), 18, 18, this.sprite);
		}

		@Override
		public void updateStatus(int i) {
			this.active = this.tier < i;
			this.setSelected(this.effect == (this.isPrimary ? BeaconScreen.this.primary : BeaconScreen.this.secondary));
		}

		@Override
		protected MutableComponent createNarrationMessage() {
			return this.createEffectDescription(this.effect);
		}
	}

	@Environment(EnvType.CLIENT)
	abstract static class BeaconScreenButton extends AbstractButton implements BeaconScreen.BeaconButton {
		private boolean selected;

		protected BeaconScreenButton(int i, int j) {
			super(i, j, 22, 22, TextComponent.EMPTY);
		}

		protected BeaconScreenButton(int i, int j, Component component) {
			super(i, j, 22, 22, component);
		}

		@Override
		public void renderButton(PoseStack poseStack, int i, int j, float f) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, BeaconScreen.BEACON_LOCATION);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			int k = 219;
			int l = 0;
			if (!this.active) {
				l += this.width * 2;
			} else if (this.selected) {
				l += this.width * 1;
			} else if (this.isHovered()) {
				l += this.width * 3;
			}

			this.blit(poseStack, this.x, this.y, l, 219, this.width, this.height);
			this.renderIcon(poseStack);
		}

		protected abstract void renderIcon(PoseStack poseStack);

		public boolean isSelected() {
			return this.selected;
		}

		public void setSelected(boolean bl) {
			this.selected = bl;
		}

		@Override
		public boolean isShowingTooltip() {
			return this.isHovered;
		}

		@Override
		public void updateNarration(NarrationElementOutput narrationElementOutput) {
			this.defaultButtonNarrationText(narrationElementOutput);
		}
	}

	@Environment(EnvType.CLIENT)
	abstract class BeaconSpriteScreenButton extends BeaconScreen.BeaconScreenButton {
		private final int iconX;
		private final int iconY;

		protected BeaconSpriteScreenButton(int i, int j, int k, int l, Component component) {
			super(i, j, component);
			this.iconX = k;
			this.iconY = l;
		}

		@Override
		protected void renderIcon(PoseStack poseStack) {
			this.blit(poseStack, this.x + 2, this.y + 2, this.iconX, this.iconY, 18, 18);
		}

		@Override
		public void renderToolTip(PoseStack poseStack, int i, int j) {
			BeaconScreen.this.renderTooltip(poseStack, BeaconScreen.this.title, i, j);
		}
	}

	@Environment(EnvType.CLIENT)
	class BeaconUpgradePowerButton extends BeaconScreen.BeaconPowerButton {
		public BeaconUpgradePowerButton(int i, int j, MobEffect mobEffect) {
			super(i, j, mobEffect, false, 3);
		}

		@Override
		protected MutableComponent createEffectDescription(MobEffect mobEffect) {
			return new TranslatableComponent(mobEffect.getDescriptionId()).append(" II");
		}

		@Override
		public void updateStatus(int i) {
			if (BeaconScreen.this.primary != null) {
				this.visible = true;
				this.setEffect(BeaconScreen.this.primary);
				super.updateStatus(i);
			} else {
				this.visible = false;
			}
		}
	}
}
