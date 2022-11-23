package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;

public record Instrument(SoundEvent soundEvent, int useDuration, float range) {
	public static final Codec<Instrument> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("sound_event").forGetter(Instrument::soundEvent),
					ExtraCodecs.POSITIVE_INT.fieldOf("use_duration").forGetter(Instrument::useDuration),
					ExtraCodecs.POSITIVE_FLOAT.fieldOf("range").forGetter(Instrument::range)
				)
				.apply(instance, Instrument::new)
	);
}
