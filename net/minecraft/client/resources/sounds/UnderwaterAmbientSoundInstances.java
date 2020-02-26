/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

@Environment(value=EnvType.CLIENT)
public class UnderwaterAmbientSoundInstances {

    @Environment(value=EnvType.CLIENT)
    public static class UnderwaterAmbientSoundInstance
    extends AbstractTickableSoundInstance {
        private final LocalPlayer player;
        private int fade;

        public UnderwaterAmbientSoundInstance(LocalPlayer localPlayer) {
            super(SoundEvents.AMBIENT_UNDERWATER_LOOP, SoundSource.AMBIENT);
            this.player = localPlayer;
            this.looping = true;
            this.delay = 0;
            this.volume = 1.0f;
            this.priority = true;
            this.relative = true;
        }

        @Override
        public void tick() {
            if (this.player.removed || this.fade < 0) {
                this.stop();
                return;
            }
            this.fade = this.player.isUnderWater() ? ++this.fade : (this.fade -= 2);
            this.fade = Math.min(this.fade, 40);
            this.volume = Math.max(0.0f, Math.min((float)this.fade / 40.0f, 1.0f));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class SubSound
    extends AbstractTickableSoundInstance {
        private final LocalPlayer player;

        protected SubSound(LocalPlayer localPlayer, SoundEvent soundEvent) {
            super(soundEvent, SoundSource.AMBIENT);
            this.player = localPlayer;
            this.looping = false;
            this.delay = 0;
            this.volume = 1.0f;
            this.priority = true;
            this.relative = true;
        }

        @Override
        public void tick() {
            if (this.player.removed || !this.player.isUnderWater()) {
                this.stop();
            }
        }
    }
}

