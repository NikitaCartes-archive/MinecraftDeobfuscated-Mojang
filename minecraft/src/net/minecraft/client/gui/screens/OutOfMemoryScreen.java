package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class OutOfMemoryScreen extends Screen {
	private static final Component TITLE = Component.translatable("outOfMemory.title");
	private static final Component MESSAGE = Component.translatable("outOfMemory.message");
	private static final int MESSAGE_WIDTH = 300;
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

	public OutOfMemoryScreen() {
		super(TITLE);
	}

	@Override
	protected void init() {
		this.layout.addTitleHeader(TITLE, this.font);
		this.layout.addToContents(new FocusableTextWidget(300, MESSAGE, this.font));
		LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
		linearLayout.addChild(Button.builder(CommonComponents.GUI_TO_TITLE, button -> this.minecraft.setScreen(new TitleScreen())).build());
		linearLayout.addChild(Button.builder(Component.translatable("menu.quit"), button -> this.minecraft.stop()).build());
		this.layout.visitWidgets(this::addRenderableWidget);
		this.repositionElements();
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
}
