package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ShulkerBoxMenu;

@Environment(EnvType.CLIENT)
public class ShulkerBoxScreen extends AbstractContainerScreen<ShulkerBoxMenu> {
	private static final ResourceLocation CONTAINER_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/shulker_box.png");

	public ShulkerBoxScreen(ShulkerBoxMenu shulkerBoxMenu, Inventory inventory, Component component) {
		super(shulkerBoxMenu, inventory, component);
		this.imageHeight++;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		this.renderTooltip(guiGraphics, i, j);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		guiGraphics.blit(CONTAINER_TEXTURE, k, l, 0, 0, this.imageWidth, this.imageHeight);
	}
}
