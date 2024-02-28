package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ChatOptionsScreen extends SimpleOptionsSubScreen {
	private static final Component TITLE = Component.translatable("options.chat.title");

	public ChatOptionsScreen(Screen screen, Options options) {
		super(
			screen,
			options,
			TITLE,
			new OptionInstance[]{
				options.chatVisibility(),
				options.chatColors(),
				options.chatLinks(),
				options.chatLinksPrompt(),
				options.chatOpacity(),
				options.textBackgroundOpacity(),
				options.chatScale(),
				options.chatLineSpacing(),
				options.chatDelay(),
				options.chatWidth(),
				options.chatHeightFocused(),
				options.chatHeightUnfocused(),
				options.narrator(),
				options.autoSuggestions(),
				options.hideMatchedNames(),
				options.reducedDebugInfo(),
				options.onlyShowSecureChat()
			}
		);
	}
}
