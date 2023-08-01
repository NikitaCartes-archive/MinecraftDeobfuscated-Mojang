package net.minecraft.client.gui.screens.telemetry;

import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class TelemetryInfoScreen extends Screen {
	private static final int PADDING = 8;
	private static final Component TITLE = Component.translatable("telemetry_info.screen.title");
	private static final Component DESCRIPTION = Component.translatable("telemetry_info.screen.description").withStyle(ChatFormatting.GRAY);
	private static final Component BUTTON_PRIVACY_STATEMENT = Component.translatable("telemetry_info.button.privacy_statement");
	private static final Component BUTTON_GIVE_FEEDBACK = Component.translatable("telemetry_info.button.give_feedback");
	private static final Component BUTTON_SHOW_DATA = Component.translatable("telemetry_info.button.show_data");
	private final Screen lastScreen;
	private final Options options;
	private TelemetryEventWidget telemetryEventWidget;
	private double savedScroll;

	public TelemetryInfoScreen(Screen screen, Options options) {
		super(TITLE);
		this.lastScreen = screen;
		this.options = options;
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinForNarration(super.getNarrationMessage(), DESCRIPTION);
	}

	@Override
	protected void init() {
		FrameLayout frameLayout = new FrameLayout();
		frameLayout.defaultChildLayoutSetting().padding(8);
		frameLayout.setMinHeight(this.height);
		LinearLayout linearLayout = frameLayout.addChild(LinearLayout.vertical(), frameLayout.newChildLayoutSettings().align(0.5F, 0.0F));
		linearLayout.defaultCellSetting().alignHorizontallyCenter().paddingBottom(8);
		linearLayout.addChild(new StringWidget(this.getTitle(), this.font));
		linearLayout.addChild(new MultiLineTextWidget(DESCRIPTION, this.font).setMaxWidth(this.width - 16).setCentered(true));
		Button button = Button.builder(BUTTON_PRIVACY_STATEMENT, this::openPrivacyStatementLink).build();
		linearLayout.addChild(button);
		GridLayout gridLayout = this.twoButtonContainer(
			Button.builder(BUTTON_GIVE_FEEDBACK, this::openFeedbackLink).build(), Button.builder(BUTTON_SHOW_DATA, this::openDataFolder).build()
		);
		linearLayout.addChild(gridLayout);
		GridLayout gridLayout2 = this.twoButtonContainer(this.createTelemetryButton(), Button.builder(CommonComponents.GUI_DONE, this::openLastScreen).build());
		frameLayout.addChild(gridLayout2, frameLayout.newChildLayoutSettings().align(0.5F, 1.0F));
		frameLayout.arrangeElements();
		this.telemetryEventWidget = new TelemetryEventWidget(
			0, 0, this.width - 40, gridLayout2.getY() - (gridLayout.getY() + gridLayout.getHeight()) - 16, this.minecraft.font
		);
		this.telemetryEventWidget.setScrollAmount(this.savedScroll);
		this.telemetryEventWidget.setOnScrolledListener(d -> this.savedScroll = d);
		this.setInitialFocus(this.telemetryEventWidget);
		linearLayout.addChild(this.telemetryEventWidget);
		frameLayout.arrangeElements();
		FrameLayout.alignInRectangle(frameLayout, 0, 0, this.width, this.height, 0.5F, 0.0F);
		frameLayout.visitWidgets(guiEventListener -> {
			AbstractWidget var10000 = this.addRenderableWidget(guiEventListener);
		});
	}

	private AbstractWidget createTelemetryButton() {
		AbstractWidget abstractWidget = this.options
			.telemetryOptInExtra()
			.createButton(this.options, 0, 0, 150, boolean_ -> this.telemetryEventWidget.onOptInChanged(boolean_));
		abstractWidget.active = this.minecraft.extraTelemetryAvailable();
		return abstractWidget;
	}

	private void openLastScreen(Button button) {
		this.minecraft.setScreen(this.lastScreen);
	}

	private void openPrivacyStatementLink(Button button) {
		this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
			if (bl) {
				Util.getPlatform().openUri("http://go.microsoft.com/fwlink/?LinkId=521839");
			}

			this.minecraft.setScreen(this);
		}, "http://go.microsoft.com/fwlink/?LinkId=521839", true));
	}

	private void openFeedbackLink(Button button) {
		this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
			if (bl) {
				Util.getPlatform().openUri("https://aka.ms/javafeedback?ref=game");
			}

			this.minecraft.setScreen(this);
		}, "https://aka.ms/javafeedback?ref=game", true));
	}

	private void openDataFolder(Button button) {
		Path path = this.minecraft.getTelemetryManager().getLogDirectory();
		Util.getPlatform().openUri(path.toUri());
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderDirtBackground(guiGraphics);
	}

	private GridLayout twoButtonContainer(AbstractWidget abstractWidget, AbstractWidget abstractWidget2) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.defaultCellSetting().alignHorizontallyCenter().paddingHorizontal(4);
		gridLayout.addChild(abstractWidget, 0, 0);
		gridLayout.addChild(abstractWidget2, 0, 1);
		return gridLayout;
	}
}
