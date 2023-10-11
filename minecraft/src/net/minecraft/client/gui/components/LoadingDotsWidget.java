package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class LoadingDotsWidget extends AbstractWidget {
	private final Font font;

	public LoadingDotsWidget(Font font, Component component) {
		super(0, 0, font.width(component), 9 * 3, component);
		this.font = font;
	}

	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		int k = this.getX() + this.getWidth() / 2;
		int l = this.getY() + this.getHeight() / 2;
		Component component = this.getMessage();
		guiGraphics.drawString(this.font, component, k - this.font.width(component) / 2, l - 9, -1, false);
		String string = LoadingDotsText.get(Util.getMillis());
		guiGraphics.drawString(this.font, string, k - this.font.width(string) / 2, l + 9, -8355712, false);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Nullable
	@Override
	public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
		return null;
	}
}
