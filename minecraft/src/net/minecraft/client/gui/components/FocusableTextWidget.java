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
	private static final int DEFAULT_PADDING = 4;
	private final boolean alwaysShowBorder;
	private final int padding;

	public FocusableTextWidget(int i, Component component, Font font) {
		this(i, component, font, 4);
	}

	public FocusableTextWidget(int i, Component component, Font font, int j) {
		this(i, component, font, true, j);
	}

	public FocusableTextWidget(int i, Component component, Font font, boolean bl, int j) {
		super(component, font);
		this.setMaxWidth(i);
		this.setCentered(true);
		this.active = true;
		this.alwaysShowBorder = bl;
		this.padding = j;
	}

	public void containWithin(int i) {
		this.setMaxWidth(i - this.padding * 4);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		narrationElementOutput.add(NarratedElementType.TITLE, this.getMessage());
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		if (this.isFocused() || this.alwaysShowBorder) {
			int k = this.getX() - this.padding;
			int l = this.getY() - this.padding;
			int m = this.getWidth() + this.padding * 2;
			int n = this.getHeight() + this.padding * 2;
			int o = this.alwaysShowBorder ? (this.isFocused() ? -1 : -6250336) : -1;
			guiGraphics.fill(k + 1, l, k + m, l + n, -16777216);
			guiGraphics.renderOutline(k, l, m, n, o);
		}

		super.renderWidget(guiGraphics, i, j, f);
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
	}
}
