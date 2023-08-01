package net.minecraft.client.gui.screens.advancements;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
enum AdvancementTabType {
	ABOVE(
		new AdvancementTabType.Sprites(
			new ResourceLocation("advancements/tab_above_left_selected"),
			new ResourceLocation("advancements/tab_above_middle_selected"),
			new ResourceLocation("advancements/tab_above_right_selected")
		),
		new AdvancementTabType.Sprites(
			new ResourceLocation("advancements/tab_above_left"),
			new ResourceLocation("advancements/tab_above_middle"),
			new ResourceLocation("advancements/tab_above_right")
		),
		28,
		32,
		8
	),
	BELOW(
		new AdvancementTabType.Sprites(
			new ResourceLocation("advancements/tab_below_left_selected"),
			new ResourceLocation("advancements/tab_below_middle_selected"),
			new ResourceLocation("advancements/tab_below_right_selected")
		),
		new AdvancementTabType.Sprites(
			new ResourceLocation("advancements/tab_below_left"),
			new ResourceLocation("advancements/tab_below_middle"),
			new ResourceLocation("advancements/tab_below_right")
		),
		28,
		32,
		8
	),
	LEFT(
		new AdvancementTabType.Sprites(
			new ResourceLocation("advancements/tab_left_top_selected"),
			new ResourceLocation("advancements/tab_left_middle_selected"),
			new ResourceLocation("advancements/tab_left_bottom_selected")
		),
		new AdvancementTabType.Sprites(
			new ResourceLocation("advancements/tab_left_top"),
			new ResourceLocation("advancements/tab_left_middle"),
			new ResourceLocation("advancements/tab_left_bottom")
		),
		32,
		28,
		5
	),
	RIGHT(
		new AdvancementTabType.Sprites(
			new ResourceLocation("advancements/tab_right_top_selected"),
			new ResourceLocation("advancements/tab_right_middle_selected"),
			new ResourceLocation("advancements/tab_right_bottom_selected")
		),
		new AdvancementTabType.Sprites(
			new ResourceLocation("advancements/tab_right_top"),
			new ResourceLocation("advancements/tab_right_middle"),
			new ResourceLocation("advancements/tab_right_bottom")
		),
		32,
		28,
		5
	);

	private final AdvancementTabType.Sprites selectedSprites;
	private final AdvancementTabType.Sprites unselectedSprites;
	private final int width;
	private final int height;
	private final int max;

	private AdvancementTabType(AdvancementTabType.Sprites sprites, AdvancementTabType.Sprites sprites2, int j, int k, int l) {
		this.selectedSprites = sprites;
		this.unselectedSprites = sprites2;
		this.width = j;
		this.height = k;
		this.max = l;
	}

	public int getMax() {
		return this.max;
	}

	public void draw(GuiGraphics guiGraphics, int i, int j, boolean bl, int k) {
		AdvancementTabType.Sprites sprites = bl ? this.selectedSprites : this.unselectedSprites;
		ResourceLocation resourceLocation;
		if (k == 0) {
			resourceLocation = sprites.first();
		} else if (k == this.max - 1) {
			resourceLocation = sprites.last();
		} else {
			resourceLocation = sprites.middle();
		}

		guiGraphics.blitSprite(resourceLocation, i + this.getX(k), j + this.getY(k), this.width, this.height);
	}

	public void drawIcon(GuiGraphics guiGraphics, int i, int j, int k, ItemStack itemStack) {
		int l = i + this.getX(k);
		int m = j + this.getY(k);
		switch (this) {
			case ABOVE:
				l += 6;
				m += 9;
				break;
			case BELOW:
				l += 6;
				m += 6;
				break;
			case LEFT:
				l += 10;
				m += 5;
				break;
			case RIGHT:
				l += 6;
				m += 5;
		}

		guiGraphics.renderFakeItem(itemStack, l, m);
	}

	public int getX(int i) {
		switch (this) {
			case ABOVE:
				return (this.width + 4) * i;
			case BELOW:
				return (this.width + 4) * i;
			case LEFT:
				return -this.width + 4;
			case RIGHT:
				return 248;
			default:
				throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
		}
	}

	public int getY(int i) {
		switch (this) {
			case ABOVE:
				return -this.height + 4;
			case BELOW:
				return 136;
			case LEFT:
				return this.height * i;
			case RIGHT:
				return this.height * i;
			default:
				throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
		}
	}

	public boolean isMouseOver(int i, int j, int k, double d, double e) {
		int l = i + this.getX(k);
		int m = j + this.getY(k);
		return d > (double)l && d < (double)(l + this.width) && e > (double)m && e < (double)(m + this.height);
	}

	@Environment(EnvType.CLIENT)
	static record Sprites(ResourceLocation first, ResourceLocation middle, ResourceLocation last) {
	}
}
