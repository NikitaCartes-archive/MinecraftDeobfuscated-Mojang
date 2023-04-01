package net.minecraft.voting.votes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

public record ClientVote(CommonVoteData header, Map<OptionId, ClientVote.ClientOption> options) {
	public static final Codec<ClientVote> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					CommonVoteData.CODEC.forGetter(ClientVote::header),
					Codec.unboundedMap(OptionId.STRING_CODEC, ClientVote.ClientOption.CODEC).fieldOf("options").forGetter(ClientVote::options)
				)
				.apply(instance, ClientVote::new)
	);

	public static record ClientOption(Component displayName, boolean irregular) {
		public static final Codec<ClientVote.ClientOption> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.COMPONENT.fieldOf("display_name").forGetter(ClientVote.ClientOption::displayName),
						Codec.BOOL.fieldOf("irregular").forGetter(ClientVote.ClientOption::irregular)
					)
					.apply(instance, ClientVote.ClientOption::new)
		);
	}
}
