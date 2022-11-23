/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;

public record Instrument(SoundEvent soundEvent, int useDuration, float range) {
    public static final Codec<Instrument> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("sound_event")).forGetter(Instrument::soundEvent), ((MapCodec)ExtraCodecs.POSITIVE_INT.fieldOf("use_duration")).forGetter(Instrument::useDuration), ((MapCodec)ExtraCodecs.POSITIVE_FLOAT.fieldOf("range")).forGetter(Instrument::range)).apply((Applicative<Instrument, ?>)instance, Instrument::new));
}

