package net.minecraft.realms;

import java.util.Collection;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;

@Environment(EnvType.CLIENT)
public class RealmsObjectSelectionListProxy<E extends ObjectSelectionList.Entry<E>> extends ObjectSelectionList<E> {
	private final RealmsObjectSelectionList realmsObjectSelectionList;

	public RealmsObjectSelectionListProxy(RealmsObjectSelectionList realmsObjectSelectionList, int i, int j, int k, int l, int m) {
		super(Minecraft.getInstance(), i, j, k, l, m);
		this.realmsObjectSelectionList = realmsObjectSelectionList;
	}

	@Override
	public int getItemCount() {
		return super.getItemCount();
	}

	public void clear() {
		super.clearEntries();
	}

	@Override
	public boolean isFocused() {
		return this.realmsObjectSelectionList.isFocused();
	}

	protected void setSelectedItem(int i) {
		if (i == -1) {
			super.setSelected(null);
		} else if (super.getItemCount() != 0) {
			E entry = super.getEntry(i);
			super.setSelected(entry);
		}
	}

	public void setSelected(@Nullable E entry) {
		super.setSelected(entry);
		this.realmsObjectSelectionList.selectItem(super.children().indexOf(entry));
	}

	@Override
	public void renderBackground() {
		this.realmsObjectSelectionList.renderBackground();
	}

	public int getWidth() {
		return this.width;
	}

	@Override
	public int getMaxPosition() {
		return this.realmsObjectSelectionList.getMaxPosition();
	}

	@Override
	public int getScrollbarPosition() {
		return this.realmsObjectSelectionList.getScrollbarPosition();
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		return this.realmsObjectSelectionList.mouseScrolled(d, e, f) ? true : super.mouseScrolled(d, e, f);
	}

	@Override
	public int getRowWidth() {
		return this.realmsObjectSelectionList.getRowWidth();
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		return this.realmsObjectSelectionList.mouseClicked(d, e, i) ? true : access$001(this, d, e, i);
	}

	@Override
	public boolean mouseReleased(double d, double e, int i) {
		return this.realmsObjectSelectionList.mouseReleased(d, e, i);
	}

	@Override
	public boolean mouseDragged(double d, double e, int i, double f, double g) {
		return this.realmsObjectSelectionList.mouseDragged(d, e, i, f, g) ? true : super.mouseDragged(d, e, i, f, g);
	}

	protected final int addEntry(E entry) {
		return super.addEntry(entry);
	}

	public E remove(int i) {
		return super.remove(i);
	}

	public boolean removeEntry(E entry) {
		return super.removeEntry(entry);
	}

	@Override
	public void setScrollAmount(double d) {
		super.setScrollAmount(d);
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

	public int itemHeight() {
		return this.itemHeight;
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		return super.keyPressed(i, j, k) ? true : this.realmsObjectSelectionList.keyPressed(i, j, k);
	}

	@Override
	public void replaceEntries(Collection<E> collection) {
		super.replaceEntries(collection);
	}

	@Override
	public int getRowTop(int i) {
		return super.getRowTop(i);
	}

	@Override
	public int getRowLeft() {
		return super.getRowLeft();
	}
}
