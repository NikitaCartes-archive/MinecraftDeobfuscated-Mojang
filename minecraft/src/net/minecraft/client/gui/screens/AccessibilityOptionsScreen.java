package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class AccessibilityOptionsScreen extends SimpleOptionsSubScreen {
	private static final Option[] OPTIONS = new Option[]{
		Option.NARRATOR,
		Option.SHOW_SUBTITLES,
		Option.TEXT_BACKGROUND_OPACITY,
		Option.TEXT_BACKGROUND,
		Option.CHAT_OPACITY,
		Option.CHAT_LINE_SPACING,
		Option.CHAT_DELAY,
		Option.AUTO_JUMP,
		Option.TOGGLE_CROUCH,
		Option.TOGGLE_SPRINT,
		Option.SCREEN_EFFECTS_SCALE,
		Option.FOV_EFFECTS_SCALE
	};

	public AccessibilityOptionsScreen(Screen screen, Options options) {
		super(screen, options, new TranslatableComponent("options.accessibility.title"), OPTIONS);
	}
}
