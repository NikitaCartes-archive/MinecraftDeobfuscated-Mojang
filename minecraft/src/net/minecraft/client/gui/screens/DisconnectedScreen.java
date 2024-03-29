package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class DisconnectedScreen extends Screen {
	private static final Component TO_SERVER_LIST = Component.translatable("gui.toMenu");
	private static final Component TO_TITLE = Component.translatable("gui.toTitle");
	private final Screen parent;
	private final Component reason;
	private final Component buttonText;
	private final LinearLayout layout = LinearLayout.vertical();

	public DisconnectedScreen(Screen screen, Component component, Component component2) {
		this(screen, component, component2, TO_SERVER_LIST);
	}

	public DisconnectedScreen(Screen screen, Component component, Component component2, Component component3) {
		super(component);
		this.parent = screen;
		this.reason = component2;
		this.buttonText = component3;
	}

	@Override
	protected void init() {
		this.layout.defaultCellSetting().alignHorizontallyCenter().padding(10);
		this.layout.addChild(new StringWidget(this.title, this.font));
		this.layout.addChild(new MultiLineTextWidget(this.reason, this.font).setMaxWidth(this.width - 50).setCentered(true));
		Button button;
		if (this.minecraft.allowsMultiplayer()) {
			button = Button.builder(this.buttonText, buttonx -> this.minecraft.setScreen(this.parent)).build();
		} else {
			button = Button.builder(TO_TITLE, buttonx -> this.minecraft.setScreen(new TitleScreen())).build();
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
		return CommonComponents.joinForNarration(this.title, this.reason);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
}
