package net.minecraft.client.gui.screens.options;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class FontOptionsScreen extends OptionsSubScreen {
	private static OptionInstance<?>[] options(Options options) {
		return new OptionInstance[]{options.forceUnicodeFont(), options.japaneseGlyphVariants()};
	}

	public FontOptionsScreen(Screen screen, Options options) {
		super(screen, options, Component.translatable("options.font.title"));
	}

	@Override
	protected void addOptions() {
		this.list.addSmall(options(this.options));
	}
}
