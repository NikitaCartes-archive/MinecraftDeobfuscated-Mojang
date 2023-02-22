/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.sniffer.Sniffer;

@Environment(value=EnvType.CLIENT)
public class SnifferSoundInstance
extends AbstractTickableSoundInstance {
    private static final float VOLUME = 1.0f;
    private static final float PITCH = 1.0f;
    private final Sniffer sniffer;

    public SnifferSoundInstance(Sniffer sniffer) {
        super(SoundEvents.SNIFFER_DIGGING, SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
        this.sniffer = sniffer;
        this.attenuation = SoundInstance.Attenuation.LINEAR;
        this.looping = false;
        this.delay = 0;
    }

    @Override
    public boolean canPlaySound() {
        return !this.sniffer.isSilent();
    }

    @Override
    public void tick() {
        if (this.sniffer.isRemoved() || this.sniffer.getTarget() != null || !this.sniffer.canPlayDiggingSound()) {
            this.stop();
            return;
        }
        this.x = (float)this.sniffer.getX();
        this.y = (float)this.sniffer.getY();
        this.z = (float)this.sniffer.getZ();
        this.volume = 1.0f;
        this.pitch = 1.0f;
    }
}

