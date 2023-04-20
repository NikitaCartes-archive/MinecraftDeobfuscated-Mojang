package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class CreditsAndAttributionScreen extends Screen {
	private static final int BUTTON_SPACING = 8;
	private static final int BUTTON_WIDTH = 210;
	private static final Component TITLE = Component.translatable("credits_and_attribution.screen.title");
	private static final Component CREDITS_BUTTON = Component.translatable("credits_and_attribution.button.credits");
	private static final Component ATTRIBUTION_BUTTON = Component.translatable("credits_and_attribution.button.attribution");
	private static final Component LICENSES_BUTTON = Component.translatable("credits_and_attribution.button.licenses");
	private final Screen lastScreen;
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

	public CreditsAndAttributionScreen(Screen screen) {
		super(TITLE);
		this.lastScreen = screen;
	}

	@Override
	protected void init() {
		this.layout.addToHeader(new StringWidget(this.getTitle(), this.font));
		GridLayout gridLayout = this.layout.addToContents(new GridLayout()).spacing(8);
		gridLayout.defaultCellSetting().alignHorizontallyCenter();
		GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(1);
		rowHelper.addChild(Button.builder(CREDITS_BUTTON, button -> this.openCreditsScreen()).width(210).build());
		rowHelper.addChild(
			Button.builder(ATTRIBUTION_BUTTON, ConfirmLinkScreen.confirmLink("https://aka.ms/MinecraftJavaAttribution", this, true)).width(210).build()
		);
		rowHelper.addChild(Button.builder(LICENSES_BUTTON, ConfirmLinkScreen.confirmLink("https://aka.ms/MinecraftJavaLicenses", this, true)).width(210).build());
		this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).build());
		this.layout.arrangeElements();
		this.layout.visitWidgets(this::addRenderableWidget);
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
	}

	private void openCreditsScreen() {
		this.minecraft.setScreen(new WinScreen(false, () -> this.minecraft.setScreen(this)));
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderBackground(guiGraphics);
		super.render(guiGraphics, i, j, f);
	}
}
