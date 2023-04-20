package net.minecraft.client.gui.screens.inventory.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ClientBundleTooltip implements ClientTooltipComponent {
	public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/container/bundle.png");
	private static final int MARGIN_Y = 4;
	private static final int BORDER_WIDTH = 1;
	private static final int TEX_SIZE = 128;
	private static final int SLOT_SIZE_X = 18;
	private static final int SLOT_SIZE_Y = 20;
	private final NonNullList<ItemStack> items;
	private final int weight;

	public ClientBundleTooltip(BundleTooltip bundleTooltip) {
		this.items = bundleTooltip.getItems();
		this.weight = bundleTooltip.getWeight();
	}

	@Override
	public int getHeight() {
		return this.gridSizeY() * 20 + 2 + 4;
	}

	@Override
	public int getWidth(Font font) {
		return this.gridSizeX() * 18 + 2;
	}

	@Override
	public void renderImage(Font font, int i, int j, GuiGraphics guiGraphics) {
		int k = this.gridSizeX();
		int l = this.gridSizeY();
		boolean bl = this.weight >= 64;
		int m = 0;

		for (int n = 0; n < l; n++) {
			for (int o = 0; o < k; o++) {
				int p = i + o * 18 + 1;
				int q = j + n * 20 + 1;
				this.renderSlot(p, q, m++, bl, guiGraphics, font);
			}
		}

		this.drawBorder(i, j, k, l, guiGraphics);
	}

	private void renderSlot(int i, int j, int k, boolean bl, GuiGraphics guiGraphics, Font font) {
		if (k >= this.items.size()) {
			this.blit(guiGraphics, i, j, bl ? ClientBundleTooltip.Texture.BLOCKED_SLOT : ClientBundleTooltip.Texture.SLOT);
		} else {
			ItemStack itemStack = this.items.get(k);
			this.blit(guiGraphics, i, j, ClientBundleTooltip.Texture.SLOT);
			guiGraphics.renderItem(itemStack, i + 1, j + 1, k);
			guiGraphics.renderItemDecorations(font, itemStack, i + 1, j + 1);
			if (k == 0) {
				AbstractContainerScreen.renderSlotHighlight(guiGraphics, i + 1, j + 1, 0);
			}
		}
	}

	private void drawBorder(int i, int j, int k, int l, GuiGraphics guiGraphics) {
		this.blit(guiGraphics, i, j, ClientBundleTooltip.Texture.BORDER_CORNER_TOP);
		this.blit(guiGraphics, i + k * 18 + 1, j, ClientBundleTooltip.Texture.BORDER_CORNER_TOP);

		for (int m = 0; m < k; m++) {
			this.blit(guiGraphics, i + 1 + m * 18, j, ClientBundleTooltip.Texture.BORDER_HORIZONTAL_TOP);
			this.blit(guiGraphics, i + 1 + m * 18, j + l * 20, ClientBundleTooltip.Texture.BORDER_HORIZONTAL_BOTTOM);
		}

		for (int m = 0; m < l; m++) {
			this.blit(guiGraphics, i, j + m * 20 + 1, ClientBundleTooltip.Texture.BORDER_VERTICAL);
			this.blit(guiGraphics, i + k * 18 + 1, j + m * 20 + 1, ClientBundleTooltip.Texture.BORDER_VERTICAL);
		}

		this.blit(guiGraphics, i, j + l * 20, ClientBundleTooltip.Texture.BORDER_CORNER_BOTTOM);
		this.blit(guiGraphics, i + k * 18 + 1, j + l * 20, ClientBundleTooltip.Texture.BORDER_CORNER_BOTTOM);
	}

	private void blit(GuiGraphics guiGraphics, int i, int j, ClientBundleTooltip.Texture texture) {
		guiGraphics.blit(TEXTURE_LOCATION, i, j, 0, (float)texture.x, (float)texture.y, texture.w, texture.h, 128, 128);
	}

	private int gridSizeX() {
		return Math.max(2, (int)Math.ceil(Math.sqrt((double)this.items.size() + 1.0)));
	}

	private int gridSizeY() {
		return (int)Math.ceil(((double)this.items.size() + 1.0) / (double)this.gridSizeX());
	}

	@Environment(EnvType.CLIENT)
	static enum Texture {
		SLOT(0, 0, 18, 20),
		BLOCKED_SLOT(0, 40, 18, 20),
		BORDER_VERTICAL(0, 18, 1, 20),
		BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
		BORDER_HORIZONTAL_BOTTOM(0, 60, 18, 1),
		BORDER_CORNER_TOP(0, 20, 1, 1),
		BORDER_CORNER_BOTTOM(0, 60, 1, 1);

		public final int x;
		public final int y;
		public final int w;
		public final int h;

		private Texture(int j, int k, int l, int m) {
			this.x = j;
			this.y = k;
			this.w = l;
			this.h = m;
		}
	}
}
