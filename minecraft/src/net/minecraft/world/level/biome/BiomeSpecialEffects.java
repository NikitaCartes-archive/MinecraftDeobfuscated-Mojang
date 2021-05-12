package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.StringRepresentable;

public class BiomeSpecialEffects {
	public static final Codec<BiomeSpecialEffects> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.INT.fieldOf("fog_color").forGetter(biomeSpecialEffects -> biomeSpecialEffects.fogColor),
					Codec.INT.fieldOf("water_color").forGetter(biomeSpecialEffects -> biomeSpecialEffects.waterColor),
					Codec.INT.fieldOf("water_fog_color").forGetter(biomeSpecialEffects -> biomeSpecialEffects.waterFogColor),
					Codec.INT.fieldOf("sky_color").forGetter(biomeSpecialEffects -> biomeSpecialEffects.skyColor),
					Codec.INT.optionalFieldOf("foliage_color").forGetter(biomeSpecialEffects -> biomeSpecialEffects.foliageColorOverride),
					Codec.INT.optionalFieldOf("grass_color").forGetter(biomeSpecialEffects -> biomeSpecialEffects.grassColorOverride),
					BiomeSpecialEffects.GrassColorModifier.CODEC
						.optionalFieldOf("grass_color_modifier", BiomeSpecialEffects.GrassColorModifier.NONE)
						.forGetter(biomeSpecialEffects -> biomeSpecialEffects.grassColorModifier),
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
	private final int skyColor;
	private final Optional<Integer> foliageColorOverride;
	private final Optional<Integer> grassColorOverride;
	private final BiomeSpecialEffects.GrassColorModifier grassColorModifier;
	private final Optional<AmbientParticleSettings> ambientParticleSettings;
	private final Optional<SoundEvent> ambientLoopSoundEvent;
	private final Optional<AmbientMoodSettings> ambientMoodSettings;
	private final Optional<AmbientAdditionsSettings> ambientAdditionsSettings;
	private final Optional<Music> backgroundMusic;

	BiomeSpecialEffects(
		int i,
		int j,
		int k,
		int l,
		Optional<Integer> optional,
		Optional<Integer> optional2,
		BiomeSpecialEffects.GrassColorModifier grassColorModifier,
		Optional<AmbientParticleSettings> optional3,
		Optional<SoundEvent> optional4,
		Optional<AmbientMoodSettings> optional5,
		Optional<AmbientAdditionsSettings> optional6,
		Optional<Music> optional7
	) {
		this.fogColor = i;
		this.waterColor = j;
		this.waterFogColor = k;
		this.skyColor = l;
		this.foliageColorOverride = optional;
		this.grassColorOverride = optional2;
		this.grassColorModifier = grassColorModifier;
		this.ambientParticleSettings = optional3;
		this.ambientLoopSoundEvent = optional4;
		this.ambientMoodSettings = optional5;
		this.ambientAdditionsSettings = optional6;
		this.backgroundMusic = optional7;
	}

	public int getFogColor() {
		return this.fogColor;
	}

	public int getWaterColor() {
		return this.waterColor;
	}

	public int getWaterFogColor() {
		return this.waterFogColor;
	}

	public int getSkyColor() {
		return this.skyColor;
	}

	public Optional<Integer> getFoliageColorOverride() {
		return this.foliageColorOverride;
	}

	public Optional<Integer> getGrassColorOverride() {
		return this.grassColorOverride;
	}

	public BiomeSpecialEffects.GrassColorModifier getGrassColorModifier() {
		return this.grassColorModifier;
	}

	public Optional<AmbientParticleSettings> getAmbientParticleSettings() {
		return this.ambientParticleSettings;
	}

	public Optional<SoundEvent> getAmbientLoopSoundEvent() {
		return this.ambientLoopSoundEvent;
	}

	public Optional<AmbientMoodSettings> getAmbientMoodSettings() {
		return this.ambientMoodSettings;
	}

	public Optional<AmbientAdditionsSettings> getAmbientAdditionsSettings() {
		return this.ambientAdditionsSettings;
	}

	public Optional<Music> getBackgroundMusic() {
		return this.backgroundMusic;
	}

	public static class Builder {
		private OptionalInt fogColor = OptionalInt.empty();
		private OptionalInt waterColor = OptionalInt.empty();
		private OptionalInt waterFogColor = OptionalInt.empty();
		private OptionalInt skyColor = OptionalInt.empty();
		private Optional<Integer> foliageColorOverride = Optional.empty();
		private Optional<Integer> grassColorOverride = Optional.empty();
		private BiomeSpecialEffects.GrassColorModifier grassColorModifier = BiomeSpecialEffects.GrassColorModifier.NONE;
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

		public BiomeSpecialEffects.Builder skyColor(int i) {
			this.skyColor = OptionalInt.of(i);
			return this;
		}

		public BiomeSpecialEffects.Builder foliageColorOverride(int i) {
			this.foliageColorOverride = Optional.of(i);
			return this;
		}

		public BiomeSpecialEffects.Builder grassColorOverride(int i) {
			this.grassColorOverride = Optional.of(i);
			return this;
		}

		public BiomeSpecialEffects.Builder grassColorModifier(BiomeSpecialEffects.GrassColorModifier grassColorModifier) {
			this.grassColorModifier = grassColorModifier;
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
				this.skyColor.orElseThrow(() -> new IllegalStateException("Missing 'sky' color.")),
				this.foliageColorOverride,
				this.grassColorOverride,
				this.grassColorModifier,
				this.ambientParticle,
				this.ambientLoopSoundEvent,
				this.ambientMoodSettings,
				this.ambientAdditionsSettings,
				this.backgroundMusic
			);
		}
	}

	public static enum GrassColorModifier implements StringRepresentable {
		NONE("none") {
			@Override
			public int modifyColor(double d, double e, int i) {
				return i;
			}
		},
		DARK_FOREST("dark_forest") {
			@Override
			public int modifyColor(double d, double e, int i) {
				return (i & 16711422) + 2634762 >> 1;
			}
		},
		SWAMP("swamp") {
			@Override
			public int modifyColor(double d, double e, int i) {
				double f = Biome.BIOME_INFO_NOISE.getValue(d * 0.0225, e * 0.0225, false);
				return f < -0.1 ? 5011004 : 6975545;
			}
		};

		private final String name;
		public static final Codec<BiomeSpecialEffects.GrassColorModifier> CODEC = StringRepresentable.fromEnum(
			BiomeSpecialEffects.GrassColorModifier::values, BiomeSpecialEffects.GrassColorModifier::byName
		);
		private static final Map<String, BiomeSpecialEffects.GrassColorModifier> BY_NAME = (Map<String, BiomeSpecialEffects.GrassColorModifier>)Arrays.stream(
				values()
			)
			.collect(Collectors.toMap(BiomeSpecialEffects.GrassColorModifier::getName, grassColorModifier -> grassColorModifier));

		public abstract int modifyColor(double d, double e, int i);

		GrassColorModifier(String string2) {
			this.name = string2;
		}

		public String getName() {
			return this.name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static BiomeSpecialEffects.GrassColorModifier byName(String string) {
			return (BiomeSpecialEffects.GrassColorModifier)BY_NAME.get(string);
		}
	}
}
