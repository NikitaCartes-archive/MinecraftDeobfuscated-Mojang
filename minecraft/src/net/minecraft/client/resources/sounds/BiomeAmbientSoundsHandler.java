package net.minecraft.client.resources.sounds;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Optional;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;

@Environment(EnvType.CLIENT)
public class BiomeAmbientSoundsHandler implements AmbientSoundHandler {
	private static final int LOOP_SOUND_CROSS_FADE_TIME = 40;
	private static final float SKY_MOOD_RECOVERY_RATE = 0.001F;
	private final LocalPlayer player;
	private final SoundManager soundManager;
	private final BiomeManager biomeManager;
	private final Random random;
	private final Object2ObjectArrayMap<Biome, BiomeAmbientSoundsHandler.LoopSoundInstance> loopSounds = new Object2ObjectArrayMap<>();
	private Optional<AmbientMoodSettings> moodSettings = Optional.empty();
	private Optional<AmbientAdditionsSettings> additionsSettings = Optional.empty();
	private float moodiness;
	private Biome previousBiome;

	public BiomeAmbientSoundsHandler(LocalPlayer localPlayer, SoundManager soundManager, BiomeManager biomeManager) {
		this.random = localPlayer.level.getRandom();
		this.player = localPlayer;
		this.soundManager = soundManager;
		this.biomeManager = biomeManager;
	}

	public float getMoodiness() {
		return this.moodiness;
	}

	@Override
	public void tick() {
		this.loopSounds.values().removeIf(AbstractTickableSoundInstance::isStopped);
		Biome biome = this.biomeManager.getNoiseBiomeAtPosition(this.player.getX(), this.player.getY(), this.player.getZ());
		if (biome != this.previousBiome) {
			this.previousBiome = biome;
			this.moodSettings = biome.getAmbientMood();
			this.additionsSettings = biome.getAmbientAdditions();
			this.loopSounds.values().forEach(BiomeAmbientSoundsHandler.LoopSoundInstance::fadeOut);
			biome.getAmbientLoop().ifPresent(soundEvent -> this.loopSounds.compute(biome, (biomexx, loopSoundInstance) -> {
					if (loopSoundInstance == null) {
						loopSoundInstance = new BiomeAmbientSoundsHandler.LoopSoundInstance(soundEvent);
						this.soundManager.play(loopSoundInstance);
					}

					loopSoundInstance.fadeIn();
					return loopSoundInstance;
				}));
		}

		this.additionsSettings.ifPresent(ambientAdditionsSettings -> {
			if (this.random.nextDouble() < ambientAdditionsSettings.getTickChance()) {
				this.soundManager.play(SimpleSoundInstance.forAmbientAddition(ambientAdditionsSettings.getSoundEvent()));
			}
		});
		this.moodSettings
			.ifPresent(
				ambientMoodSettings -> {
					Level level = this.player.level;
					int i = ambientMoodSettings.getBlockSearchExtent() * 2 + 1;
					BlockPos blockPos = new BlockPos(
						this.player.getX() + (double)this.random.nextInt(i) - (double)ambientMoodSettings.getBlockSearchExtent(),
						this.player.getEyeY() + (double)this.random.nextInt(i) - (double)ambientMoodSettings.getBlockSearchExtent(),
						this.player.getZ() + (double)this.random.nextInt(i) - (double)ambientMoodSettings.getBlockSearchExtent()
					);
					int j = level.getBrightness(LightLayer.SKY, blockPos);
					if (j > 0) {
						this.moodiness = this.moodiness - (float)j / (float)level.getMaxLightLevel() * 0.001F;
					} else {
						this.moodiness = this.moodiness - (float)(level.getBrightness(LightLayer.BLOCK, blockPos) - 1) / (float)ambientMoodSettings.getTickDelay();
					}

					if (this.moodiness >= 1.0F) {
						double d = (double)blockPos.getX() + 0.5;
						double e = (double)blockPos.getY() + 0.5;
						double f = (double)blockPos.getZ() + 0.5;
						double g = d - this.player.getX();
						double h = e - this.player.getEyeY();
						double k = f - this.player.getZ();
						double l = Math.sqrt(g * g + h * h + k * k);
						double m = l + ambientMoodSettings.getSoundPositionOffset();
						SimpleSoundInstance simpleSoundInstance = SimpleSoundInstance.forAmbientMood(
							ambientMoodSettings.getSoundEvent(), this.player.getX() + g / l * m, this.player.getEyeY() + h / l * m, this.player.getZ() + k / l * m
						);
						this.soundManager.play(simpleSoundInstance);
						this.moodiness = 0.0F;
					} else {
						this.moodiness = Math.max(this.moodiness, 0.0F);
					}
				}
			);
	}

	@Environment(EnvType.CLIENT)
	public static class LoopSoundInstance extends AbstractTickableSoundInstance {
		private int fadeDirection;
		private int fade;

		public LoopSoundInstance(SoundEvent soundEvent) {
			super(soundEvent, SoundSource.AMBIENT);
			this.looping = true;
			this.delay = 0;
			this.volume = 1.0F;
			this.relative = true;
		}

		@Override
		public void tick() {
			if (this.fade < 0) {
				this.stop();
			}

			this.fade = this.fade + this.fadeDirection;
			this.volume = Mth.clamp((float)this.fade / 40.0F, 0.0F, 1.0F);
		}

		public void fadeOut() {
			this.fade = Math.min(this.fade, 40);
			this.fadeDirection = -1;
		}

		public void fadeIn() {
			this.fade = Math.max(0, this.fade);
			this.fadeDirection = 1;
		}
	}
}
