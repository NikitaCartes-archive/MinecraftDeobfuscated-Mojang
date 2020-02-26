/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

@Environment(value=EnvType.CLIENT)
public class MinecartSoundInstance
extends AbstractTickableSoundInstance {
    private final AbstractMinecart minecart;
    private float pitch = 0.0f;

    public MinecartSoundInstance(AbstractMinecart abstractMinecart) {
        super(SoundEvents.MINECART_RIDING, SoundSource.NEUTRAL);
        this.minecart = abstractMinecart;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0f;
        this.x = (float)abstractMinecart.getX();
        this.y = (float)abstractMinecart.getY();
        this.z = (float)abstractMinecart.getZ();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (this.minecart.removed) {
            this.stop();
            return;
        }
        this.x = (float)this.minecart.getX();
        this.y = (float)this.minecart.getY();
        this.z = (float)this.minecart.getZ();
        float f = Mth.sqrt(Entity.getHorizontalDistanceSqr(this.minecart.getDeltaMovement()));
        if ((double)f >= 0.01) {
            this.pitch = Mth.clamp(this.pitch + 0.0025f, 0.0f, 1.0f);
            this.volume = Mth.lerp(Mth.clamp(f, 0.0f, 0.5f), 0.0f, 0.7f);
        } else {
            this.pitch = 0.0f;
            this.volume = 0.0f;
        }
    }
}

