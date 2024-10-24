package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;

@Environment(EnvType.CLIENT)
public class ContainerScreen extends AbstractContainerScreen<ChestMenu> {
	private static final ResourceLocation CONTAINER_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
	private final int containerRows;

	public ContainerScreen(ChestMenu chestMenu, Inventory inventory, Component component) {
		super(chestMenu, inventory, component);
		int i = 222;
		int j = 114;
		this.containerRows = chestMenu.getRowCount();
		this.imageHeight = 114 + this.containerRows * 18;
		this.inventoryLabelY = this.imageHeight - 94;
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
		guiGraphics.blit(RenderType::guiTextured, CONTAINER_BACKGROUND, k, l, 0.0F, 0.0F, this.imageWidth, this.containerRows * 18 + 17, 256, 256);
		guiGraphics.blit(RenderType::guiTextured, CONTAINER_BACKGROUND, k, l + this.containerRows * 18 + 17, 0.0F, 126.0F, this.imageWidth, 96, 256, 256);
	}
}
