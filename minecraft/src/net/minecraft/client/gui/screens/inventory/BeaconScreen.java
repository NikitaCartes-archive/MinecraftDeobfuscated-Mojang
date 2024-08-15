package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
	private static final ResourceLocation BEACON_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/beacon.png");
	static final ResourceLocation BUTTON_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/button_disabled");
	static final ResourceLocation BUTTON_SELECTED_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/button_selected");
	static final ResourceLocation BUTTON_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/button_highlighted");
	static final ResourceLocation BUTTON_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/button");
	static final ResourceLocation CONFIRM_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/confirm");
	static final ResourceLocation CANCEL_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/cancel");
	private static final Component PRIMARY_EFFECT_LABEL = Component.translatable("block.minecraft.beacon.primary");
	private static final Component SECONDARY_EFFECT_LABEL = Component.translatable("block.minecraft.beacon.secondary");
	private final List<BeaconScreen.BeaconButton> beaconButtons = Lists.<BeaconScreen.BeaconButton>newArrayList();
	@Nullable
	Holder<MobEffect> primary;
	@Nullable
	Holder<MobEffect> secondary;

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
			int j = ((List)BeaconBlockEntity.BEACON_EFFECTS.get(i)).size();
			int k = j * 22 + (j - 1) * 2;

			for (int l = 0; l < j; l++) {
				Holder<MobEffect> holder = (Holder<MobEffect>)((List)BeaconBlockEntity.BEACON_EFFECTS.get(i)).get(l);
				BeaconScreen.BeaconPowerButton beaconPowerButton = new BeaconScreen.BeaconPowerButton(
					this.leftPos + 76 + l * 24 - k / 2, this.topPos + 22 + i * 25, holder, true, i
				);
				beaconPowerButton.active = false;
				this.addBeaconButton(beaconPowerButton);
			}
		}

		int i = 3;
		int j = ((List)BeaconBlockEntity.BEACON_EFFECTS.get(3)).size() + 1;
		int k = j * 22 + (j - 1) * 2;

		for (int l = 0; l < j - 1; l++) {
			Holder<MobEffect> holder = (Holder<MobEffect>)((List)BeaconBlockEntity.BEACON_EFFECTS.get(3)).get(l);
			BeaconScreen.BeaconPowerButton beaconPowerButton = new BeaconScreen.BeaconPowerButton(
				this.leftPos + 167 + l * 24 - k / 2, this.topPos + 47, holder, false, 3
			);
			beaconPowerButton.active = false;
			this.addBeaconButton(beaconPowerButton);
		}

		Holder<MobEffect> holder2 = (Holder<MobEffect>)((List)BeaconBlockEntity.BEACON_EFFECTS.get(0)).get(0);
		BeaconScreen.BeaconPowerButton beaconPowerButton2 = new BeaconScreen.BeaconUpgradePowerButton(
			this.leftPos + 167 + (j - 1) * 24 - k / 2, this.topPos + 47, holder2
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
	protected void renderLabels(GuiGraphics guiGraphics, int i, int j) {
		guiGraphics.drawCenteredString(this.font, PRIMARY_EFFECT_LABEL, 62, 10, 14737632);
		guiGraphics.drawCenteredString(this.font, SECONDARY_EFFECT_LABEL, 169, 10, 14737632);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		guiGraphics.blit(RenderType::guiTextured, BEACON_LOCATION, k, l, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
		guiGraphics.renderItem(new ItemStack(Items.NETHERITE_INGOT), k + 20, l + 109);
		guiGraphics.renderItem(new ItemStack(Items.EMERALD), k + 41, l + 109);
		guiGraphics.renderItem(new ItemStack(Items.DIAMOND), k + 41 + 22, l + 109);
		guiGraphics.renderItem(new ItemStack(Items.GOLD_INGOT), k + 42 + 44, l + 109);
		guiGraphics.renderItem(new ItemStack(Items.IRON_INGOT), k + 42 + 66, l + 109);
		guiGraphics.pose().popPose();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		this.renderTooltip(guiGraphics, i, j);
	}

	@Environment(EnvType.CLIENT)
	interface BeaconButton {
		void updateStatus(int i);
	}

	@Environment(EnvType.CLIENT)
	class BeaconCancelButton extends BeaconScreen.BeaconSpriteScreenButton {
		public BeaconCancelButton(final int i, final int j) {
			super(i, j, BeaconScreen.CANCEL_SPRITE, CommonComponents.GUI_CANCEL);
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
		public BeaconConfirmButton(final int i, final int j) {
			super(i, j, BeaconScreen.CONFIRM_SPRITE, CommonComponents.GUI_DONE);
		}

		@Override
		public void onPress() {
			BeaconScreen.this.minecraft
				.getConnection()
				.send(new ServerboundSetBeaconPacket(Optional.ofNullable(BeaconScreen.this.primary), Optional.ofNullable(BeaconScreen.this.secondary)));
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
		private Holder<MobEffect> effect;
		private TextureAtlasSprite sprite;

		public BeaconPowerButton(final int i, final int j, final Holder<MobEffect> holder, final boolean bl, final int k) {
			super(i, j);
			this.isPrimary = bl;
			this.tier = k;
			this.setEffect(holder);
		}

		protected void setEffect(Holder<MobEffect> holder) {
			this.effect = holder;
			this.sprite = Minecraft.getInstance().getMobEffectTextures().get(holder);
			this.setTooltip(Tooltip.create(this.createEffectDescription(holder), null));
		}

		protected MutableComponent createEffectDescription(Holder<MobEffect> holder) {
			return Component.translatable(holder.value().getDescriptionId());
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
		protected void renderIcon(GuiGraphics guiGraphics) {
			guiGraphics.blitSprite(RenderType::guiTextured, this.sprite, this.getX() + 2, this.getY() + 2, 18, 18);
		}

		@Override
		public void updateStatus(int i) {
			this.active = this.tier < i;
			this.setSelected(this.effect.equals(this.isPrimary ? BeaconScreen.this.primary : BeaconScreen.this.secondary));
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
			super(i, j, 22, 22, CommonComponents.EMPTY);
		}

		protected BeaconScreenButton(int i, int j, Component component) {
			super(i, j, 22, 22, component);
		}

		@Override
		public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
			ResourceLocation resourceLocation;
			if (!this.active) {
				resourceLocation = BeaconScreen.BUTTON_DISABLED_SPRITE;
			} else if (this.selected) {
				resourceLocation = BeaconScreen.BUTTON_SELECTED_SPRITE;
			} else if (this.isHoveredOrFocused()) {
				resourceLocation = BeaconScreen.BUTTON_HIGHLIGHTED_SPRITE;
			} else {
				resourceLocation = BeaconScreen.BUTTON_SPRITE;
			}

			guiGraphics.blitSprite(RenderType::guiTextured, resourceLocation, this.getX(), this.getY(), this.width, this.height);
			this.renderIcon(guiGraphics);
		}

		protected abstract void renderIcon(GuiGraphics guiGraphics);

		public boolean isSelected() {
			return this.selected;
		}

		public void setSelected(boolean bl) {
			this.selected = bl;
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
			this.defaultButtonNarrationText(narrationElementOutput);
		}
	}

	@Environment(EnvType.CLIENT)
	abstract static class BeaconSpriteScreenButton extends BeaconScreen.BeaconScreenButton {
		private final ResourceLocation sprite;

		protected BeaconSpriteScreenButton(int i, int j, ResourceLocation resourceLocation, Component component) {
			super(i, j, component);
			this.sprite = resourceLocation;
		}

		@Override
		protected void renderIcon(GuiGraphics guiGraphics) {
			guiGraphics.blitSprite(RenderType::guiTextured, this.sprite, this.getX() + 2, this.getY() + 2, 18, 18);
		}
	}

	@Environment(EnvType.CLIENT)
	class BeaconUpgradePowerButton extends BeaconScreen.BeaconPowerButton {
		public BeaconUpgradePowerButton(final int i, final int j, final Holder<MobEffect> holder) {
			super(i, j, holder, false, 3);
		}

		@Override
		protected MutableComponent createEffectDescription(Holder<MobEffect> holder) {
			return Component.translatable(holder.value().getDescriptionId()).append(" II");
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
