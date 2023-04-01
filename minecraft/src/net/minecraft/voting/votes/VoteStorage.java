package net.minecraft.voting.votes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.voting.rules.Rule;
import net.minecraft.voting.rules.RuleChange;

public record VoteStorage(List<RuleChange> approved, Map<UUID, ServerVote> pending, OptionVoteStorage votes, int totalProposalCount) {
	public static final Codec<VoteStorage> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					RuleChange.CODEC.listOf().fieldOf("approved").forGetter(VoteStorage::approved),
					Codec.unboundedMap(UUIDUtil.STRING_CODEC, ServerVote.CODEC).fieldOf("pending").forGetter(VoteStorage::pending),
					OptionVoteStorage.CODEC.fieldOf("votes").forGetter(VoteStorage::votes),
					Codec.INT.fieldOf("total_proposal_count").forGetter(VoteStorage::totalProposalCount)
				)
				.apply(instance, VoteStorage::new)
	);

	public VoteStorage() {
		this(List.of(), Map.of(), new OptionVoteStorage(Map.of()), 0);
	}

	public static VoteStorage fromApprovedRules(Stream<Holder.Reference<Rule>> stream, Map<UUID, ServerVote> map, OptionVoteStorage optionVoteStorage, int i) {
		List<RuleChange> list = (List<RuleChange>)stream.flatMap(reference -> ((Rule)reference.value()).approvedChanges()).collect(Collectors.toList());
		return new VoteStorage(list, map, optionVoteStorage, i);
	}
}
