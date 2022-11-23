/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public class AmbientAdditionsSettings {
    public static final Codec<AmbientAdditionsSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("sound")).forGetter(ambientAdditionsSettings -> ambientAdditionsSettings.soundEvent), ((MapCodec)Codec.DOUBLE.fieldOf("tick_chance")).forGetter(ambientAdditionsSettings -> ambientAdditionsSettings.tickChance)).apply((Applicative<AmbientAdditionsSettings, ?>)instance, AmbientAdditionsSettings::new));
    private final SoundEvent soundEvent;
    private final double tickChance;

    public AmbientAdditionsSettings(SoundEvent soundEvent, double d) {
        this.soundEvent = soundEvent;
        this.tickChance = d;
    }

    public SoundEvent getSoundEvent() {
        return this.soundEvent;
    }

    public double getTickChance() {
        return this.tickChance;
    }
}

