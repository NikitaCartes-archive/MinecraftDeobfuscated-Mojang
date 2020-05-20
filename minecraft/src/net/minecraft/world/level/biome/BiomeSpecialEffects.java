package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;

public class BiomeSpecialEffects {
	public static final Codec<BiomeSpecialEffects> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.INT.fieldOf("fog_color").forGetter(biomeSpecialEffects -> biomeSpecialEffects.fogColor),
					Codec.INT.fieldOf("water_color").forGetter(biomeSpecialEffects -> biomeSpecialEffects.waterColor),
					Codec.INT.fieldOf("water_fog_color").forGetter(biomeSpecialEffects -> biomeSpecialEffects.waterFogColor),
					AmbientParticleSettings.CODEC.optionalFieldOf("particle").forGetter(biomeSpecialEffects -> biomeSpecialEffects.ambientParticleSettings),
					SoundEvent.CODEC.optionalFieldOf("ambient_sound").forGetter(biomeSpecialEffects -> biomeSpecialEffects.ambientLoopSoundEvent),
					AmbientMoodSettings.CODEC.optionalFieldOf("mood_sound").forGetter(biomeSpecialEffects -> biomeSpecialEffects.ambientMoodSettings),
					AmbientAdditionsSettings.CODEC.optionalFieldOf("additions_sound").forGetter(biomeSpecialEffects -> biomeSpecialEffects.ambientAdditionsSettings),
					Music.CODEC.optionalFieldOf("music").forGetter(biomeSpecialEffects -> biomeSpecialEffects.backgroundMusic)
				)
				.apply(instance, BiomeSpecialEffects::new)
	);
	private final int fogColor;
	private final int waterColor;
	private final int waterFogColor;
	private final Optional<AmbientParticleSettings> ambientParticleSettings;
	private final Optional<SoundEvent> ambientLoopSoundEvent;
	private final Optional<AmbientMoodSettings> ambientMoodSettings;
	private final Optional<AmbientAdditionsSettings> ambientAdditionsSettings;
	private final Optional<Music> backgroundMusic;

	private BiomeSpecialEffects(
		int i,
		int j,
		int k,
		Optional<AmbientParticleSettings> optional,
		Optional<SoundEvent> optional2,
		Optional<AmbientMoodSettings> optional3,
		Optional<AmbientAdditionsSettings> optional4,
		Optional<Music> optional5
	) {
		this.fogColor = i;
		this.waterColor = j;
		this.waterFogColor = k;
		this.ambientParticleSettings = optional;
		this.ambientLoopSoundEvent = optional2;
		this.ambientMoodSettings = optional3;
		this.ambientAdditionsSettings = optional4;
		this.backgroundMusic = optional5;
	}

	@Environment(EnvType.CLIENT)
	public int getFogColor() {
		return this.fogColor;
	}

	@Environment(EnvType.CLIENT)
	public int getWaterColor() {
		return this.waterColor;
	}

	@Environment(EnvType.CLIENT)
	public int getWaterFogColor() {
		return this.waterFogColor;
	}

	@Environment(EnvType.CLIENT)
	public Optional<AmbientParticleSettings> getAmbientParticleSettings() {
		return this.ambientParticleSettings;
	}

	@Environment(EnvType.CLIENT)
	public Optional<SoundEvent> getAmbientLoopSoundEvent() {
		return this.ambientLoopSoundEvent;
	}

	@Environment(EnvType.CLIENT)
	public Optional<AmbientMoodSettings> getAmbientMoodSettings() {
		return this.ambientMoodSettings;
	}

	@Environment(EnvType.CLIENT)
	public Optional<AmbientAdditionsSettings> getAmbientAdditionsSettings() {
		return this.ambientAdditionsSettings;
	}

	@Environment(EnvType.CLIENT)
	public Optional<Music> getBackgroundMusic() {
		return this.backgroundMusic;
	}

	public static class Builder {
		private OptionalInt fogColor = OptionalInt.empty();
		private OptionalInt waterColor = OptionalInt.empty();
		private OptionalInt waterFogColor = OptionalInt.empty();
		private Optional<AmbientParticleSettings> ambientParticle = Optional.empty();
		private Optional<SoundEvent> ambientLoopSoundEvent = Optional.empty();
		private Optional<AmbientMoodSettings> ambientMoodSettings = Optional.empty();
		private Optional<AmbientAdditionsSettings> ambientAdditionsSettings = Optional.empty();
		private Optional<Music> backgroundMusic = Optional.empty();

		public BiomeSpecialEffects.Builder fogColor(int i) {
			this.fogColor = OptionalInt.of(i);
			return this;
		}

		public BiomeSpecialEffects.Builder waterColor(int i) {
			this.waterColor = OptionalInt.of(i);
			return this;
		}

		public BiomeSpecialEffects.Builder waterFogColor(int i) {
			this.waterFogColor = OptionalInt.of(i);
			return this;
		}

		public BiomeSpecialEffects.Builder ambientParticle(AmbientParticleSettings ambientParticleSettings) {
			this.ambientParticle = Optional.of(ambientParticleSettings);
			return this;
		}

		public BiomeSpecialEffects.Builder ambientLoopSound(SoundEvent soundEvent) {
			this.ambientLoopSoundEvent = Optional.of(soundEvent);
			return this;
		}

		public BiomeSpecialEffects.Builder ambientMoodSound(AmbientMoodSettings ambientMoodSettings) {
			this.ambientMoodSettings = Optional.of(ambientMoodSettings);
			return this;
		}

		public BiomeSpecialEffects.Builder ambientAdditionsSound(AmbientAdditionsSettings ambientAdditionsSettings) {
			this.ambientAdditionsSettings = Optional.of(ambientAdditionsSettings);
			return this;
		}

		public BiomeSpecialEffects.Builder backgroundMusic(Music music) {
			this.backgroundMusic = Optional.of(music);
			return this;
		}

		public BiomeSpecialEffects build() {
			return new BiomeSpecialEffects(
				this.fogColor.orElseThrow(() -> new IllegalStateException("Missing 'fog' color.")),
				this.waterColor.orElseThrow(() -> new IllegalStateException("Missing 'water' color.")),
				this.waterFogColor.orElseThrow(() -> new IllegalStateException("Missing 'water fog' color.")),
				this.ambientParticle,
				this.ambientLoopSoundEvent,
				this.ambientMoodSettings,
				this.ambientAdditionsSettings,
				this.backgroundMusic
			);
		}
	}
}
