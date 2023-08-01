package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class FittingMultiLineTextWidget extends AbstractScrollWidget {
	private final Font font;
	private final MultiLineTextWidget multilineWidget;

	public FittingMultiLineTextWidget(int i, int j, int k, int l, Component component, Font font) {
		super(i, j, k, l, component);
		this.font = font;
		this.multilineWidget = new MultiLineTextWidget(component, font).setMaxWidth(this.getWidth() - this.totalInnerPadding());
	}

	public FittingMultiLineTextWidget setColor(int i) {
		this.multilineWidget.setColor(i);
		return this;
	}

	@Override
	public void setWidth(int i) {
		super.setWidth(i);
		this.multilineWidget.setMaxWidth(this.getWidth() - this.totalInnerPadding());
	}

	@Override
	protected int getInnerHeight() {
		return this.multilineWidget.getHeight();
	}

	@Override
	protected double scrollRate() {
		return 9.0;
	}

	@Override
	protected void renderBackground(GuiGraphics guiGraphics) {
		if (this.scrollbarVisible()) {
			super.renderBackground(guiGraphics);
		} else if (this.isFocused()) {
			this.renderBorder(
				guiGraphics,
				this.getX() - this.innerPadding(),
				this.getY() - this.innerPadding(),
				this.getWidth() + this.totalInnerPadding(),
				this.getHeight() + this.totalInnerPadding()
			);
		}
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		if (this.visible) {
			if (!this.scrollbarVisible()) {
				this.renderBackground(guiGraphics);
				guiGraphics.pose().pushPose();
				guiGraphics.pose().translate((float)this.getX(), (float)this.getY(), 0.0F);
				this.multilineWidget.render(guiGraphics, i, j, f);
				guiGraphics.pose().popPose();
			} else {
				super.renderWidget(guiGraphics, i, j, f);
			}
		}
	}

	@Override
	protected void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate((float)(this.getX() + this.innerPadding()), (float)(this.getY() + this.innerPadding()), 0.0F);
		this.multilineWidget.render(guiGraphics, i, j, f);
		guiGraphics.pose().popPose();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		narrationElementOutput.add(NarratedElementType.TITLE, this.getMessage());
	}
}
