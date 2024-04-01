package net.minecraft.client.gui.screens.inventory;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.FletchingMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.FletchingBlockEntity;

@Environment(EnvType.CLIENT)
public class FletchingScreen extends AbstractContainerScreen<FletchingMenu> {
	private static final ResourceLocation FLETCHING_PROGRESS_SPRITE = new ResourceLocation("container/fletching/progresss");
	private static final ResourceLocation FLETCHING_LOCATION = new ResourceLocation("textures/gui/container/fletching.png");
	private int processsTime = 100;
	private final long startTick;
	@Nullable
	private Component customTitle = null;
	private boolean wasExplored = false;

	public FletchingScreen(FletchingMenu fletchingMenu, Inventory inventory, Component component) {
		super(fletchingMenu, inventory, component);
		this.imageWidth += 320;
		this.inventoryLabelX += 160;
		this.startTick = Minecraft.getInstance().level.getGameTime();
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.getTitle())) / 2;
	}

	private Component makeTitle(char c, char d, char e, boolean bl) {
		Component component = Component.empty().append(FletchingBlockEntity.Resin.getQualityComponent(e), ", ", FletchingBlockEntity.Resin.getImpuritiesComponent(c));
		Component component2 = e >= 'j'
			? Component.translatable("item.minecraft.amber_gem")
			: Component.empty()
				.append(
					FletchingBlockEntity.Resin.getQualityComponent((char)(e + 1)),
					", ",
					bl ? FletchingBlockEntity.Resin.getImpuritiesComponent(d) : FletchingBlockEntity.Resin.getImpuritiesComponent("unknown")
				);
		return Component.translatable("screen.fletching.title", component, component2);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		int k = this.menu.getProcessTime();
		boolean bl = this.menu.isExplored();
		if (k != 0 && this.customTitle == null || bl != this.wasExplored) {
			this.customTitle = this.makeTitle(this.menu.getSourceImpurities(), this.menu.getResultImpurities(), this.menu.getSourceQuality(), bl);
			this.processsTime = k;
			this.wasExplored = bl;
			this.titleLabelX = (this.imageWidth - this.font.width(this.getTitle())) / 2;
		}

		super.render(guiGraphics, i, j, f);
		this.renderTooltip(guiGraphics, i, j);
	}

	@Override
	public Component getTitle() {
		return this.customTitle != null ? this.customTitle : super.getTitle();
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		long m = Minecraft.getInstance().level.getGameTime() - this.startTick;
		m = Math.max(0L, m - 20L);
		if (m > 160L) {
			guiGraphics.blit(FLETCHING_LOCATION, k, l, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 512, 512);
		} else {
			guiGraphics.blit(FLETCHING_LOCATION, k + 160, l + 4, 0, 160.0F, 4.0F, this.imageWidth - 320, this.imageHeight - 4, 512, 512);
			int n = 160 - (int)m;
			guiGraphics.blit(FLETCHING_LOCATION, k + n, l, 0, 0.0F, 0.0F, 164, 19, 512, 512);
			guiGraphics.blit(FLETCHING_LOCATION, k + this.imageWidth - 160 - n - 4, l, 0, (float)(this.imageWidth - 160 - 4), 0.0F, 164, 19, 512, 512);
			guiGraphics.blit(FLETCHING_LOCATION, k + 160 + 4, l, 0, 164.0F, 0.0F, this.imageWidth - 320 - 8, this.imageHeight, 512, 512);
		}

		int n = this.menu.getProgresss();
		if (n > 0) {
			float g = ((float)n + f) / (float)this.processsTime;
			double d = (Math.PI * 2) * (double)g;
			double e = (1.0 - Math.cos(d)) * 59.0;
			double h = Math.sin(2.0 * d) * 21.0;
			this.renderFloatingItem(guiGraphics, Items.FEATHER.getDefaultInstance(), (float)(k + 160 + 79 - 59) + (float)e, (float)(l + 38) + (float)h, (float)d);
			int o = (int)(21.0F * (1.0F - (float)n / (float)this.processsTime));
			if (o > 0) {
				guiGraphics.blitSprite(FLETCHING_PROGRESS_SPRITE, 9, 21, 0, 0, k + 160 + 83, l + 35, 9, o);
			}
		}
	}
}
