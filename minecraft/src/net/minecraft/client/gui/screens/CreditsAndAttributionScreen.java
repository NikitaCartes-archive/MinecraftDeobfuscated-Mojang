package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonLinks;

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
		this.layout.addTitleHeader(TITLE, this.font);
		LinearLayout linearLayout = this.layout.addToContents(LinearLayout.vertical()).spacing(8);
		linearLayout.defaultCellSetting().alignHorizontallyCenter();
		linearLayout.addChild(Button.builder(CREDITS_BUTTON, button -> this.openCreditsScreen()).width(210).build());
		linearLayout.addChild(Button.builder(ATTRIBUTION_BUTTON, ConfirmLinkScreen.confirmLink(this, CommonLinks.ATTRIBUTION)).width(210).build());
		linearLayout.addChild(Button.builder(LICENSES_BUTTON, ConfirmLinkScreen.confirmLink(this, CommonLinks.LICENSES)).width(210).build());
		this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(200).build());
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
}
