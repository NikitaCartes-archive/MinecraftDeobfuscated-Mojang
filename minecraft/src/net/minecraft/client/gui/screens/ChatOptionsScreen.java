package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class ChatOptionsScreen extends SimpleOptionsSubScreen {
	public ChatOptionsScreen(Screen screen, Options options) {
		super(
			screen,
			options,
			new TranslatableComponent("options.chat.title"),
			new Option[]{
				Option.CHAT_VISIBILITY,
				Option.CHAT_COLOR,
				Option.CHAT_LINKS,
				Option.CHAT_LINKS_PROMPT,
				Option.CHAT_OPACITY,
				Option.TEXT_BACKGROUND_OPACITY,
				Option.CHAT_SCALE,
				Option.CHAT_LINE_SPACING,
				Option.CHAT_DELAY,
				Option.CHAT_WIDTH,
				options.chatHeightFocused(),
				options.chatHeightUnfocused(),
				Option.NARRATOR,
				Option.AUTO_SUGGESTIONS,
				Option.HIDE_MATCHED_NAMES,
				Option.REDUCED_DEBUG_INFO
			}
		);
	}
}
