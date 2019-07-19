/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class EntityBoundSoundInstance
extends AbstractTickableSoundInstance {
    private final Entity entity;

    public EntityBoundSoundInstance(SoundEvent soundEvent, SoundSource soundSource, Entity entity) {
        this(soundEvent, soundSource, 1.0f, 1.0f, entity);
    }

    public EntityBoundSoundInstance(SoundEvent soundEvent, SoundSource soundSource, float f, float g, Entity entity) {
        super(soundEvent, soundSource);
        this.volume = f;
        this.pitch = g;
        this.entity = entity;
        this.x = (float)this.entity.x;
        this.y = (float)this.entity.y;
        this.z = (float)this.entity.z;
    }

    @Override
    public void tick() {
        if (this.entity.removed) {
            this.stopped = true;
            return;
        }
        this.x = (float)this.entity.x;
        this.y = (float)this.entity.y;
        this.z = (float)this.entity.z;
    }
}

