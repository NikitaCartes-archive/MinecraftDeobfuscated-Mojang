package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public abstract class AbstractSelectionList<E extends AbstractSelectionList.Entry<E>> extends AbstractContainerEventHandler implements Widget {
	protected static final int DRAG_OUTSIDE = -2;
	protected final Minecraft minecraft;
	protected final int itemHeight;
	private final List<E> children = new AbstractSelectionList.TrackedList();
	protected int width;
	protected int height;
	protected int y0;
	protected int y1;
	protected int x1;
	protected int x0;
	protected boolean centerListVertically = true;
	protected int yDrag = -2;
	private double scrollAmount;
	protected boolean renderSelection = true;
	protected boolean renderHeader;
	protected int headerHeight;
	private boolean scrolling;
	private E selected;

	public AbstractSelectionList(Minecraft minecraft, int i, int j, int k, int l, int m) {
		this.minecraft = minecraft;
		this.width = i;
		this.height = j;
		this.y0 = k;
		this.y1 = l;
		this.itemHeight = m;
		this.x0 = 0;
		this.x1 = i;
	}

	public void setRenderSelection(boolean bl) {
		this.renderSelection = bl;
	}

	protected void setRenderHeader(boolean bl, int i) {
		this.renderHeader = bl;
		this.headerHeight = i;
		if (!bl) {
			this.headerHeight = 0;
		}
	}

	public int getRowWidth() {
		return 220;
	}

	@Nullable
	public E getSelected() {
		return this.selected;
	}

	public void setSelected(@Nullable E entry) {
		this.selected = entry;
	}

	@Nullable
	public E getFocused() {
		return (E)super.getFocused();
	}

	@Override
	public final List<E> children() {
		return this.children;
	}

	protected final void clearEntries() {
		this.children.clear();
	}

	protected void replaceEntries(Collection<E> collection) {
		this.children.clear();
		this.children.addAll(collection);
	}

	protected E getEntry(int i) {
		return (E)this.children().get(i);
	}

	protected int addEntry(E entry) {
		this.children.add(entry);
		return this.children.size() - 1;
	}

	protected int getItemCount() {
		return this.children().size();
	}

	protected boolean isSelectedItem(int i) {
		return Objects.equals(this.getSelected(), this.children().get(i));
	}

	@Nullable
	protected final E getEntryAtPosition(double d, double e) {
		int i = this.getRowWidth() / 2;
		int j = this.x0 + this.width / 2;
		int k = j - i;
		int l = j + i;
		int m = Mth.floor(e - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
		int n = m / this.itemHeight;
		return (E)(d < (double)this.getScrollbarPosition() && d >= (double)k && d <= (double)l && n >= 0 && m >= 0 && n < this.getItemCount()
			? this.children().get(n)
			: null);
	}

	public void updateSize(int i, int j, int k, int l) {
		this.width = i;
		this.height = j;
		this.y0 = k;
		this.y1 = l;
		this.x0 = 0;
		this.x1 = i;
	}

	public void setLeftPos(int i) {
		this.x0 = i;
		this.x1 = i + this.width;
	}

	protected int getMaxPosition() {
		return this.getItemCount() * this.itemHeight + this.headerHeight;
	}

	protected void clickedHeader(int i, int j) {
	}

	protected void renderHeader(int i, int j, Tesselator tesselator) {
	}

	protected void renderBackground() {
	}

	protected void renderDecorations(int i, int j) {
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		int k = this.getScrollbarPosition();
		int l = k + 6;
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		this.minecraft.getTextureManager().bind(GuiComponent.BACKGROUND_LOCATION);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		float g = 32.0F;
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.vertex((double)this.x0, (double)this.y1, 0.0)
			.uv((double)((float)this.x0 / 32.0F), (double)((float)(this.y1 + (int)this.getScrollAmount()) / 32.0F))
			.color(32, 32, 32, 255)
			.endVertex();
		bufferBuilder.vertex((double)this.x1, (double)this.y1, 0.0)
			.uv((double)((float)this.x1 / 32.0F), (double)((float)(this.y1 + (int)this.getScrollAmount()) / 32.0F))
			.color(32, 32, 32, 255)
			.endVertex();
		bufferBuilder.vertex((double)this.x1, (double)this.y0, 0.0)
			.uv((double)((float)this.x1 / 32.0F), (double)((float)(this.y0 + (int)this.getScrollAmount()) / 32.0F))
			.color(32, 32, 32, 255)
			.endVertex();
		bufferBuilder.vertex((double)this.x0, (double)this.y0, 0.0)
			.uv((double)((float)this.x0 / 32.0F), (double)((float)(this.y0 + (int)this.getScrollAmount()) / 32.0F))
			.color(32, 32, 32, 255)
			.endVertex();
		tesselator.end();
		int m = this.getRowLeft();
		int n = this.y0 + 4 - (int)this.getScrollAmount();
		if (this.renderHeader) {
			this.renderHeader(m, n, tesselator);
		}

		this.renderList(m, n, i, j, f);
		GlStateManager.disableDepthTest();
		this.renderHoleBackground(0, this.y0, 255, 255);
		this.renderHoleBackground(this.y1, this.height, 255, 255);
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
		);
		GlStateManager.disableAlphaTest();
		GlStateManager.shadeModel(7425);
		GlStateManager.disableTexture();
		int o = 4;
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.vertex((double)this.x0, (double)(this.y0 + 4), 0.0).uv(0.0, 1.0).color(0, 0, 0, 0).endVertex();
		bufferBuilder.vertex((double)this.x1, (double)(this.y0 + 4), 0.0).uv(1.0, 1.0).color(0, 0, 0, 0).endVertex();
		bufferBuilder.vertex((double)this.x1, (double)this.y0, 0.0).uv(1.0, 0.0).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex((double)this.x0, (double)this.y0, 0.0).uv(0.0, 0.0).color(0, 0, 0, 255).endVertex();
		tesselator.end();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.vertex((double)this.x0, (double)this.y1, 0.0).uv(0.0, 1.0).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex((double)this.x1, (double)this.y1, 0.0).uv(1.0, 1.0).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex((double)this.x1, (double)(this.y1 - 4), 0.0).uv(1.0, 0.0).color(0, 0, 0, 0).endVertex();
		bufferBuilder.vertex((double)this.x0, (double)(this.y1 - 4), 0.0).uv(0.0, 0.0).color(0, 0, 0, 0).endVertex();
		tesselator.end();
		int p = this.getMaxScroll();
		if (p > 0) {
			int q = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
			q = Mth.clamp(q, 32, this.y1 - this.y0 - 8);
			int r = (int)this.getScrollAmount() * (this.y1 - this.y0 - q) / p + this.y0;
			if (r < this.y0) {
				r = this.y0;
			}

			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferBuilder.vertex((double)k, (double)this.y1, 0.0).uv(0.0, 1.0).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex((double)l, (double)this.y1, 0.0).uv(1.0, 1.0).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex((double)l, (double)this.y0, 0.0).uv(1.0, 0.0).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex((double)k, (double)this.y0, 0.0).uv(0.0, 0.0).color(0, 0, 0, 255).endVertex();
			tesselator.end();
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferBuilder.vertex((double)k, (double)(r + q), 0.0).uv(0.0, 1.0).color(128, 128, 128, 255).endVertex();
			bufferBuilder.vertex((double)l, (double)(r + q), 0.0).uv(1.0, 1.0).color(128, 128, 128, 255).endVertex();
			bufferBuilder.vertex((double)l, (double)r, 0.0).uv(1.0, 0.0).color(128, 128, 128, 255).endVertex();
			bufferBuilder.vertex((double)k, (double)r, 0.0).uv(0.0, 0.0).color(128, 128, 128, 255).endVertex();
			tesselator.end();
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferBuilder.vertex((double)k, (double)(r + q - 1), 0.0).uv(0.0, 1.0).color(192, 192, 192, 255).endVertex();
			bufferBuilder.vertex((double)(l - 1), (double)(r + q - 1), 0.0).uv(1.0, 1.0).color(192, 192, 192, 255).endVertex();
			bufferBuilder.vertex((double)(l - 1), (double)r, 0.0).uv(1.0, 0.0).color(192, 192, 192, 255).endVertex();
			bufferBuilder.vertex((double)k, (double)r, 0.0).uv(0.0, 0.0).color(192, 192, 192, 255).endVertex();
			tesselator.end();
		}

		this.renderDecorations(i, j);
		GlStateManager.enableTexture();
		GlStateManager.shadeModel(7424);
		GlStateManager.enableAlphaTest();
		GlStateManager.disableBlend();
	}

	protected void centerScrollOn(E entry) {
		this.setScrollAmount((double)(this.children().indexOf(entry) * this.itemHeight + this.itemHeight / 2 - (this.y1 - this.y0) / 2));
	}

	protected void ensureVisible(E entry) {
		int i = this.getRowTop(this.children().indexOf(entry));
		int j = i - this.y0 - 4 - this.itemHeight;
		if (j < 0) {
			this.scroll(j);
		}

		int k = this.y1 - i - this.itemHeight - this.itemHeight;
		if (k < 0) {
			this.scroll(-k);
		}
	}

	private void scroll(int i) {
		this.setScrollAmount(this.getScrollAmount() + (double)i);
		this.yDrag = -2;
	}

	public double getScrollAmount() {
		return this.scrollAmount;
	}

	public void setScrollAmount(double d) {
		this.scrollAmount = Mth.clamp(d, 0.0, (double)this.getMaxScroll());
	}

	private int getMaxScroll() {
		return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
	}

	public int getScrollBottom() {
		return (int)this.getScrollAmount() - this.height - this.headerHeight;
	}

	protected void updateScrollingState(double d, double e, int i) {
		this.scrolling = i == 0 && d >= (double)this.getScrollbarPosition() && d < (double)(this.getScrollbarPosition() + 6);
	}

	protected int getScrollbarPosition() {
		return this.width / 2 + 124;
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		this.updateScrollingState(d, e, i);
		if (!this.isMouseOver(d, e)) {
			return false;
		} else {
			E entry = this.getEntryAtPosition(d, e);
			if (entry != null) {
				if (entry.mouseClicked(d, e, i)) {
					this.setFocused(entry);
					this.setDragging(true);
					return true;
				}
			} else if (i == 0) {
				this.clickedHeader((int)(d - (double)(this.x0 + this.width / 2 - this.getRowWidth() / 2)), (int)(e - (double)this.y0) + (int)this.getScrollAmount() - 4);
				return true;
			}

			return this.scrolling;
		}
	}

	@Override
	public boolean mouseReleased(double d, double e, int i) {
		if (this.getFocused() != null) {
			this.getFocused().mouseReleased(d, e, i);
		}

		return false;
	}

	@Override
	public boolean mouseDragged(double d, double e, int i, double f, double g) {
		if (super.mouseDragged(d, e, i, f, g)) {
			return true;
		} else if (i == 0 && this.scrolling) {
			if (e < (double)this.y0) {
				this.setScrollAmount(0.0);
			} else if (e > (double)this.y1) {
				this.setScrollAmount((double)this.getMaxScroll());
			} else {
				double h = (double)Math.max(1, this.getMaxScroll());
				int j = this.y1 - this.y0;
				int k = Mth.clamp((int)((float)(j * j) / (float)this.getMaxPosition()), 32, j - 8);
				double l = Math.max(1.0, h / (double)(j - k));
				this.setScrollAmount(this.getScrollAmount() + g * l);
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		this.setScrollAmount(this.getScrollAmount() - f * (double)this.itemHeight / 2.0);
		return true;
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (super.keyPressed(i, j, k)) {
			return true;
		} else if (i == 264) {
			this.moveSelection(1);
			return true;
		} else if (i == 265) {
			this.moveSelection(-1);
			return true;
		} else {
			return false;
		}
	}

	protected void moveSelection(int i) {
		if (!this.children().isEmpty()) {
			int j = this.children().indexOf(this.getSelected());
			int k = Mth.clamp(j + i, 0, this.getItemCount() - 1);
			E entry = (E)this.children().get(k);
			this.setSelected(entry);
			this.ensureVisible(entry);
		}
	}

	@Override
	public boolean isMouseOver(double d, double e) {
		return e >= (double)this.y0 && e <= (double)this.y1 && d >= (double)this.x0 && d <= (double)this.x1;
	}

	protected void renderList(int i, int j, int k, int l, float f) {
		int m = this.getItemCount();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();

		for (int n = 0; n < m; n++) {
			int o = this.getRowTop(n);
			int p = this.getRowBottom(n);
			if (p >= this.y0 && o <= this.y1) {
				int q = j + n * this.itemHeight + this.headerHeight;
				int r = this.itemHeight - 4;
				E entry = this.getEntry(n);
				int s = this.getRowWidth();
				if (this.renderSelection && this.isSelectedItem(n)) {
					int t = this.x0 + this.width / 2 - s / 2;
					int u = this.x0 + this.width / 2 + s / 2;
					GlStateManager.disableTexture();
					float g = this.isFocused() ? 1.0F : 0.5F;
					GlStateManager.color4f(g, g, g, 1.0F);
					bufferBuilder.begin(7, DefaultVertexFormat.POSITION);
					bufferBuilder.vertex((double)t, (double)(q + r + 2), 0.0).endVertex();
					bufferBuilder.vertex((double)u, (double)(q + r + 2), 0.0).endVertex();
					bufferBuilder.vertex((double)u, (double)(q - 2), 0.0).endVertex();
					bufferBuilder.vertex((double)t, (double)(q - 2), 0.0).endVertex();
					tesselator.end();
					GlStateManager.color4f(0.0F, 0.0F, 0.0F, 1.0F);
					bufferBuilder.begin(7, DefaultVertexFormat.POSITION);
					bufferBuilder.vertex((double)(t + 1), (double)(q + r + 1), 0.0).endVertex();
					bufferBuilder.vertex((double)(u - 1), (double)(q + r + 1), 0.0).endVertex();
					bufferBuilder.vertex((double)(u - 1), (double)(q - 1), 0.0).endVertex();
					bufferBuilder.vertex((double)(t + 1), (double)(q - 1), 0.0).endVertex();
					tesselator.end();
					GlStateManager.enableTexture();
				}

				int t = this.getRowLeft();
				entry.render(n, o, t, s, r, k, l, this.isMouseOver((double)k, (double)l) && Objects.equals(this.getEntryAtPosition((double)k, (double)l), entry), f);
			}
		}
	}

	protected int getRowLeft() {
		return this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
	}

	protected int getRowTop(int i) {
		return this.y0 + 4 - (int)this.getScrollAmount() + i * this.itemHeight + this.headerHeight;
	}

	private int getRowBottom(int i) {
		return this.getRowTop(i) + this.itemHeight;
	}

	protected boolean isFocused() {
		return false;
	}

	protected void renderHoleBackground(int i, int j, int k, int l) {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		this.minecraft.getTextureManager().bind(GuiComponent.BACKGROUND_LOCATION);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		float f = 32.0F;
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.vertex((double)this.x0, (double)j, 0.0).uv(0.0, (double)((float)j / 32.0F)).color(64, 64, 64, l).endVertex();
		bufferBuilder.vertex((double)(this.x0 + this.width), (double)j, 0.0)
			.uv((double)((float)this.width / 32.0F), (double)((float)j / 32.0F))
			.color(64, 64, 64, l)
			.endVertex();
		bufferBuilder.vertex((double)(this.x0 + this.width), (double)i, 0.0)
			.uv((double)((float)this.width / 32.0F), (double)((float)i / 32.0F))
			.color(64, 64, 64, k)
			.endVertex();
		bufferBuilder.vertex((double)this.x0, (double)i, 0.0).uv(0.0, (double)((float)i / 32.0F)).color(64, 64, 64, k).endVertex();
		tesselator.end();
	}

	protected E remove(int i) {
		E entry = (E)this.children.get(i);
		return this.removeEntry((E)this.children.get(i)) ? entry : null;
	}

	protected boolean removeEntry(E entry) {
		boolean bl = this.children.remove(entry);
		if (bl && entry == this.getSelected()) {
			this.setSelected(null);
		}

		return bl;
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry<E extends AbstractSelectionList.Entry<E>> implements GuiEventListener {
		@Deprecated
		AbstractSelectionList<E> list;

		public abstract void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f);

		@Override
		public boolean isMouseOver(double d, double e) {
			return Objects.equals(this.list.getEntryAtPosition(d, e), this);
		}
	}

	@Environment(EnvType.CLIENT)
	class TrackedList extends AbstractList<E> {
		private final List<E> delegate = Lists.<E>newArrayList();

		private TrackedList() {
		}

		public E get(int i) {
			return (E)this.delegate.get(i);
		}

		public int size() {
			return this.delegate.size();
		}

		public E set(int i, E entry) {
			E entry2 = (E)this.delegate.set(i, entry);
			entry.list = AbstractSelectionList.this;
			return entry2;
		}

		public void add(int i, E entry) {
			this.delegate.add(i, entry);
			entry.list = AbstractSelectionList.this;
		}

		public E remove(int i) {
			return (E)this.delegate.remove(i);
		}
	}
}
