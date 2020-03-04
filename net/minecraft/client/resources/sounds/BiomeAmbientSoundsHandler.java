/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.sounds;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Optional;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.AmbientSoundHandler;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BiomeAmbientSoundsHandler
implements AmbientSoundHandler {
    private final LocalPlayer player;
    private final SoundManager soundManager;
    private final BiomeManager biomeManager;
    private final Random random;
    private Object2ObjectArrayMap<Biome, LoopSoundInstance> loopSounds;
    private Optional<SoundEvent> moodSound;
    private Optional<SoundEvent> additionsSound;
    private int ticksUntilNextMoodSound;
    private Biome previousBiome;

    public BiomeAmbientSoundsHandler(LocalPlayer localPlayer, SoundManager soundManager, BiomeManager biomeManager) {
        this.random = localPlayer.level.getRandom();
        this.player = localPlayer;
        this.soundManager = soundManager;
        this.biomeManager = biomeManager;
        this.loopSounds = new Object2ObjectArrayMap();
        this.moodSound = Optional.empty();
        this.additionsSound = Optional.empty();
        this.ticksUntilNextMoodSound = BiomeAmbientSoundsHandler.calculateTicksUntilNextMoodSound(this.random);
    }

    @Override
    public void tick() {
        this.loopSounds.values().removeIf(AbstractTickableSoundInstance::isStopped);
        Biome biome = this.biomeManager.getNoiseBiomeAtPosition(this.player.getX(), this.player.getY(), this.player.getZ());
        if (biome != this.previousBiome) {
            this.previousBiome = biome;
            this.moodSound = biome.getAmbientMoodSoundEvent();
            this.additionsSound = biome.getAmbientAdditionsSoundEvent();
            this.loopSounds.values().forEach(LoopSoundInstance::fadeOut);
            biome.getAmbientLoopSoundEvent().ifPresent(soundEvent -> this.loopSounds.compute(biome, (biome, loopSoundInstance) -> {
                if (loopSoundInstance == null) {
                    loopSoundInstance = new LoopSoundInstance((SoundEvent)soundEvent);
                    this.soundManager.play((SoundInstance)loopSoundInstance);
                }
                loopSoundInstance.fadeIn();
                return loopSoundInstance;
            }));
        }
        this.additionsSound.ifPresent(soundEvent -> {
            if (this.random.nextDouble() < (double)0.0111f) {
                this.soundManager.play(SimpleSoundInstance.forAmbientAddition(soundEvent));
            }
        });
        if (this.ticksUntilNextMoodSound > 0) {
            --this.ticksUntilNextMoodSound;
        } else {
            this.moodSound.ifPresent(soundEvent -> {
                BlockPos blockPos = this.findMoodyBlock();
                if (blockPos != null) {
                    this.soundManager.play(SimpleSoundInstance.forAmbientMood(soundEvent, blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                    this.ticksUntilNextMoodSound = BiomeAmbientSoundsHandler.calculateTicksUntilNextMoodSound(this.random);
                }
            });
        }
    }

    @Nullable
    private BlockPos findMoodyBlock() {
        BlockState blockState;
        BlockPos blockPos = this.player.blockPosition();
        Level level = this.player.level;
        int i = 9;
        BlockPos blockPos2 = blockPos.offset(this.random.nextInt(9) - 4, this.random.nextInt(9) - 4, this.random.nextInt(9) - 4);
        double d = blockPos.distSqr(blockPos2);
        if (d >= 4.0 && d <= 256.0 && (blockState = level.getBlockState(blockPos2)).isAir() && level.getRawBrightness(blockPos2, 0) <= this.random.nextInt(8) && level.getBrightness(LightLayer.SKY, blockPos2) <= 0) {
            return blockPos2;
        }
        return null;
    }

    private static int calculateTicksUntilNextMoodSound(Random random) {
        return random.nextInt(12000) + 6000;
    }

    @Environment(value=EnvType.CLIENT)
    public static class LoopSoundInstance
    extends AbstractTickableSoundInstance {
        private int fadeDirection;
        private int fade;

        public LoopSoundInstance(SoundEvent soundEvent) {
            super(soundEvent, SoundSource.AMBIENT);
            this.looping = true;
            this.delay = 0;
            this.volume = 1.0f;
            this.relative = true;
        }

        @Override
        public void tick() {
            if (this.fade < 0) {
                this.stop();
            }
            this.fade += this.fadeDirection;
            this.volume = Mth.clamp((float)this.fade / 40.0f, 0.0f, 1.0f);
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

