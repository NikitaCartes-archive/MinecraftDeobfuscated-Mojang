package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class ChatOptionsScreen extends OptionsSubScreen {
	private static final Option[] CHAT_OPTIONS = new Option[]{
		Option.CHAT_VISIBILITY,
		Option.CHAT_COLOR,
		Option.CHAT_LINKS,
		Option.CHAT_LINKS_PROMPT,
		Option.CHAT_OPACITY,
		Option.TEXT_BACKGROUND_OPACITY,
		Option.CHAT_SCALE,
		Option.CHAT_LINE_SPACING,
		Option.CHAT_WIDTH,
		Option.CHAT_HEIGHT_FOCUSED,
		Option.CHAT_HEIGHT_UNFOCUSED,
		Option.NARRATOR,
		Option.AUTO_SUGGESTIONS,
		Option.REDUCED_DEBUG_INFO
	};
	private AbstractWidget narratorButton;

	public ChatOptionsScreen(Screen screen, Options options) {
		super(screen, options, new TranslatableComponent("options.chat.title"));
	}

	@Override
	protected void init() {
		int i = 0;

		for (Option option : CHAT_OPTIONS) {
			int j = this.width / 2 - 155 + i % 2 * 160;
			int k = this.height / 6 + 24 * (i >> 1);
			AbstractWidget abstractWidget = this.addButton(option.createButton(this.minecraft.options, j, k, 150));
			if (option == Option.NARRATOR) {
				this.narratorButton = abstractWidget;
				abstractWidget.active = NarratorChatListener.INSTANCE.isActive();
			}

			i++;
		}

		this.addButton(
			new Button(this.width / 2 - 100, this.height / 6 + 24 * (i + 1) / 2, 200, 20, I18n.get("gui.done"), button -> this.minecraft.setScreen(this.lastScreen))
		);
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 20, 16777215);
		super.render(i, j, f);
	}

	public void updateNarratorButton() {
		this.narratorButton.setMessage(Option.NARRATOR.getMessage(this.options));
	}
}
