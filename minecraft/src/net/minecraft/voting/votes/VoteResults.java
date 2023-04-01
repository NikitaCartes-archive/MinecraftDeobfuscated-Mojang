package net.minecraft.voting.votes;

import java.util.Comparator;
import java.util.List;

public record VoteResults(VoteResults.VoteCounts total, List<VoteResults.OptionResult> options) {
	public static record OptionResult(OptionId optionId, VoteResults.VoteCounts counts) {
		public static final Comparator<VoteResults.OptionResult> BY_VOTES = Comparator.comparing(VoteResults.OptionResult::counts, VoteResults.VoteCounts.BY_VOTES);
	}

	public static record VoteCounts(int votesCount, int votersCount, OptionVotes votes) {
		public static final VoteResults.VoteCounts EMPTY = new VoteResults.VoteCounts(0, 0, OptionVotes.EMPTY);
		public static final Comparator<VoteResults.VoteCounts> BY_VOTES = Comparator.comparing(VoteResults.VoteCounts::votesCount);

		public VoteCounts(OptionVotes optionVotes) {
			this(optionVotes.countVotes(), optionVotes.countVoters(), optionVotes);
		}
	}
}
