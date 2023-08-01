package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class FocusableTextWidget extends MultiLineTextWidget {
	private static final int BACKGROUND_COLOR = 1426063360;
	private static final int PADDING = 4;
	private final boolean alwaysShowBorder;

	public FocusableTextWidget(int i, Component component, Font font) {
		this(i, component, font, true);
	}

	public FocusableTextWidget(int i, Component component, Font font, boolean bl) {
		super(component, font);
		this.setMaxWidth(i);
		this.setCentered(true);
		this.active = true;
		this.alwaysShowBorder = bl;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		narrationElementOutput.add(NarratedElementType.TITLE, this.getMessage());
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		if (this.isFocused() || this.alwaysShowBorder) {
			int k = this.getX() - 4;
			int l = this.getY() - 4;
			int m = this.getWidth() + 8;
			int n = this.getHeight() + 8;
			int o = this.alwaysShowBorder ? (this.isFocused() ? -1 : -6250336) : -1;
			guiGraphics.fill(k + 1, l, k + m, l + n, 1426063360);
			guiGraphics.renderOutline(k, l, m, n, o);
		}

		super.renderWidget(guiGraphics, i, j, f);
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
	}
}
