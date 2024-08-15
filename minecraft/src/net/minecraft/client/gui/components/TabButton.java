package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class TabButton extends AbstractWidget {
	private static final WidgetSprites SPRITES = new WidgetSprites(
		ResourceLocation.withDefaultNamespace("widget/tab_selected"),
		ResourceLocation.withDefaultNamespace("widget/tab"),
		ResourceLocation.withDefaultNamespace("widget/tab_selected_highlighted"),
		ResourceLocation.withDefaultNamespace("widget/tab_highlighted")
	);
	private static final int SELECTED_OFFSET = 3;
	private static final int TEXT_MARGIN = 1;
	private static final int UNDERLINE_HEIGHT = 1;
	private static final int UNDERLINE_MARGIN_X = 4;
	private static final int UNDERLINE_MARGIN_BOTTOM = 2;
	private final TabManager tabManager;
	private final Tab tab;

	public TabButton(TabManager tabManager, Tab tab, int i, int j) {
		super(0, 0, i, j, tab.getTabTitle());
		this.tabManager = tabManager;
		this.tab = tab;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		guiGraphics.blitSprite(RenderType::guiTextured, SPRITES.get(this.isSelected(), this.isHoveredOrFocused()), this.getX(), this.getY(), this.width, this.height);
		Font font = Minecraft.getInstance().font;
		int k = this.active ? -1 : -6250336;
		this.renderString(guiGraphics, font, k);
		if (this.isSelected()) {
			this.renderMenuBackground(guiGraphics, this.getX() + 2, this.getY() + 2, this.getRight() - 2, this.getBottom());
			this.renderFocusUnderline(guiGraphics, font, k);
		}
	}

	protected void renderMenuBackground(GuiGraphics guiGraphics, int i, int j, int k, int l) {
		Screen.renderMenuBackgroundTexture(guiGraphics, Screen.MENU_BACKGROUND, i, j, 0.0F, 0.0F, k - i, l - j);
	}

	public void renderString(GuiGraphics guiGraphics, Font font, int i) {
		int j = this.getX() + 1;
		int k = this.getY() + (this.isSelected() ? 0 : 3);
		int l = this.getX() + this.getWidth() - 1;
		int m = this.getY() + this.getHeight();
		renderScrollingString(guiGraphics, font, this.getMessage(), j, k, l, m, i);
	}

	private void renderFocusUnderline(GuiGraphics guiGraphics, Font font, int i) {
		int j = Math.min(font.width(this.getMessage()), this.getWidth() - 4);
		int k = this.getX() + (this.getWidth() - j) / 2;
		int l = this.getY() + this.getHeight() - 2;
		guiGraphics.fill(k, l, k + j, l + 1, i);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		narrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("gui.narrate.tab", this.tab.getTabTitle()));
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
	}

	public Tab tab() {
		return this.tab;
	}

	public boolean isSelected() {
		return this.tabManager.getCurrentTab() == this.tab;
	}
}
