package net.minecraft.client.sounds;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class WeighedSoundEvents implements Weighted<Sound> {
	private final List<Weighted<Sound>> list = Lists.<Weighted<Sound>>newArrayList();
	private final Random random = new Random();
	private final ResourceLocation location;
	private final Component subtitle;

	public WeighedSoundEvents(ResourceLocation resourceLocation, @Nullable String string) {
		this.location = resourceLocation;
		this.subtitle = string == null ? null : new TranslatableComponent(string);
	}

	@Override
	public int getWeight() {
		int i = 0;

		for (Weighted<Sound> weighted : this.list) {
			i += weighted.getWeight();
		}

		return i;
	}

	public Sound getSound() {
		int i = this.getWeight();
		if (!this.list.isEmpty() && i != 0) {
			int j = this.random.nextInt(i);

			for (Weighted<Sound> weighted : this.list) {
				j -= weighted.getWeight();
				if (j < 0) {
					return weighted.getSound();
				}
			}

			return SoundManager.EMPTY_SOUND;
		} else {
			return SoundManager.EMPTY_SOUND;
		}
	}

	public void addSound(Weighted<Sound> weighted) {
		this.list.add(weighted);
	}

	@Nullable
	public Component getSubtitle() {
		return this.subtitle;
	}

	@Override
	public void preloadIfRequired(SoundEngine soundEngine) {
		for (Weighted<Sound> weighted : this.list) {
			weighted.preloadIfRequired(soundEngine);
		}
	}
}
