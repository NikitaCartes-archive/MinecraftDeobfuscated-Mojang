package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.CrafterSlot;
import net.minecraft.world.inventory.Slot;

@Environment(EnvType.CLIENT)
public class CrafterScreen extends AbstractContainerScreen<CrafterMenu> {
	private static final ResourceLocation DISABLED_SLOT_LOCATION_SPRITE = new ResourceLocation("container/crafter/disabled_slot");
	private static final ResourceLocation POWERED_REDSTONE_LOCATION_SPRITE = new ResourceLocation("container/crafter/powered_redstone");
	private static final ResourceLocation UNPOWERED_REDSTONE_LOCATION_SPRITE = new ResourceLocation("container/crafter/unpowered_redstone");
	private static final ResourceLocation CONTAINER_LOCATION = new ResourceLocation("textures/gui/container/crafter.png");
	private static final Component DISABLED_SLOT_TOOLTIP = Component.translatable("gui.togglable_slot");
	private final Player player;

	public CrafterScreen(CrafterMenu crafterMenu, Inventory inventory, Component component) {
		super(crafterMenu, inventory, component);
		this.player = inventory.player;
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
	}

	@Override
	protected void slotClicked(Slot slot, int i, int j, ClickType clickType) {
		if (this.player.isSpectator()) {
			super.slotClicked(slot, i, j, clickType);
		} else {
			if (i > -1 && i < 9 && slot instanceof CrafterSlot) {
				if (slot.hasItem()) {
					super.slotClicked(slot, i, j, clickType);
					return;
				}

				boolean bl = this.menu.isSlotDisabled(i);
				if (bl || this.menu.getCarried().isEmpty()) {
					this.menu.setSlotState(i, bl);
					super.handleSlotStateChanged(i, this.menu.containerId, bl);
					if (bl) {
						this.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4F, 1.0F);
					} else {
						this.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4F, 0.75F);
					}
				}
			}

			super.slotClicked(slot, i, j, clickType);
		}
	}

	@Override
	public void renderSlot(GuiGraphics guiGraphics, Slot slot) {
		if (slot instanceof CrafterSlot crafterSlot && this.menu.isSlotDisabled(slot.index)) {
			this.renderDisabledSlot(guiGraphics, crafterSlot);
			return;
		}

		super.renderSlot(guiGraphics, slot);
	}

	private void renderDisabledSlot(GuiGraphics guiGraphics, CrafterSlot crafterSlot) {
		guiGraphics.blitSprite(DISABLED_SLOT_LOCATION_SPRITE, crafterSlot.x - 1, crafterSlot.y - 1, 18, 18);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		this.renderRedstone(guiGraphics);
		this.renderTooltip(guiGraphics, i, j);
		if (this.hoveredSlot instanceof CrafterSlot
			&& !this.menu.isSlotDisabled(this.hoveredSlot.index)
			&& this.menu.getCarried().isEmpty()
			&& !this.hoveredSlot.hasItem()) {
			guiGraphics.renderTooltip(this.font, DISABLED_SLOT_TOOLTIP, i, j);
		}
	}

	private void renderRedstone(GuiGraphics guiGraphics) {
		int i = this.width / 2 + 9;
		int j = this.height / 2 - 48;
		ResourceLocation resourceLocation;
		if (this.menu.isPowered()) {
			resourceLocation = POWERED_REDSTONE_LOCATION_SPRITE;
		} else {
			resourceLocation = UNPOWERED_REDSTONE_LOCATION_SPRITE;
		}

		guiGraphics.blitSprite(resourceLocation, i, j, 16, 16);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		guiGraphics.blit(CONTAINER_LOCATION, k, l, 0, 0, this.imageWidth, this.imageHeight);
	}
}
