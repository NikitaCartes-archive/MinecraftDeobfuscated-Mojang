package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public abstract class AbstractScrollWidget extends AbstractWidget implements Renderable, GuiEventListener {
	private static final WidgetSprites BACKGROUND_SPRITES = new WidgetSprites(
		new ResourceLocation("widget/text_field"), new ResourceLocation("widget/text_field_highlighted")
	);
	private static final ResourceLocation SCROLLER_SPRITE = new ResourceLocation("widget/scroller");
	private static final int INNER_PADDING = 4;
	private static final int SCROLL_BAR_WIDTH = 8;
	private double scrollAmount;
	private boolean scrolling;

	public AbstractScrollWidget(int i, int j, int k, int l, Component component) {
		super(i, j, k, l, component);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (!this.visible) {
			return false;
		} else {
			boolean bl = this.withinContentAreaPoint(d, e);
			boolean bl2 = this.scrollbarVisible()
				&& d >= (double)(this.getX() + this.width)
				&& d <= (double)(this.getX() + this.width + 8)
				&& e >= (double)this.getY()
				&& e < (double)(this.getY() + this.height);
			if (bl2 && i == 0) {
				this.scrolling = true;
				return true;
			} else {
				return bl || bl2;
			}
		}
	}

	@Override
	public boolean mouseReleased(double d, double e, int i) {
		if (i == 0) {
			this.scrolling = false;
		}

		return super.mouseReleased(d, e, i);
	}

	@Override
	public boolean mouseDragged(double d, double e, int i, double f, double g) {
		if (this.visible && this.isFocused() && this.scrolling) {
			if (e < (double)this.getY()) {
				this.setScrollAmount(0.0);
			} else if (e > (double)(this.getY() + this.height)) {
				this.setScrollAmount((double)this.getMaxScrollAmount());
			} else {
				int j = this.getScrollBarHeight();
				double h = (double)Math.max(1, this.getMaxScrollAmount() / (this.height - j));
				this.setScrollAmount(this.scrollAmount + g * h);
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f, double g) {
		if (!this.visible) {
			return false;
		} else {
			this.setScrollAmount(this.scrollAmount - g * this.scrollRate());
			return true;
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		boolean bl = i == 265;
		boolean bl2 = i == 264;
		if (bl || bl2) {
			double d = this.scrollAmount;
			this.setScrollAmount(this.scrollAmount + (double)(bl ? -1 : 1) * this.scrollRate());
			if (d != this.scrollAmount) {
				return true;
			}
		}

		return super.keyPressed(i, j, k);
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		if (this.visible) {
			this.renderBackground(guiGraphics);
			guiGraphics.enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1);
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0.0, -this.scrollAmount, 0.0);
			this.renderContents(guiGraphics, i, j, f);
			guiGraphics.pose().popPose();
			guiGraphics.disableScissor();
			this.renderDecorations(guiGraphics);
		}
	}

	private int getScrollBarHeight() {
		return Mth.clamp((int)((float)(this.height * this.height) / (float)this.getContentHeight()), 32, this.height);
	}

	protected void renderDecorations(GuiGraphics guiGraphics) {
		if (this.scrollbarVisible()) {
			this.renderScrollBar(guiGraphics);
		}
	}

	protected int innerPadding() {
		return 4;
	}

	protected int totalInnerPadding() {
		return this.innerPadding() * 2;
	}

	protected double scrollAmount() {
		return this.scrollAmount;
	}

	protected void setScrollAmount(double d) {
		this.scrollAmount = Mth.clamp(d, 0.0, (double)this.getMaxScrollAmount());
	}

	protected int getMaxScrollAmount() {
		return Math.max(0, this.getContentHeight() - (this.height - 4));
	}

	private int getContentHeight() {
		return this.getInnerHeight() + 4;
	}

	protected void renderBackground(GuiGraphics guiGraphics) {
		this.renderBorder(guiGraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}

	protected void renderBorder(GuiGraphics guiGraphics, int i, int j, int k, int l) {
		ResourceLocation resourceLocation = BACKGROUND_SPRITES.get(this.isActive(), this.isFocused());
		guiGraphics.blitSprite(resourceLocation, i, j, k, l);
	}

	private void renderScrollBar(GuiGraphics guiGraphics) {
		int i = this.getScrollBarHeight();
		int j = this.getX() + this.width;
		int k = Math.max(this.getY(), (int)this.scrollAmount * (this.height - i) / this.getMaxScrollAmount() + this.getY());
		guiGraphics.blitSprite(SCROLLER_SPRITE, j, k, 8, i);
	}

	protected boolean withinContentAreaTopBottom(int i, int j) {
		return (double)j - this.scrollAmount >= (double)this.getY() && (double)i - this.scrollAmount <= (double)(this.getY() + this.height);
	}

	protected boolean withinContentAreaPoint(double d, double e) {
		return d >= (double)this.getX() && d < (double)(this.getX() + this.width) && e >= (double)this.getY() && e < (double)(this.getY() + this.height);
	}

	protected boolean scrollbarVisible() {
		return this.getInnerHeight() > this.getHeight();
	}

	public int scrollbarWidth() {
		return 8;
	}

	protected abstract int getInnerHeight();

	protected abstract double scrollRate();

	protected abstract void renderContents(GuiGraphics guiGraphics, int i, int j, float f);
}
