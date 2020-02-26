/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class ElytraOnPlayerSoundInstance
extends AbstractTickableSoundInstance {
    private final LocalPlayer player;
    private int time;

    public ElytraOnPlayerSoundInstance(LocalPlayer localPlayer) {
        super(SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS);
        this.player = localPlayer;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.1f;
    }

    @Override
    public void tick() {
        ++this.time;
        if (this.player.removed || this.time > 20 && !this.player.isFallFlying()) {
            this.stop();
            return;
        }
        this.x = (float)this.player.getX();
        this.y = (float)this.player.getY();
        this.z = (float)this.player.getZ();
        float f = (float)this.player.getDeltaMovement().lengthSqr();
        this.volume = (double)f >= 1.0E-7 ? Mth.clamp(f / 4.0f, 0.0f, 1.0f) : 0.0f;
        if (this.time < 20) {
            this.volume = 0.0f;
        } else if (this.time < 40) {
            this.volume = (float)((double)this.volume * ((double)(this.time - 20) / 20.0));
        }
        float g = 0.8f;
        this.pitch = this.volume > 0.8f ? 1.0f + (this.volume - 0.8f) : 1.0f;
    }
}

