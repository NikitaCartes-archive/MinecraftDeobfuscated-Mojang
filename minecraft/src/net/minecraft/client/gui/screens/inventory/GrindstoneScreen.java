package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.GrindstoneMenu;

@Environment(EnvType.CLIENT)
public class GrindstoneScreen extends AbstractContainerScreen<GrindstoneMenu> {
	private static final ResourceLocation GRINDSTONE_LOCATION = new ResourceLocation("textures/gui/container/grindstone.png");

	public GrindstoneScreen(GrindstoneMenu grindstoneMenu, Inventory inventory, Component component) {
		super(grindstoneMenu, inventory, component);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderBackground(guiGraphics);
		this.renderBg(guiGraphics, f, i, j);
		super.render(guiGraphics, i, j, f);
		this.renderTooltip(guiGraphics, i, j);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		guiGraphics.blit(GRINDSTONE_LOCATION, k, l, 0, 0, this.imageWidth, this.imageHeight);
		if ((this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(2).hasItem()) {
			guiGraphics.blit(GRINDSTONE_LOCATION, k + 92, l + 31, this.imageWidth, 0, 28, 21);
		}
	}
}
