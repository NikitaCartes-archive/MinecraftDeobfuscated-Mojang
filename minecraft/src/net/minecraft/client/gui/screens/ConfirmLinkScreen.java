package net.minecraft.client.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class ConfirmLinkScreen extends ConfirmScreen {
	private final String warning;
	private final String copyButton;
	private final String url;
	private final boolean showWarning;

	public ConfirmLinkScreen(BooleanConsumer booleanConsumer, String string, boolean bl) {
		super(booleanConsumer, new TranslatableComponent(bl ? "chat.link.confirmTrusted" : "chat.link.confirm"), new TextComponent(string));
		this.yesButton = I18n.get(bl ? "chat.link.open" : "gui.yes");
		this.noButton = I18n.get(bl ? "gui.cancel" : "gui.no");
		this.copyButton = I18n.get("chat.copy");
		this.warning = I18n.get("chat.link.warning");
		this.showWarning = !bl;
		this.url = string;
	}

	@Override
	protected void init() {
		super.init();
		this.buttons.clear();
		this.children.clear();
		this.addButton(new Button(this.width / 2 - 50 - 105, this.height / 6 + 96, 100, 20, this.yesButton, button -> this.callback.accept(true)));
		this.addButton(new Button(this.width / 2 - 50, this.height / 6 + 96, 100, 20, this.copyButton, button -> {
			this.copyToClipboard();
			this.callback.accept(false);
		}));
		this.addButton(new Button(this.width / 2 - 50 + 105, this.height / 6 + 96, 100, 20, this.noButton, button -> this.callback.accept(false)));
	}

	public void copyToClipboard() {
		this.minecraft.keyboardHandler.setClipboard(this.url);
	}

	@Override
	public void render(int i, int j, float f) {
		super.render(i, j, f);
		if (this.showWarning) {
			this.drawCenteredString(this.font, this.warning, this.width / 2, 110, 16764108);
		}
	}
}
