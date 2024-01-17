package net.minecraft.client.gui.screens.telemetry;

import java.nio.file.Path;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;
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
	private static final Component CHECKBOX_OPT_IN = Component.translatable("telemetry_info.opt_in.description");
	private final Screen lastScreen;
	private final Options options;
	@Nullable
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
		GridLayout gridLayout = this.twoButtonContainer(
			Button.builder(BUTTON_PRIVACY_STATEMENT, this::openPrivacyStatementLink).build(), Button.builder(BUTTON_GIVE_FEEDBACK, this::openFeedbackLink).build()
		);
		linearLayout.addChild(gridLayout);
		Layout layout = this.createLowerSection();
		frameLayout.arrangeElements();
		layout.arrangeElements();
		int i = gridLayout.getY() + gridLayout.getHeight();
		int j = layout.getHeight();
		int k = this.height - i - j - 16;
		this.telemetryEventWidget = new TelemetryEventWidget(0, 0, this.width - 40, k, this.minecraft.font);
		this.telemetryEventWidget.setScrollAmount(this.savedScroll);
		this.telemetryEventWidget.setOnScrolledListener(d -> this.savedScroll = d);
		linearLayout.addChild(this.telemetryEventWidget);
		linearLayout.addChild(layout);
		frameLayout.arrangeElements();
		FrameLayout.alignInRectangle(frameLayout, 0, 0, this.width, this.height, 0.5F, 0.0F);
		frameLayout.visitWidgets(guiEventListener -> {
			AbstractWidget var10000 = this.addRenderableWidget(guiEventListener);
		});
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.telemetryEventWidget);
	}

	private Layout createLowerSection() {
		LinearLayout linearLayout = LinearLayout.vertical();
		linearLayout.defaultCellSetting().alignHorizontallyCenter().paddingBottom(4);
		if (this.minecraft.extraTelemetryAvailable()) {
			linearLayout.addChild(this.createTelemetryCheckbox());
		}

		linearLayout.addChild(
			this.twoButtonContainer(
				Button.builder(BUTTON_SHOW_DATA, this::openDataFolder).build(), Button.builder(CommonComponents.GUI_DONE, this::openLastScreen).build()
			)
		);
		return linearLayout;
	}

	private AbstractWidget createTelemetryCheckbox() {
		OptionInstance<Boolean> optionInstance = this.options.telemetryOptInExtra();
		Checkbox checkbox = Checkbox.builder(CHECKBOX_OPT_IN, this.minecraft.font).selected(optionInstance).onValueChange(this::onOptInChanged).build();
		checkbox.active = this.minecraft.extraTelemetryAvailable();
		return checkbox;
	}

	private void onOptInChanged(AbstractWidget abstractWidget, boolean bl) {
		if (this.telemetryEventWidget != null) {
			this.telemetryEventWidget.onOptInChanged(bl);
		}
	}

	private void openLastScreen(Button button) {
		this.minecraft.setScreen(this.lastScreen);
	}

	private void openPrivacyStatementLink(Button button) {
		ConfirmLinkScreen.confirmLinkNow(this, "http://go.microsoft.com/fwlink/?LinkId=521839");
	}

	private void openFeedbackLink(Button button) {
		ConfirmLinkScreen.confirmLinkNow(this, "https://aka.ms/javafeedback?ref=game");
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
