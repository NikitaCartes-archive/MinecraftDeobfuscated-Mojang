package net.minecraft.realms;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.events.GuiEventListener;

@Environment(EnvType.CLIENT)
public abstract class RealmsObjectSelectionList<E extends RealmListEntry> extends RealmsGuiEventListener {
	private final RealmsObjectSelectionListProxy proxy;

	public RealmsObjectSelectionList(int i, int j, int k, int l, int m) {
		this.proxy = new RealmsObjectSelectionListProxy(this, i, j, k, l, m);
	}

	public void render(int i, int j, float f) {
		this.proxy.render(i, j, f);
	}

	public void addEntry(E realmListEntry) {
		this.proxy.addEntry(realmListEntry);
	}

	public void remove(int i) {
		this.proxy.remove(i);
	}

	public void clear() {
		this.proxy.clear();
	}

	public boolean removeEntry(E realmListEntry) {
		return this.proxy.removeEntry(realmListEntry);
	}

	public int width() {
		return this.proxy.getWidth();
	}

	protected void renderItem(int i, int j, int k, int l, Tezzelator tezzelator, int m, int n) {
	}

	public void setLeftPos(int i) {
		this.proxy.setLeftPos(i);
	}

	public void renderItem(int i, int j, int k, int l, int m, int n) {
		this.renderItem(i, j, k, l, Tezzelator.instance, m, n);
	}

	public void setSelected(int i) {
		this.proxy.setSelectedItem(i);
	}

	public void itemClicked(int i, int j, double d, double e, int k) {
	}

	public int getItemCount() {
		return this.proxy.getItemCount();
	}

	public void renderBackground() {
	}

	public int getMaxPosition() {
		return 0;
	}

	public int getScrollbarPosition() {
		return this.proxy.getRowLeft() + this.proxy.getRowWidth();
	}

	public int y0() {
		return this.proxy.y0();
	}

	public int y1() {
		return this.proxy.y1();
	}

	public int headerHeight() {
		return this.proxy.headerHeight();
	}

	public int itemHeight() {
		return this.proxy.itemHeight();
	}

	public void scroll(int i) {
		this.proxy.setScrollAmount((double)i);
	}

	public int getScroll() {
		return (int)this.proxy.getScrollAmount();
	}

	@Override
	public GuiEventListener getProxy() {
		return this.proxy;
	}

	public int getRowWidth() {
		return (int)((double)this.width() * 0.6);
	}

	public abstract boolean isFocused();

	public void selectItem(int i) {
		this.setSelected(i);
	}

	@Nullable
	public E getSelected() {
		return (E)this.proxy.getSelected();
	}

	public List<E> children() {
		return (List<E>)this.proxy.children();
	}

	public void replaceEntries(Collection<E> collection) {
		this.proxy.replaceEntries(collection);
	}

	public int getRowTop(int i) {
		return this.proxy.getRowTop(i);
	}

	public int getRowLeft() {
		return this.proxy.getRowLeft();
	}
}
