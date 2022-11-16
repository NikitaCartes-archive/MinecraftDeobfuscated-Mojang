package net.minecraft.client.gui.screens.telemetry;

import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CenteredStringWidget;
import net.minecraft.client.gui.components.FrameWidget;
import net.minecraft.client.gui.components.GridWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class TelemetryInfoScreen extends Screen {
	private static final int PADDING = 8;
	private static final String FEEDBACK_URL = "https://feedback.minecraft.net";
	private static final Component TITLE = Component.translatable("telemetry_info.screen.title");
	private static final Component DESCRIPTION = Component.translatable("telemetry_info.screen.description").withStyle(ChatFormatting.GRAY);
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
		FrameWidget frameWidget = new FrameWidget(0, 0, this.width, this.height);
		frameWidget.defaultChildLayoutSetting().padding(8);
		frameWidget.setMinHeight(this.height);
		GridWidget gridWidget = frameWidget.addChild(new GridWidget(), frameWidget.newChildLayoutSettings().align(0.5F, 0.0F));
		gridWidget.defaultCellSetting().alignHorizontallyCenter().paddingBottom(8);
		GridWidget.RowHelper rowHelper = gridWidget.createRowHelper(1);
		rowHelper.addChild(new CenteredStringWidget(this.getTitle(), this.font));
		rowHelper.addChild(MultiLineTextWidget.createCentered(this.width - 16, this.font, DESCRIPTION));
		GridWidget gridWidget2 = this.twoButtonContainer(
			Button.builder(BUTTON_GIVE_FEEDBACK, this::openFeedbackLink).build(), Button.builder(BUTTON_SHOW_DATA, this::openDataFolder).build()
		);
		rowHelper.addChild(gridWidget2);
		GridWidget gridWidget3 = this.twoButtonContainer(this.createTelemetryButton(), Button.builder(CommonComponents.GUI_DONE, this::openLastScreen).build());
		frameWidget.addChild(gridWidget3, frameWidget.newChildLayoutSettings().align(0.5F, 1.0F));
		gridWidget.pack();
		frameWidget.pack();
		this.telemetryEventWidget = new TelemetryEventWidget(
			0, 0, this.width - 40, gridWidget3.getY() - (gridWidget2.getY() + gridWidget2.getHeight()) - 16, this.minecraft.font
		);
		this.telemetryEventWidget.setScrollAmount(this.savedScroll);
		this.telemetryEventWidget.setOnScrolledListener(d -> this.savedScroll = d);
		this.setInitialFocus(this.telemetryEventWidget);
		rowHelper.addChild(this.telemetryEventWidget);
		gridWidget.pack();
		frameWidget.pack();
		FrameWidget.alignInRectangle(frameWidget, 0, 0, this.width, this.height, 0.5F, 0.0F);
		this.addRenderableWidget(frameWidget);
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

	private void openFeedbackLink(Button button) {
		this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
			if (bl) {
				Util.getPlatform().openUri("https://feedback.minecraft.net");
			}

			this.minecraft.setScreen(this);
		}, "https://feedback.minecraft.net", true));
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
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderDirtBackground(0);
		super.render(poseStack, i, j, f);
	}

	private GridWidget twoButtonContainer(AbstractWidget abstractWidget, AbstractWidget abstractWidget2) {
		GridWidget gridWidget = new GridWidget();
		gridWidget.defaultCellSetting().alignHorizontallyCenter().paddingHorizontal(4);
		gridWidget.addChild(abstractWidget, 0, 0);
		gridWidget.addChild(abstractWidget2, 0, 1);
		gridWidget.pack();
		return gridWidget;
	}
}
