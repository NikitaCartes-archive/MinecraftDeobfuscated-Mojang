package net.minecraft.voting.votes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

public record Voter(Component displayName, int voteCount) {
	public static final Comparator<Voter> BY_VOTES = Comparator.comparing(Voter::voteCount);
	public static final Codec<Voter> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.COMPONENT.fieldOf("display_name").forGetter(Voter::displayName), Codec.INT.fieldOf("vote_count").forGetter(Voter::voteCount)
				)
				.apply(instance, Voter::new)
	);
	public static final FriendlyByteBuf.Reader<Voter> READER = friendlyByteBuf -> {
		Component component = friendlyByteBuf.readComponent();
		int i = friendlyByteBuf.readVarInt();
		return new Voter(component, i);
	};
	public static final FriendlyByteBuf.Writer<Voter> WRITER = (friendlyByteBuf, voter) -> {
		friendlyByteBuf.writeComponent(voter.displayName);
		friendlyByteBuf.writeVarInt(voter.voteCount);
	};

	public static Voter update(@Nullable Voter voter, Component component, int i) {
		int j = voter != null ? voter.voteCount : 0;
		return new Voter(component, j + i);
	}
}
