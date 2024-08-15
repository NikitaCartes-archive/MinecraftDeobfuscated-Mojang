package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.GrindstoneMenu;

@Environment(EnvType.CLIENT)
public class GrindstoneScreen extends AbstractContainerScreen<GrindstoneMenu> {
	private static final ResourceLocation ERROR_SPRITE = ResourceLocation.withDefaultNamespace("container/grindstone/error");
	private static final ResourceLocation GRINDSTONE_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/grindstone.png");

	public GrindstoneScreen(GrindstoneMenu grindstoneMenu, Inventory inventory, Component component) {
		super(grindstoneMenu, inventory, component);
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
		guiGraphics.blit(RenderType::guiTextured, GRINDSTONE_LOCATION, k, l, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
		if ((this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(2).hasItem()) {
			guiGraphics.blitSprite(RenderType::guiTextured, ERROR_SPRITE, k + 92, l + 31, 28, 21);
		}
	}
}
