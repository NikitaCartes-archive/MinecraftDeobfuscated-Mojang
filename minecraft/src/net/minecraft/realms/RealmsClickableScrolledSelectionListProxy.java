package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ScrolledSelectionList;

@Environment(EnvType.CLIENT)
public class RealmsClickableScrolledSelectionListProxy extends ScrolledSelectionList {
	private final RealmsClickableScrolledSelectionList realmsClickableScrolledSelectionList;

	public RealmsClickableScrolledSelectionListProxy(RealmsClickableScrolledSelectionList realmsClickableScrolledSelectionList, int i, int j, int k, int l, int m) {
		super(Minecraft.getInstance(), i, j, k, l, m);
		this.realmsClickableScrolledSelectionList = realmsClickableScrolledSelectionList;
	}

	@Override
	public int getItemCount() {
		return this.realmsClickableScrolledSelectionList.getItemCount();
	}

	@Override
	public boolean selectItem(int i, int j, double d, double e) {
		return this.realmsClickableScrolledSelectionList.selectItem(i, j, d, e);
	}

	@Override
	public boolean isSelectedItem(int i) {
		return this.realmsClickableScrolledSelectionList.isSelectedItem(i);
	}

	@Override
	public void renderBackground() {
		this.realmsClickableScrolledSelectionList.renderBackground();
	}

	@Override
	public void renderItem(int i, int j, int k, int l, int m, int n, float f) {
		this.realmsClickableScrolledSelectionList.renderItem(i, j, k, l, m, n);
	}

	public int getWidth() {
		return this.width;
	}

	@Override
	public int getMaxPosition() {
		return this.realmsClickableScrolledSelectionList.getMaxPosition();
	}

	@Override
	public int getScrollbarPosition() {
		return this.realmsClickableScrolledSelectionList.getScrollbarPosition();
	}

	public void itemClicked(int i, int j, int k, int l, int m) {
		this.realmsClickableScrolledSelectionList.itemClicked(i, j, (double)k, (double)l, m);
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		return this.realmsClickableScrolledSelectionList.mouseScrolled(d, e, f) ? true : super.mouseScrolled(d, e, f);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		return this.realmsClickableScrolledSelectionList.mouseClicked(d, e, i) ? true : super.mouseClicked(d, e, i);
	}

	@Override
	public boolean mouseReleased(double d, double e, int i) {
		return this.realmsClickableScrolledSelectionList.mouseReleased(d, e, i);
	}

	@Override
	public boolean mouseDragged(double d, double e, int i, double f, double g) {
		return this.realmsClickableScrolledSelectionList.mouseDragged(d, e, i, f, g) ? true : super.mouseDragged(d, e, i, f, g);
	}

	public void renderSelected(int i, int j, int k, Tezzelator tezzelator) {
		this.realmsClickableScrolledSelectionList.renderSelected(i, j, k, tezzelator);
	}

	@Override
	public void renderList(int i, int j, int k, int l, float f) {
		int m = this.getItemCount();

		for (int n = 0; n < m; n++) {
			int o = j + n * this.itemHeight + this.headerHeight;
			int p = this.itemHeight - 4;
			if (o > this.y1 || o + p < this.y0) {
				this.updateItemPosition(n, i, o, f);
			}

			if (this.renderSelection && this.isSelectedItem(n)) {
				this.renderSelected(this.width, o, p, Tezzelator.instance);
			}

			this.renderItem(n, i, o, p, k, l, f);
		}
	}

	public int y0() {
		return this.y0;
	}

	public int y1() {
		return this.y1;
	}

	public int headerHeight() {
		return this.headerHeight;
	}

	public double yo() {
		return this.yo;
	}

	public int itemHeight() {
		return this.itemHeight;
	}
}
