/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import java.util.Optional;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.biome.AmbientParticleSettings;

public class BiomeSpecialEffects {
    private final int fogColor;
    private final int waterColor;
    private final int waterFogColor;
    private final Optional<AmbientParticleSettings> ambientParticleSettings;
    private final Optional<SoundEvent> ambientLoopSoundEvent;
    private final Optional<SoundEvent> ambientMoodSoundEvent;
    private final Optional<SoundEvent> ambientAdditionsSoundEvent;

    private BiomeSpecialEffects(int i, int j, int k, Optional<AmbientParticleSettings> optional, Optional<SoundEvent> optional2, Optional<SoundEvent> optional3, Optional<SoundEvent> optional4) {
        this.fogColor = i;
        this.waterColor = j;
        this.waterFogColor = k;
        this.ambientParticleSettings = optional;
        this.ambientLoopSoundEvent = optional2;
        this.ambientMoodSoundEvent = optional3;
        this.ambientAdditionsSoundEvent = optional4;
    }

    @Environment(value=EnvType.CLIENT)
    public int getFogColor() {
        return this.fogColor;
    }

    @Environment(value=EnvType.CLIENT)
    public int getWaterColor() {
        return this.waterColor;
    }

    @Environment(value=EnvType.CLIENT)
    public int getWaterFogColor() {
        return this.waterFogColor;
    }

    @Environment(value=EnvType.CLIENT)
    public Optional<AmbientParticleSettings> getAmbientParticleSettings() {
        return this.ambientParticleSettings;
    }

    @Environment(value=EnvType.CLIENT)
    public Optional<SoundEvent> getAmbientLoopSoundEvent() {
        return this.ambientLoopSoundEvent;
    }

    @Environment(value=EnvType.CLIENT)
    public Optional<SoundEvent> getAmbientMoodSoundEvent() {
        return this.ambientMoodSoundEvent;
    }

    @Environment(value=EnvType.CLIENT)
    public Optional<SoundEvent> getAmbientAdditionsSoundEvent() {
        return this.ambientAdditionsSoundEvent;
    }

    public static class Builder {
        private OptionalInt fogColor = OptionalInt.empty();
        private OptionalInt waterColor = OptionalInt.empty();
        private OptionalInt waterFogColor = OptionalInt.empty();
        private Optional<AmbientParticleSettings> ambientParticle = Optional.empty();
        private Optional<SoundEvent> ambientLoopSoundEvent = Optional.empty();
        private Optional<SoundEvent> ambientMoodSoundEvent = Optional.empty();
        private Optional<SoundEvent> ambientAdditionsSoundEvent = Optional.empty();

        public Builder fogColor(int i) {
            this.fogColor = OptionalInt.of(i);
            return this;
        }

        public Builder waterColor(int i) {
            this.waterColor = OptionalInt.of(i);
            return this;
        }

        public Builder waterFogColor(int i) {
            this.waterFogColor = OptionalInt.of(i);
            return this;
        }

        public Builder ambientParticle(AmbientParticleSettings ambientParticleSettings) {
            this.ambientParticle = Optional.of(ambientParticleSettings);
            return this;
        }

        public Builder ambientLoopSound(SoundEvent soundEvent) {
            this.ambientLoopSoundEvent = Optional.of(soundEvent);
            return this;
        }

        public Builder ambientMoodSound(SoundEvent soundEvent) {
            this.ambientMoodSoundEvent = Optional.of(soundEvent);
            return this;
        }

        public Builder ambientAdditionsSound(SoundEvent soundEvent) {
            this.ambientAdditionsSoundEvent = Optional.of(soundEvent);
            return this;
        }

        public BiomeSpecialEffects build() {
            return new BiomeSpecialEffects(this.fogColor.orElseThrow(() -> new IllegalStateException("Missing 'fog' color.")), this.waterColor.orElseThrow(() -> new IllegalStateException("Missing 'water' color.")), this.waterFogColor.orElseThrow(() -> new IllegalStateException("Missing 'water fog' color.")), this.ambientParticle, this.ambientLoopSoundEvent, this.ambientMoodSoundEvent, this.ambientAdditionsSoundEvent);
        }
    }
}

