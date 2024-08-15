package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;

public record Instrument(Holder<SoundEvent> soundEvent, float useDuration, float range, Component description) {
	public static final Codec<Instrument> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					SoundEvent.CODEC.fieldOf("sound_event").forGetter(Instrument::soundEvent),
					ExtraCodecs.POSITIVE_FLOAT.fieldOf("use_duration").forGetter(Instrument::useDuration),
					ExtraCodecs.POSITIVE_FLOAT.fieldOf("range").forGetter(Instrument::range),
					ComponentSerialization.CODEC.fieldOf("description").forGetter(Instrument::description)
				)
				.apply(instance, Instrument::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, Instrument> DIRECT_STREAM_CODEC = StreamCodec.composite(
		SoundEvent.STREAM_CODEC,
		Instrument::soundEvent,
		ByteBufCodecs.FLOAT,
		Instrument::useDuration,
		ByteBufCodecs.FLOAT,
		Instrument::range,
		ComponentSerialization.STREAM_CODEC,
		Instrument::description,
		Instrument::new
	);
	public static final Codec<Holder<Instrument>> CODEC = RegistryFileCodec.create(Registries.INSTRUMENT, DIRECT_CODEC);
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Instrument>> STREAM_CODEC = ByteBufCodecs.holder(Registries.INSTRUMENT, DIRECT_STREAM_CODEC);
}
