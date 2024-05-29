package net.minecraft.client.gui.screens;

import java.net.URI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonLinks;

@Environment(EnvType.CLIENT)
public class NoticeWithLinkScreen extends Screen {
	private static final Component SYMLINK_WORLD_TITLE = Component.translatable("symlink_warning.title.world").withStyle(ChatFormatting.BOLD);
	private static final Component SYMLINK_WORLD_MESSAGE_TEXT = Component.translatable(
		"symlink_warning.message.world", Component.translationArg(CommonLinks.SYMLINK_HELP)
	);
	private static final Component SYMLINK_PACK_TITLE = Component.translatable("symlink_warning.title.pack").withStyle(ChatFormatting.BOLD);
	private static final Component SYMLINK_PACK_MESSAGE_TEXT = Component.translatable(
		"symlink_warning.message.pack", Component.translationArg(CommonLinks.SYMLINK_HELP)
	);
	private final Component message;
	private final URI uri;
	private final Runnable onClose;
	private final GridLayout layout = new GridLayout().rowSpacing(10);

	public NoticeWithLinkScreen(Component component, Component component2, URI uRI, Runnable runnable) {
		super(component);
		this.message = component2;
		this.uri = uRI;
		this.onClose = runnable;
	}

	public static Screen createWorldSymlinkWarningScreen(Runnable runnable) {
		return new NoticeWithLinkScreen(SYMLINK_WORLD_TITLE, SYMLINK_WORLD_MESSAGE_TEXT, CommonLinks.SYMLINK_HELP, runnable);
	}

	public static Screen createPackSymlinkWarningScreen(Runnable runnable) {
		return new NoticeWithLinkScreen(SYMLINK_PACK_TITLE, SYMLINK_PACK_MESSAGE_TEXT, CommonLinks.SYMLINK_HELP, runnable);
	}

	@Override
	protected void init() {
		super.init();
		this.layout.defaultCellSetting().alignHorizontallyCenter();
		GridLayout.RowHelper rowHelper = this.layout.createRowHelper(1);
		rowHelper.addChild(new StringWidget(this.title, this.font));
		rowHelper.addChild(new MultiLineTextWidget(this.message, this.font).setMaxWidth(this.width - 50).setCentered(true));
		int i = 120;
		GridLayout gridLayout = new GridLayout().columnSpacing(5);
		GridLayout.RowHelper rowHelper2 = gridLayout.createRowHelper(3);
		rowHelper2.addChild(Button.builder(CommonComponents.GUI_OPEN_IN_BROWSER, button -> Util.getPlatform().openUri(this.uri)).size(120, 20).build());
		rowHelper2.addChild(
			Button.builder(CommonComponents.GUI_COPY_LINK_TO_CLIPBOARD, button -> this.minecraft.keyboardHandler.setClipboard(this.uri.toString()))
				.size(120, 20)
				.build()
		);
		rowHelper2.addChild(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).size(120, 20).build());
		rowHelper.addChild(gridLayout);
		this.repositionElements();
		this.layout.visitWidgets(this::addRenderableWidget);
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
		FrameLayout.centerInRectangle(this.layout, this.getRectangle());
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinForNarration(super.getNarrationMessage(), this.message);
	}

	@Override
	public void onClose() {
		this.onClose.run();
	}
}
