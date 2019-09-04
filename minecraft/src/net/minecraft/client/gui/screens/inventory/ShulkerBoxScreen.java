package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ShulkerBoxMenu;

@Environment(EnvType.CLIENT)
public class ShulkerBoxScreen extends AbstractContainerScreen<ShulkerBoxMenu> {
	private static final ResourceLocation CONTAINER_TEXTURE = new ResourceLocation("textures/gui/container/shulker_box.png");

	public ShulkerBoxScreen(ShulkerBoxMenu shulkerBoxMenu, Inventory inventory, Component component) {
		super(shulkerBoxMenu, inventory, component);
		this.imageHeight++;
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		super.render(i, j, f);
		this.renderTooltip(i, j);
	}

	@Override
	protected void renderLabels(int i, int j) {
		this.font.draw(this.title.getColoredString(), 8.0F, 6.0F, 4210752);
		this.font.draw(this.inventory.getDisplayName().getColoredString(), 8.0F, (float)(this.imageHeight - 96 + 2), 4210752);
	}

	@Override
	protected void renderBg(float f, int i, int j) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(CONTAINER_TEXTURE);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		this.blit(k, l, 0, 0, this.imageWidth, this.imageHeight);
	}
}
