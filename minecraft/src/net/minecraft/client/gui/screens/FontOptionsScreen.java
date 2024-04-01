package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class FontOptionsScreen extends SimpleOptionsSubScreen {
	private static OptionInstance<?>[] options(Options options) {
		return new OptionInstance[]{options.forceUnicodeFont(), options.japaneseGlyphVariants(), options.potatoFont()};
	}

	public FontOptionsScreen(Screen screen, Options options) {
		super(screen, options, Component.translatable("options.font.title"), options(options));
	}
}
