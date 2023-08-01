package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BrewingStandMenu;

@Environment(EnvType.CLIENT)
public class BrewingStandScreen extends AbstractContainerScreen<BrewingStandMenu> {
	private static final ResourceLocation FUEL_LENGTH_SPRITE = new ResourceLocation("container/brewing_stand/fuel_length");
	private static final ResourceLocation BREW_PROGRESS_SPRITE = new ResourceLocation("container/brewing_stand/brew_progress");
	private static final ResourceLocation BUBBLES_SPRITE = new ResourceLocation("container/brewing_stand/bubbles");
	private static final ResourceLocation BREWING_STAND_LOCATION = new ResourceLocation("textures/gui/container/brewing_stand.png");
	private static final int[] BUBBLELENGTHS = new int[]{29, 24, 20, 16, 11, 6, 0};

	public BrewingStandScreen(BrewingStandMenu brewingStandMenu, Inventory inventory, Component component) {
		super(brewingStandMenu, inventory, component);
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
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
		guiGraphics.blit(BREWING_STAND_LOCATION, k, l, 0, 0, this.imageWidth, this.imageHeight);
		int m = this.menu.getFuel();
		int n = Mth.clamp((18 * m + 20 - 1) / 20, 0, 18);
		if (n > 0) {
			guiGraphics.blitSprite(FUEL_LENGTH_SPRITE, 18, 4, 0, 0, k + 60, l + 44, n, 4);
		}

		int o = this.menu.getBrewingTicks();
		if (o > 0) {
			int p = (int)(28.0F * (1.0F - (float)o / 400.0F));
			if (p > 0) {
				guiGraphics.blitSprite(BREW_PROGRESS_SPRITE, 9, 28, 0, 0, k + 97, l + 16, 9, p);
			}

			p = BUBBLELENGTHS[o / 2 % 7];
			if (p > 0) {
				guiGraphics.blitSprite(BUBBLES_SPRITE, 12, 29, 0, 29 - p, k + 63, l + 14 + 29 - p, 12, p);
			}
		}
	}
}
