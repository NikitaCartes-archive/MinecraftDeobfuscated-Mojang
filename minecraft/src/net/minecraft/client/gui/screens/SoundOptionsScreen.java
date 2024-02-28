package net.minecraft.client.gui.screens;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

@Environment(EnvType.CLIENT)
public class SoundOptionsScreen extends OptionsSubScreen {
	private static final Component TITLE = Component.translatable("options.sounds.title");
	private OptionsList list;

	private static OptionInstance<?>[] buttonOptions(Options options) {
		return new OptionInstance[]{options.showSubtitles(), options.directionalAudio()};
	}

	public SoundOptionsScreen(Screen screen, Options options) {
		super(screen, options, TITLE);
	}

	@Override
	protected void init() {
		this.list = this.addRenderableWidget(new OptionsList(this.minecraft, this.width, this.height, this));
		this.list.addBig(this.options.getSoundSourceOptionInstance(SoundSource.MASTER));
		this.list.addSmall(this.getAllSoundOptionsExceptMaster());
		this.list.addBig(this.options.soundDevice());
		this.list.addSmall(buttonOptions(this.options));
		super.init();
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		this.list.updateSize(this.width, this.layout);
	}

	private OptionInstance<?>[] getAllSoundOptionsExceptMaster() {
		return (OptionInstance<?>[])Arrays.stream(SoundSource.values())
			.filter(soundSource -> soundSource != SoundSource.MASTER)
			.map(soundSource -> this.options.getSoundSourceOptionInstance(soundSource))
			.toArray(OptionInstance[]::new);
	}
}
