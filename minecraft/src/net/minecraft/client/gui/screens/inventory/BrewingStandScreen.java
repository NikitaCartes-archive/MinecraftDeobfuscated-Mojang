package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BrewingStandMenu;

@Environment(EnvType.CLIENT)
public class BrewingStandScreen extends AbstractContainerScreen<BrewingStandMenu> {
	private static final ResourceLocation BREWING_STAND_LOCATION = new ResourceLocation("textures/gui/container/brewing_stand.png");
	private static final int[] BUBBLELENGTHS = new int[]{29, 24, 20, 16, 11, 6, 0};

	public BrewingStandScreen(BrewingStandMenu brewingStandMenu, Inventory inventory, Component component) {
		super(brewingStandMenu, inventory, component);
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		super.render(i, j, f);
		this.renderTooltip(i, j);
	}

	@Override
	protected void renderLabels(int i, int j) {
		this.font.draw(this.title.getColoredString(), (float)(this.imageWidth / 2 - this.font.width(this.title.getColoredString()) / 2), 6.0F, 4210752);
		this.font.draw(this.inventory.getDisplayName().getColoredString(), 8.0F, (float)(this.imageHeight - 96 + 2), 4210752);
	}

	@Override
	protected void renderBg(float f, int i, int j) {
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(BREWING_STAND_LOCATION);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		this.blit(k, l, 0, 0, this.imageWidth, this.imageHeight);
		int m = this.menu.getFuel();
		int n = Mth.clamp((18 * m + 20 - 1) / 20, 0, 18);
		if (n > 0) {
			this.blit(k + 60, l + 44, 176, 29, n, 4);
		}

		int o = this.menu.getBrewingTicks();
		if (o > 0) {
			int p = (int)(28.0F * (1.0F - (float)o / 400.0F));
			if (p > 0) {
				this.blit(k + 97, l + 16, 176, 0, 9, p);
			}

			p = BUBBLELENGTHS[o / 2 % 7];
			if (p > 0) {
				this.blit(k + 63, l + 14 + 29 - p, 185, 29 - p, 12, p);
			}
		}
	}
}
