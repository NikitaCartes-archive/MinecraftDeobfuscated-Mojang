package net.minecraft.client.gui.screens.advancements;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
enum AdvancementTabType {
	ABOVE(0, 0, 28, 32, 8),
	BELOW(84, 0, 28, 32, 8),
	LEFT(0, 64, 32, 28, 5),
	RIGHT(96, 64, 32, 28, 5);

	private final int textureX;
	private final int textureY;
	private final int width;
	private final int height;
	private final int max;

	private AdvancementTabType(int j, int k, int l, int m, int n) {
		this.textureX = j;
		this.textureY = k;
		this.width = l;
		this.height = m;
		this.max = n;
	}

	public int getMax() {
		return this.max;
	}

	public void draw(PoseStack poseStack, GuiComponent guiComponent, int i, int j, boolean bl, int k) {
		int l = this.textureX;
		if (k > 0) {
			l += this.width;
		}

		if (k == this.max - 1) {
			l += this.width;
		}

		int m = bl ? this.textureY + this.height : this.textureY;
		guiComponent.blit(poseStack, i + this.getX(k), j + this.getY(k), l, m, this.width, this.height);
	}

	public void drawIcon(int i, int j, int k, ItemRenderer itemRenderer, ItemStack itemStack) {
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

		itemRenderer.renderAndDecorateFakeItem(itemStack, l, m);
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
}
