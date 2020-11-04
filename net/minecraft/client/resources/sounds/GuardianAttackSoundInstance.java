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
import net.minecraft.world.entity.monster.Guardian;

@Environment(value=EnvType.CLIENT)
public class GuardianAttackSoundInstance
extends AbstractTickableSoundInstance {
    private final Guardian guardian;

    public GuardianAttackSoundInstance(Guardian guardian) {
        super(SoundEvents.GUARDIAN_ATTACK, SoundSource.HOSTILE);
        this.guardian = guardian;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.looping = true;
        this.delay = 0;
    }

    @Override
    public boolean canPlaySound() {
        return !this.guardian.isSilent();
    }

    @Override
    public void tick() {
        if (this.guardian.isRemoved() || this.guardian.getTarget() != null) {
            this.stop();
            return;
        }
        this.x = (float)this.guardian.getX();
        this.y = (float)this.guardian.getY();
        this.z = (float)this.guardian.getZ();
        float f = this.guardian.getAttackAnimationScale(0.0f);
        this.volume = 0.0f + 1.0f * f * f;
        this.pitch = 0.7f + 0.5f * f;
    }
}

