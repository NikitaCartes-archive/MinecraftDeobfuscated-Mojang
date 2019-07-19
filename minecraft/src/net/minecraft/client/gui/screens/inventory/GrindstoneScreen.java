package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
	protected void renderLabels(int i, int j) {
		this.font.draw(this.title.getColoredString(), 8.0F, 6.0F, 4210752);
		this.font.draw(this.inventory.getDisplayName().getColoredString(), 8.0F, (float)(this.imageHeight - 96 + 2), 4210752);
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.renderBg(f, i, j);
		super.render(i, j, f);
		this.renderTooltip(i, j);
	}

	@Override
	protected void renderBg(float f, int i, int j) {
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(GRINDSTONE_LOCATION);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		this.blit(k, l, 0, 0, this.imageWidth, this.imageHeight);
		if ((this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(2).hasItem()) {
			this.blit(k + 92, l + 31, this.imageWidth, 0, 28, 21);
		}
	}
}
