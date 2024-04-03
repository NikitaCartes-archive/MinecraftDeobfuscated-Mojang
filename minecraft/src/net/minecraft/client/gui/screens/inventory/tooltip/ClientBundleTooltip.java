package net.minecraft.client.gui.screens.inventory.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;

@Environment(EnvType.CLIENT)
public class ClientBundleTooltip implements ClientTooltipComponent {
	private static final ResourceLocation BACKGROUND_SPRITE = new ResourceLocation("container/bundle/background");
	private static final int MARGIN_Y = 4;
	private static final int BORDER_WIDTH = 1;
	private static final int SLOT_SIZE_X = 18;
	private static final int SLOT_SIZE_Y = 20;
	private final BundleContents contents;

	public ClientBundleTooltip(BundleContents bundleContents) {
		this.contents = bundleContents;
	}

	@Override
	public int getHeight() {
		return this.backgroundHeight() + 4;
	}

	@Override
	public int getWidth(Font font) {
		return this.backgroundWidth();
	}

	private int backgroundWidth() {
		return this.gridSizeX() * 18 + 2;
	}

	private int backgroundHeight() {
		return this.gridSizeY() * 20 + 2;
	}

	@Override
	public void renderImage(Font font, int i, int j, GuiGraphics guiGraphics) {
		int k = this.gridSizeX();
		int l = this.gridSizeY();
		guiGraphics.blitSprite(BACKGROUND_SPRITE, i, j, this.backgroundWidth(), this.backgroundHeight());
		boolean bl = this.contents.weight().compareTo(Fraction.ONE) >= 0;
		int m = 0;

		for (int n = 0; n < l; n++) {
			for (int o = 0; o < k; o++) {
				int p = i + o * 18 + 1;
				int q = j + n * 20 + 1;
				this.renderSlot(p, q, m++, bl, guiGraphics, font);
			}
		}
	}

	private void renderSlot(int i, int j, int k, boolean bl, GuiGraphics guiGraphics, Font font) {
		if (k >= this.contents.size()) {
			this.blit(guiGraphics, i, j, bl ? ClientBundleTooltip.Texture.BLOCKED_SLOT : ClientBundleTooltip.Texture.SLOT);
		} else {
			ItemStack itemStack = this.contents.getItemUnsafe(k);
			this.blit(guiGraphics, i, j, ClientBundleTooltip.Texture.SLOT);
			guiGraphics.renderItem(itemStack, i + 1, j + 1, k);
			guiGraphics.renderItemDecorations(font, itemStack, i + 1, j + 1);
			if (k == 0) {
				AbstractContainerScreen.renderSlotHighlight(guiGraphics, i + 1, j + 1, 0);
			}
		}
	}

	private void blit(GuiGraphics guiGraphics, int i, int j, ClientBundleTooltip.Texture texture) {
		guiGraphics.blitSprite(texture.sprite, i, j, 0, texture.w, texture.h);
	}

	private int gridSizeX() {
		return Math.max(2, (int)Math.ceil(Math.sqrt((double)this.contents.size() + 1.0)));
	}

	private int gridSizeY() {
		return (int)Math.ceil(((double)this.contents.size() + 1.0) / (double)this.gridSizeX());
	}

	@Environment(EnvType.CLIENT)
	static enum Texture {
		BLOCKED_SLOT(new ResourceLocation("container/bundle/blocked_slot"), 18, 20),
		SLOT(new ResourceLocation("container/bundle/slot"), 18, 20);

		public final ResourceLocation sprite;
		public final int w;
		public final int h;

		private Texture(ResourceLocation resourceLocation, int j, int k) {
			this.sprite = resourceLocation;
			this.w = j;
			this.h = k;
		}
	}
}
