package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public abstract class AbstractSelectionList<E extends AbstractSelectionList.Entry<E>> extends AbstractContainerEventHandler implements Widget, NarratableEntry {
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
	private double scrollAmount;
	private boolean renderSelection = true;
	private boolean renderHeader;
	protected int headerHeight;
	private boolean scrolling;
	@Nullable
	private E selected;
	private boolean renderBackground = true;
	private boolean renderTopAndBottom = true;
	@Nullable
	private E hovered;

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

	public void setRenderBackground(boolean bl) {
		this.renderBackground = bl;
	}

	public void setRenderTopAndBottom(boolean bl) {
		this.renderTopAndBottom = bl;
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

	protected void renderHeader(PoseStack poseStack, int i, int j, Tesselator tesselator) {
	}

	protected void renderBackground(PoseStack poseStack) {
	}

	protected void renderDecorations(PoseStack poseStack, int i, int j) {
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		int k = this.getScrollbarPosition();
		int l = k + 6;
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		this.hovered = this.isMouseOver((double)i, (double)j) ? this.getEntryAtPosition((double)i, (double)j) : null;
		if (this.renderBackground) {
			RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			float g = 32.0F;
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferBuilder.vertex((double)this.x0, (double)this.y1, 0.0)
				.uv((float)this.x0 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F)
				.color(32, 32, 32, 255)
				.endVertex();
			bufferBuilder.vertex((double)this.x1, (double)this.y1, 0.0)
				.uv((float)this.x1 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F)
				.color(32, 32, 32, 255)
				.endVertex();
			bufferBuilder.vertex((double)this.x1, (double)this.y0, 0.0)
				.uv((float)this.x1 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F)
				.color(32, 32, 32, 255)
				.endVertex();
			bufferBuilder.vertex((double)this.x0, (double)this.y0, 0.0)
				.uv((float)this.x0 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F)
				.color(32, 32, 32, 255)
				.endVertex();
			tesselator.end();
		}

		int m = this.getRowLeft();
		int n = this.y0 + 4 - (int)this.getScrollAmount();
		if (this.renderHeader) {
			this.renderHeader(poseStack, m, n, tesselator);
		}

		this.renderList(poseStack, m, n, i, j, f);
		if (this.renderTopAndBottom) {
			RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
			RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
			RenderSystem.enableDepthTest();
			RenderSystem.depthFunc(519);
			float h = 32.0F;
			int o = -100;
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferBuilder.vertex((double)this.x0, (double)this.y0, -100.0).uv(0.0F, (float)this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
			bufferBuilder.vertex((double)(this.x0 + this.width), (double)this.y0, -100.0)
				.uv((float)this.width / 32.0F, (float)this.y0 / 32.0F)
				.color(64, 64, 64, 255)
				.endVertex();
			bufferBuilder.vertex((double)(this.x0 + this.width), 0.0, -100.0).uv((float)this.width / 32.0F, 0.0F).color(64, 64, 64, 255).endVertex();
			bufferBuilder.vertex((double)this.x0, 0.0, -100.0).uv(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();
			bufferBuilder.vertex((double)this.x0, (double)this.height, -100.0).uv(0.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
			bufferBuilder.vertex((double)(this.x0 + this.width), (double)this.height, -100.0)
				.uv((float)this.width / 32.0F, (float)this.height / 32.0F)
				.color(64, 64, 64, 255)
				.endVertex();
			bufferBuilder.vertex((double)(this.x0 + this.width), (double)this.y1, -100.0)
				.uv((float)this.width / 32.0F, (float)this.y1 / 32.0F)
				.color(64, 64, 64, 255)
				.endVertex();
			bufferBuilder.vertex((double)this.x0, (double)this.y1, -100.0).uv(0.0F, (float)this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
			tesselator.end();
			RenderSystem.depthFunc(515);
			RenderSystem.disableDepthTest();
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
			);
			RenderSystem.disableTexture();
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			int p = 4;
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			bufferBuilder.vertex((double)this.x0, (double)(this.y0 + 4), 0.0).color(0, 0, 0, 0).endVertex();
			bufferBuilder.vertex((double)this.x1, (double)(this.y0 + 4), 0.0).color(0, 0, 0, 0).endVertex();
			bufferBuilder.vertex((double)this.x1, (double)this.y0, 0.0).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex((double)this.x0, (double)this.y0, 0.0).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex((double)this.x0, (double)this.y1, 0.0).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex((double)this.x1, (double)this.y1, 0.0).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex((double)this.x1, (double)(this.y1 - 4), 0.0).color(0, 0, 0, 0).endVertex();
			bufferBuilder.vertex((double)this.x0, (double)(this.y1 - 4), 0.0).color(0, 0, 0, 0).endVertex();
			tesselator.end();
		}

		int q = this.getMaxScroll();
		if (q > 0) {
			RenderSystem.disableTexture();
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			int o = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
			o = Mth.clamp(o, 32, this.y1 - this.y0 - 8);
			int p = (int)this.getScrollAmount() * (this.y1 - this.y0 - o) / q + this.y0;
			if (p < this.y0) {
				p = this.y0;
			}

			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			bufferBuilder.vertex((double)k, (double)this.y1, 0.0).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex((double)l, (double)this.y1, 0.0).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex((double)l, (double)this.y0, 0.0).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex((double)k, (double)this.y0, 0.0).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex((double)k, (double)(p + o), 0.0).color(128, 128, 128, 255).endVertex();
			bufferBuilder.vertex((double)l, (double)(p + o), 0.0).color(128, 128, 128, 255).endVertex();
			bufferBuilder.vertex((double)l, (double)p, 0.0).color(128, 128, 128, 255).endVertex();
			bufferBuilder.vertex((double)k, (double)p, 0.0).color(128, 128, 128, 255).endVertex();
			bufferBuilder.vertex((double)k, (double)(p + o - 1), 0.0).color(192, 192, 192, 255).endVertex();
			bufferBuilder.vertex((double)(l - 1), (double)(p + o - 1), 0.0).color(192, 192, 192, 255).endVertex();
			bufferBuilder.vertex((double)(l - 1), (double)p, 0.0).color(192, 192, 192, 255).endVertex();
			bufferBuilder.vertex((double)k, (double)p, 0.0).color(192, 192, 192, 255).endVertex();
			tesselator.end();
		}

		this.renderDecorations(poseStack, i, j);
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
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
	}

	public double getScrollAmount() {
		return this.scrollAmount;
	}

	public void setScrollAmount(double d) {
		this.scrollAmount = Mth.clamp(d, 0.0, (double)this.getMaxScroll());
	}

	public int getMaxScroll() {
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
			this.moveSelection(AbstractSelectionList.SelectionDirection.DOWN);
			return true;
		} else if (i == 265) {
			this.moveSelection(AbstractSelectionList.SelectionDirection.UP);
			return true;
		} else {
			return false;
		}
	}

	protected void moveSelection(AbstractSelectionList.SelectionDirection selectionDirection) {
		this.moveSelection(selectionDirection, entry -> true);
	}

	protected void refreshSelection() {
		E entry = this.getSelected();
		if (entry != null) {
			this.setSelected(entry);
			this.ensureVisible(entry);
		}
	}

	protected void moveSelection(AbstractSelectionList.SelectionDirection selectionDirection, Predicate<E> predicate) {
		int i = selectionDirection == AbstractSelectionList.SelectionDirection.UP ? -1 : 1;
		if (!this.children().isEmpty()) {
			int j = this.children().indexOf(this.getSelected());

			while (true) {
				int k = Mth.clamp(j + i, 0, this.getItemCount() - 1);
				if (j == k) {
					break;
				}

				E entry = (E)this.children().get(k);
				if (predicate.test(entry)) {
					this.setSelected(entry);
					this.ensureVisible(entry);
					break;
				}

				j = k;
			}
		}
	}

	@Override
	public boolean isMouseOver(double d, double e) {
		return e >= (double)this.y0 && e <= (double)this.y1 && d >= (double)this.x0 && d <= (double)this.x1;
	}

	protected void renderList(PoseStack poseStack, int i, int j, int k, int l, float f) {
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
					RenderSystem.disableTexture();
					RenderSystem.setShader(GameRenderer::getPositionShader);
					float g = this.isFocused() ? 1.0F : 0.5F;
					RenderSystem.setShaderColor(g, g, g, 1.0F);
					bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
					bufferBuilder.vertex((double)t, (double)(q + r + 2), 0.0).endVertex();
					bufferBuilder.vertex((double)u, (double)(q + r + 2), 0.0).endVertex();
					bufferBuilder.vertex((double)u, (double)(q - 2), 0.0).endVertex();
					bufferBuilder.vertex((double)t, (double)(q - 2), 0.0).endVertex();
					tesselator.end();
					RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
					bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
					bufferBuilder.vertex((double)(t + 1), (double)(q + r + 1), 0.0).endVertex();
					bufferBuilder.vertex((double)(u - 1), (double)(q + r + 1), 0.0).endVertex();
					bufferBuilder.vertex((double)(u - 1), (double)(q - 1), 0.0).endVertex();
					bufferBuilder.vertex((double)(t + 1), (double)(q - 1), 0.0).endVertex();
					tesselator.end();
					RenderSystem.enableTexture();
				}

				int t = this.getRowLeft();
				entry.render(poseStack, n, o, t, s, r, k, l, Objects.equals(this.hovered, entry), f);
			}
		}
	}

	public int getRowLeft() {
		return this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
	}

	public int getRowRight() {
		return this.getRowLeft() + this.getRowWidth();
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

	@Override
	public NarratableEntry.NarrationPriority narrationPriority() {
		if (this.isFocused()) {
			return NarratableEntry.NarrationPriority.FOCUSED;
		} else {
			return this.hovered != null ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
		}
	}

	@Nullable
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

	@Nullable
	protected E getHovered() {
		return this.hovered;
	}

	void bindEntryToSelf(AbstractSelectionList.Entry<E> entry) {
		entry.list = this;
	}

	protected void narrateListElementPosition(NarrationElementOutput narrationElementOutput, E entry) {
		List<E> list = this.children();
		if (list.size() > 1) {
			int i = list.indexOf(entry);
			if (i != -1) {
				narrationElementOutput.add(NarratedElementType.POSITION, Component.translatable("narrator.position.list", i + 1, list.size()));
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry<E extends AbstractSelectionList.Entry<E>> implements GuiEventListener {
		@Deprecated
		AbstractSelectionList<E> list;

		public abstract void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f);

		@Override
		public boolean isMouseOver(double d, double e) {
			return Objects.equals(this.list.getEntryAtPosition(d, e), this);
		}
	}

	@Environment(EnvType.CLIENT)
	protected static enum SelectionDirection {
		UP,
		DOWN;
	}

	@Environment(EnvType.CLIENT)
	class TrackedList extends AbstractList<E> {
		private final List<E> delegate = Lists.<E>newArrayList();

		public E get(int i) {
			return (E)this.delegate.get(i);
		}

		public int size() {
			return this.delegate.size();
		}

		public E set(int i, E entry) {
			E entry2 = (E)this.delegate.set(i, entry);
			AbstractSelectionList.this.bindEntryToSelf(entry);
			return entry2;
		}

		public void add(int i, E entry) {
			this.delegate.add(i, entry);
			AbstractSelectionList.this.bindEntryToSelf(entry);
		}

		public E remove(int i) {
			return (E)this.delegate.remove(i);
		}
	}
}
