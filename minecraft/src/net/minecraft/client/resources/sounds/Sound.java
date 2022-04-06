package net.minecraft.client.resources.sounds;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.SampledFloat;

@Environment(EnvType.CLIENT)
public class Sound implements Weighted<Sound> {
	private final ResourceLocation location;
	private final SampledFloat volume;
	private final SampledFloat pitch;
	private final int weight;
	private final Sound.Type type;
	private final boolean stream;
	private final boolean preload;
	private final int attenuationDistance;

	public Sound(String string, SampledFloat sampledFloat, SampledFloat sampledFloat2, int i, Sound.Type type, boolean bl, boolean bl2, int j) {
		this.location = new ResourceLocation(string);
		this.volume = sampledFloat;
		this.pitch = sampledFloat2;
		this.weight = i;
		this.type = type;
		this.stream = bl;
		this.preload = bl2;
		this.attenuationDistance = j;
	}

	public ResourceLocation getLocation() {
		return this.location;
	}

	public ResourceLocation getPath() {
		return new ResourceLocation(this.location.getNamespace(), "sounds/" + this.location.getPath() + ".ogg");
	}

	public SampledFloat getVolume() {
		return this.volume;
	}

	public SampledFloat getPitch() {
		return this.pitch;
	}

	@Override
	public int getWeight() {
		return this.weight;
	}

	public Sound getSound(RandomSource randomSource) {
		return this;
	}

	@Override
	public void preloadIfRequired(SoundEngine soundEngine) {
		if (this.preload) {
			soundEngine.requestPreload(this);
		}
	}

	public Sound.Type getType() {
		return this.type;
	}

	public boolean shouldStream() {
		return this.stream;
	}

	public boolean shouldPreload() {
		return this.preload;
	}

	public int getAttenuationDistance() {
		return this.attenuationDistance;
	}

	public String toString() {
		return "Sound[" + this.location + "]";
	}

	@Environment(EnvType.CLIENT)
	public static enum Type {
		FILE("file"),
		SOUND_EVENT("event");

		private final String name;

		private Type(String string2) {
			this.name = string2;
		}

		@Nullable
		public static Sound.Type getByName(String string) {
			for (Sound.Type type : values()) {
				if (type.name.equals(string)) {
					return type;
				}
			}

			return null;
		}
	}
}
