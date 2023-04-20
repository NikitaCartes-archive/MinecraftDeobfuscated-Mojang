package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public abstract class ItemCombinerScreen<T extends ItemCombinerMenu> extends AbstractContainerScreen<T> implements ContainerListener {
	private final ResourceLocation menuResource;

	public ItemCombinerScreen(T itemCombinerMenu, Inventory inventory, Component component, ResourceLocation resourceLocation) {
		super(itemCombinerMenu, inventory, component);
		this.menuResource = resourceLocation;
	}

	protected void subInit() {
	}

	@Override
	protected void init() {
		super.init();
		this.subInit();
		this.menu.addSlotListener(this);
	}

	@Override
	public void removed() {
		super.removed();
		this.menu.removeSlotListener(this);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderBackground(guiGraphics);
		super.render(guiGraphics, i, j, f);
		this.renderFg(guiGraphics, i, j, f);
		this.renderTooltip(guiGraphics, i, j);
	}

	protected void renderFg(GuiGraphics guiGraphics, int i, int j, float f) {
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
		guiGraphics.blit(this.menuResource, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		this.renderErrorIcon(guiGraphics, this.leftPos, this.topPos);
	}

	protected abstract void renderErrorIcon(GuiGraphics guiGraphics, int i, int j);

	@Override
	public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int j) {
	}

	@Override
	public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
	}
}
