/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class EntityBoundSoundInstance
extends AbstractTickableSoundInstance {
    private final Entity entity;

    public EntityBoundSoundInstance(SoundEvent soundEvent, SoundSource soundSource, float f, float g, Entity entity, long l) {
        super(soundEvent, soundSource, RandomSource.create(l));
        this.volume = f;
        this.pitch = g;
        this.entity = entity;
        this.x = (float)this.entity.getX();
        this.y = (float)this.entity.getY();
        this.z = (float)this.entity.getZ();
    }

    @Override
    public boolean canPlaySound() {
        return !this.entity.isSilent();
    }

    @Override
    public void tick() {
        if (this.entity.isRemoved()) {
            this.stop();
            return;
        }
        this.x = (float)this.entity.getX();
        this.y = (float)this.entity.getY();
        this.z = (float)this.entity.getZ();
    }
}

