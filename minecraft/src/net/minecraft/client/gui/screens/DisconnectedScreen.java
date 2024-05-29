package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class DisconnectedScreen extends Screen {
	private static final Component TO_SERVER_LIST = Component.translatable("gui.toMenu");
	private static final Component TO_TITLE = Component.translatable("gui.toTitle");
	private static final Component REPORT_TO_SERVER_TITLE = Component.translatable("gui.report_to_server");
	private static final Component OPEN_REPORT_DIR_TITLE = Component.translatable("gui.open_report_dir");
	private final Screen parent;
	private final DisconnectionDetails details;
	private final Component buttonText;
	private final LinearLayout layout = LinearLayout.vertical();

	public DisconnectedScreen(Screen screen, Component component, Component component2) {
		this(screen, component, new DisconnectionDetails(component2));
	}

	public DisconnectedScreen(Screen screen, Component component, Component component2, Component component3) {
		this(screen, component, new DisconnectionDetails(component2), component3);
	}

	public DisconnectedScreen(Screen screen, Component component, DisconnectionDetails disconnectionDetails) {
		this(screen, component, disconnectionDetails, TO_SERVER_LIST);
	}

	public DisconnectedScreen(Screen screen, Component component, DisconnectionDetails disconnectionDetails, Component component2) {
		super(component);
		this.parent = screen;
		this.details = disconnectionDetails;
		this.buttonText = component2;
	}

	@Override
	protected void init() {
		this.layout.defaultCellSetting().alignHorizontallyCenter().padding(10);
		this.layout.addChild(new StringWidget(this.title, this.font));
		this.layout.addChild(new MultiLineTextWidget(this.details.reason(), this.font).setMaxWidth(this.width - 50).setCentered(true));
		this.layout.defaultCellSetting().padding(2);
		this.details
			.bugReportLink()
			.ifPresent(uRI -> this.layout.addChild(Button.builder(REPORT_TO_SERVER_TITLE, ConfirmLinkScreen.confirmLink(this, uRI, false)).width(200).build()));
		this.details
			.report()
			.ifPresent(path -> this.layout.addChild(Button.builder(OPEN_REPORT_DIR_TITLE, buttonx -> Util.getPlatform().openPath(path.getParent())).width(200).build()));
		Button button;
		if (this.minecraft.allowsMultiplayer()) {
			button = Button.builder(this.buttonText, buttonx -> this.minecraft.setScreen(this.parent)).width(200).build();
		} else {
			button = Button.builder(TO_TITLE, buttonx -> this.minecraft.setScreen(new TitleScreen())).width(200).build();
		}

		this.layout.addChild(button);
		this.layout.arrangeElements();
		this.layout.visitWidgets(this::addRenderableWidget);
		this.repositionElements();
	}

	@Override
	protected void repositionElements() {
		FrameLayout.centerInRectangle(this.layout, this.getRectangle());
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinForNarration(this.title, this.details.reason());
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
}
