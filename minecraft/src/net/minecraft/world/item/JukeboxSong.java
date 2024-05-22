package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

public record JukeboxSong(Holder<SoundEvent> soundEvent, Component description, float lengthInSeconds, int comparatorOutput) {
	public static final Codec<JukeboxSong> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					SoundEvent.CODEC.fieldOf("sound_event").forGetter(JukeboxSong::soundEvent),
					ComponentSerialization.CODEC.fieldOf("description").forGetter(JukeboxSong::description),
					ExtraCodecs.POSITIVE_FLOAT.fieldOf("length_in_seconds").forGetter(JukeboxSong::lengthInSeconds),
					ExtraCodecs.intRange(0, 15).fieldOf("comparator_output").forGetter(JukeboxSong::comparatorOutput)
				)
				.apply(instance, JukeboxSong::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, JukeboxSong> DIRECT_STREAM_CODEC = StreamCodec.composite(
		SoundEvent.STREAM_CODEC,
		JukeboxSong::soundEvent,
		ComponentSerialization.STREAM_CODEC,
		JukeboxSong::description,
		ByteBufCodecs.FLOAT,
		JukeboxSong::lengthInSeconds,
		ByteBufCodecs.VAR_INT,
		JukeboxSong::comparatorOutput,
		JukeboxSong::new
	);
	public static final Codec<Holder<JukeboxSong>> CODEC = RegistryFixedCodec.create(Registries.JUKEBOX_SONG);
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<JukeboxSong>> STREAM_CODEC = ByteBufCodecs.holder(Registries.JUKEBOX_SONG, DIRECT_STREAM_CODEC);
	private static final int SONG_END_PADDING_TICKS = 20;

	public int lengthInTicks() {
		return Mth.ceil(this.lengthInSeconds * 20.0F);
	}

	public boolean hasFinished(long l) {
		return l >= (long)(this.lengthInTicks() + 20);
	}

	public static Optional<Holder<JukeboxSong>> fromStack(HolderLookup.Provider provider, ItemStack itemStack) {
		JukeboxPlayable jukeboxPlayable = itemStack.get(DataComponents.JUKEBOX_PLAYABLE);
		return jukeboxPlayable != null ? jukeboxPlayable.song().unwrap(provider) : Optional.empty();
	}
}
