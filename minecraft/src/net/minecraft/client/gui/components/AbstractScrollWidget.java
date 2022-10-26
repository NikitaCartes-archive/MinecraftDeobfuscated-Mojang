package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public abstract class AbstractScrollWidget extends AbstractWidget implements Renderable, GuiEventListener {
	private static final int BORDER_COLOR_FOCUSED = -1;
	private static final int BORDER_COLOR = -6250336;
	private static final int BACKGROUND_COLOR = -16777216;
	private static final int INNER_PADDING = 4;
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
			this.setFocused(bl || bl2);
			if (bl2 && i == 0) {
				this.scrolling = true;
				return true;
			} else {
				return false;
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
	public boolean mouseScrolled(double d, double e, double f) {
		if (this.visible && this.isFocused()) {
			this.setScrollAmount(this.scrollAmount - f * this.scrollRate());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void renderButton(PoseStack poseStack, int i, int j, float f) {
		if (this.visible) {
			this.renderBackground(poseStack);
			enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1);
			poseStack.pushPose();
			poseStack.translate(0.0, -this.scrollAmount, 0.0);
			this.renderContents(poseStack, i, j, f);
			poseStack.popPose();
			disableScissor();
			this.renderDecorations(poseStack);
		}
	}

	private int getScrollBarHeight() {
		return Mth.clamp((int)((float)(this.height * this.height) / (float)this.getContentHeight()), 32, this.height);
	}

	protected void renderDecorations(PoseStack poseStack) {
		if (this.scrollbarVisible()) {
			this.renderScrollBar();
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

	private void renderBackground(PoseStack poseStack) {
		int i = this.isFocused() ? -1 : -6250336;
		fill(poseStack, this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, i);
		fill(poseStack, this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, -16777216);
	}

	private void renderScrollBar() {
		int i = this.getScrollBarHeight();
		int j = this.getX() + this.width;
		int k = this.getX() + this.width + 8;
		int l = Math.max(this.getY(), (int)this.scrollAmount * (this.height - i) / this.getMaxScrollAmount() + this.getY());
		int m = l + i;
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.vertex((double)j, (double)m, 0.0).color(128, 128, 128, 255).endVertex();
		bufferBuilder.vertex((double)k, (double)m, 0.0).color(128, 128, 128, 255).endVertex();
		bufferBuilder.vertex((double)k, (double)l, 0.0).color(128, 128, 128, 255).endVertex();
		bufferBuilder.vertex((double)j, (double)l, 0.0).color(128, 128, 128, 255).endVertex();
		bufferBuilder.vertex((double)j, (double)(m - 1), 0.0).color(192, 192, 192, 255).endVertex();
		bufferBuilder.vertex((double)(k - 1), (double)(m - 1), 0.0).color(192, 192, 192, 255).endVertex();
		bufferBuilder.vertex((double)(k - 1), (double)l, 0.0).color(192, 192, 192, 255).endVertex();
		bufferBuilder.vertex((double)j, (double)l, 0.0).color(192, 192, 192, 255).endVertex();
		tesselator.end();
	}

	protected boolean withinContentAreaTopBottom(int i, int j) {
		return (double)j - this.scrollAmount >= (double)this.getY() && (double)i - this.scrollAmount <= (double)(this.getY() + this.height);
	}

	protected boolean withinContentAreaPoint(double d, double e) {
		return d >= (double)this.getX() && d < (double)(this.getX() + this.width) && e >= (double)this.getY() && e < (double)(this.getY() + this.height);
	}

	protected abstract int getInnerHeight();

	protected abstract boolean scrollbarVisible();

	protected abstract double scrollRate();

	protected abstract void renderContents(PoseStack poseStack, int i, int j, float f);
}
