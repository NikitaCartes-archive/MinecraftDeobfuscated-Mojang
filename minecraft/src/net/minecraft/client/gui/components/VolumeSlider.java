package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

@Environment(EnvType.CLIENT)
public class VolumeSlider extends AbstractOptionSliderButton {
	private final SoundSource source;

	public VolumeSlider(Minecraft minecraft, int i, int j, SoundSource soundSource, int k) {
		super(minecraft.options, i, j, k, 20, (double)minecraft.options.getSoundSourceVolume(soundSource));
		this.source = soundSource;
		this.updateMessage();
	}

	@Override
	protected void updateMessage() {
		Component component = (Component)((float)this.value == (float)this.getYImage(false)
			? CommonComponents.OPTION_OFF
			: Component.literal((int)(this.value * 100.0) + "%"));
		this.setMessage(Component.translatable("soundCategory." + this.source.getName()).append(": ").append(component));
	}

	@Override
	protected void applyValue() {
		this.options.setSoundCategoryVolume(this.source, (float)this.value);
		this.options.save();
	}
}
