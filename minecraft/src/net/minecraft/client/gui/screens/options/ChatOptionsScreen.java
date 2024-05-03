package net.minecraft.client.gui.screens.options;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ChatOptionsScreen extends OptionsSubScreen {
	private static final Component TITLE = Component.translatable("options.chat.title");
	@Nullable
	private AbstractWidget narratorButton;

	private static OptionInstance<?>[] options(Options options) {
		return new OptionInstance[]{
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
		};
	}

	public ChatOptionsScreen(Screen screen, Options options) {
		super(screen, options, TITLE);
	}

	@Override
	public void init() {
		super.init();
		this.narratorButton = this.list.findOption(this.options.narrator());
		if (this.narratorButton != null) {
			this.narratorButton.active = this.minecraft.getNarrator().isActive();
		}
	}

	@Override
	protected void addOptions() {
		this.list.addSmall(options(this.options));
	}

	public void updateNarratorButton() {
		if (this.narratorButton instanceof CycleButton) {
			((CycleButton)this.narratorButton).setValue(this.options.narrator().get());
		}
	}
}
