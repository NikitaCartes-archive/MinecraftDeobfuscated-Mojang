package net.minecraft.voting.votes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;

public record OptionVotes(Map<UUID, Voter> voters) {
	public static final OptionVotes EMPTY = new OptionVotes(Map.of());
	public static final Codec<OptionVotes> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(Codec.unboundedMap(UUIDUtil.STRING_CODEC, Voter.CODEC).fieldOf("voters").forGetter(OptionVotes::voters))
				.apply(instance, OptionVotes::new)
	);
	public static final FriendlyByteBuf.Reader<OptionVotes> READER = friendlyByteBuf -> new OptionVotes(
			friendlyByteBuf.readMap(FriendlyByteBuf::readUUID, Voter.READER)
		);
	public static final FriendlyByteBuf.Writer<OptionVotes> WRITER = (friendlyByteBuf, optionVotes) -> friendlyByteBuf.writeMap(
			optionVotes.voters, FriendlyByteBuf::writeUUID, Voter.WRITER
		);

	public int countVoters() {
		return this.voters.size();
	}

	public int countVotes() {
		return this.voters.values().stream().mapToInt(Voter::voteCount).sum();
	}

	public static OptionVotes singlePlayer(UUID uUID, Voter voter) {
		return new OptionVotes(Map.of(uUID, voter));
	}
}
