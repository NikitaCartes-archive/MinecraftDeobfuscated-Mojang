package net.minecraft.voting.votes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

public record OptionVoteStorage(Map<OptionId, OptionVotes> options) {
	public static final Codec<OptionVoteStorage> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(Codec.unboundedMap(OptionId.STRING_CODEC, OptionVotes.CODEC).fieldOf("options").forGetter(OptionVoteStorage::options))
				.apply(instance, OptionVoteStorage::new)
	);

	public VoteResults getVotingResults() {
		Map<UUID, Voter> map = new HashMap();
		List<VoteResults.OptionResult> list = new ArrayList();

		for (Entry<OptionId, OptionVotes> entry : this.options.entrySet()) {
			OptionVotes optionVotes = (OptionVotes)entry.getValue();
			list.add(new VoteResults.OptionResult((OptionId)entry.getKey(), new VoteResults.VoteCounts(optionVotes)));
			optionVotes.voters().forEach((uUID, voter) -> map.compute(uUID, (uUIDx, voter2) -> Voter.update(voter2, voter.displayName(), voter.voteCount())));
		}

		return new VoteResults(new VoteResults.VoteCounts(new OptionVotes(map)), List.copyOf(list));
	}
}
