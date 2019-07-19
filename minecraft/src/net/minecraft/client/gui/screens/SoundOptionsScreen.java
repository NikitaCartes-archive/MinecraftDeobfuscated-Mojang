package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.components.VolumeSlider;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundSource;

@Environment(EnvType.CLIENT)
public class SoundOptionsScreen extends Screen {
	private final Screen lastScreen;
	private final Options options;

	public SoundOptionsScreen(Screen screen, Options options) {
		super(new TranslatableComponent("options.sounds.title"));
		this.lastScreen = screen;
		this.options = options;
	}

	@Override
	protected void init() {
		int i = 0;
		this.addButton(new VolumeSlider(this.minecraft, this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), SoundSource.MASTER, 310));
		i += 2;

		for (SoundSource soundSource : SoundSource.values()) {
			if (soundSource != SoundSource.MASTER) {
				this.addButton(new VolumeSlider(this.minecraft, this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), soundSource, 150));
				i++;
			}
		}

		this.addButton(
			new OptionButton(
				this.width / 2 - 75, this.height / 6 - 12 + 24 * (++i >> 1), 150, 20, Option.SHOW_SUBTITLES, Option.SHOW_SUBTITLES.getMessage(this.options), button -> {
					Option.SHOW_SUBTITLES.toggle(this.minecraft.options);
					button.setMessage(Option.SHOW_SUBTITLES.getMessage(this.minecraft.options));
					this.minecraft.options.save();
				}
			)
		);
		this.addButton(new Button(this.width / 2 - 100, this.height / 6 + 168, 200, 20, I18n.get("gui.done"), button -> this.minecraft.setScreen(this.lastScreen)));
	}

	@Override
	public void removed() {
		this.minecraft.options.save();
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 15, 16777215);
		super.render(i, j, f);
	}
}
