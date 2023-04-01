package net.minecraft.voting.votes;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public record FinishedVote(UUID id, ServerVote vote, VoteResults results) {
	private static final Comparator<Entry<UUID, Voter>> VOTER_COMPARATOR = Entry.comparingByValue(Voter.BY_VOTES.reversed()).thenComparing(Entry.comparingByKey());
	private static final Comparator<VoteResults.OptionResult> INDEX_COMPARATOR = Comparator.comparingInt(optionResult -> optionResult.optionId().index());

	private List<VoteResults.OptionResult> getAllResults() {
		Set<OptionId> set = new HashSet(this.vote.options().keySet());
		List<VoteResults.OptionResult> list = (List<VoteResults.OptionResult>)this.results
			.options()
			.stream()
			.peek(optionResult -> set.remove(optionResult.optionId()))
			.collect(Collectors.toCollection(ArrayList::new));
		set.stream().map(optionId -> new VoteResults.OptionResult(optionId, VoteResults.VoteCounts.EMPTY)).forEach(list::add);
		return list;
	}

	@Nullable
	private ServerVote.Option getOption(OptionId optionId) {
		return (ServerVote.Option)this.vote.options().get(optionId);
	}

	private static Component getOptionNameSafe(@Nullable ServerVote.Option option) {
		return (Component)(option != null ? option.displayName() : Component.literal("???"));
	}

	private Component getOptionName(OptionId optionId) {
		return getOptionNameSafe(this.getOption(optionId));
	}

	public void unpack(Consumer<ServerVote.Effect> consumer, Consumer<Component> consumer2, FinishedVote.UnpackOptions unpackOptions) {
		consumer2.accept(Component.translatable("vote.finished", this.vote.header().displayName()));
		List<VoteResults.OptionResult> list = this.getAllResults();
		this.displayVotingResults(consumer2, unpackOptions, list);
		List<VoteResults.OptionResult> list2 = this.selectResults(consumer2, unpackOptions, list);
		if (list2.isEmpty()) {
			consumer2.accept(Component.translatable("vote.no_option"));
		} else {
			this.applyPassedResults(consumer, consumer2, list2);
		}
	}

	private void displayVotingResults(Consumer<Component> consumer, FinishedVote.UnpackOptions unpackOptions, List<VoteResults.OptionResult> list) {
		if (unpackOptions.showTotals) {
			VoteResults.VoteCounts voteCounts = this.results.total();
			displayTotals(consumer, unpackOptions, voteCounts, Component.translatable("vote.total_count", voteCounts.votesCount(), voteCounts.votersCount()));
		}

		Comparator<VoteResults.OptionResult> comparator = unpackOptions.selectTopVotes
			? VoteResults.OptionResult.BY_VOTES.reversed()
			: VoteResults.OptionResult.BY_VOTES;
		list.stream()
			.sorted(comparator.thenComparing(INDEX_COMPARATOR))
			.forEach(
				optionResult -> {
					VoteResults.VoteCounts voteCounts = optionResult.counts();
					OptionId optionId = optionResult.optionId();
					Component component = this.getOptionName(optionId);
					Component component2 = unpackOptions.showTotals
						? Component.translatable("vote.option_count", optionId.index() + 1, component, voteCounts.votesCount(), voteCounts.votersCount())
						: Component.translatable("vote.option_no_count", optionId.index() + 1, component);
					displayTotals(consumer, unpackOptions, voteCounts, component2);
				}
			);
	}

	private static void displayTotals(
		Consumer<Component> consumer, FinishedVote.UnpackOptions unpackOptions, VoteResults.VoteCounts voteCounts, Component component
	) {
		if (!unpackOptions.selectTopVotes || voteCounts.votesCount() != 0) {
			if (unpackOptions.showTotals) {
				consumer.accept(component);
			}

			if (unpackOptions.showVoters) {
				Map<UUID, Voter> map = voteCounts.votes().voters();
				if (!map.isEmpty()) {
					consumer.accept(!unpackOptions.showTotals ? component : Component.translatable("vote.voters"));
					displayVoters(consumer, map);
				}
			}
		}
	}

	private static void displayVoters(Consumer<Component> consumer, Map<UUID, Voter> map) {
		map.entrySet()
			.stream()
			.sorted(VOTER_COMPARATOR)
			.forEach(entry -> consumer.accept(Component.translatable("vote.voter", ((Voter)entry.getValue()).displayName(), ((Voter)entry.getValue()).voteCount())));
	}

	private List<VoteResults.OptionResult> selectResults(
		Consumer<Component> consumer, FinishedVote.UnpackOptions unpackOptions, List<VoteResults.OptionResult> list
	) {
		List<VoteResults.OptionResult> list2;
		if (this.checkQuorum(consumer, unpackOptions)) {
			list2 = this.pickWinningOptions(consumer, list, unpackOptions);
		} else {
			list2 = List.of();
		}

		if (list2.isEmpty() && this.checkRandomFallback(unpackOptions)) {
			consumer.accept(Component.translatable("vote.no_option.random"));
			ObjectArrayList<VoteResults.OptionResult> objectArrayList = (ObjectArrayList<VoteResults.OptionResult>)list.stream()
				.sorted(INDEX_COMPARATOR)
				.collect(Collectors.toCollection(ObjectArrayList::new));
			Util.shuffle(objectArrayList, unpackOptions.random);
			list2 = objectArrayList.stream().limit((long)unpackOptions.maxSelectedOptions).toList();
		}

		return list2;
	}

	private List<VoteResults.OptionResult> pickWinningOptions(
		Consumer<Component> consumer, List<VoteResults.OptionResult> list, FinishedVote.UnpackOptions unpackOptions
	) {
		Map<Integer, List<VoteResults.OptionResult>> map = (Map<Integer, List<VoteResults.OptionResult>>)list.stream()
			.collect(Collectors.groupingBy(optionResult -> optionResult.counts().votesCount(), Collectors.toCollection(ArrayList::new)));
		List<VoteResults.OptionResult> list2 = new ArrayList();
		boolean bl = false;
		int i = this.calculateVotesNeeded(consumer, unpackOptions);
		Comparator<Integer> comparator = unpackOptions.selectTopVotes ? Comparator.reverseOrder() : Comparator.naturalOrder();

		for (Entry<Integer, List<VoteResults.OptionResult>> entry : map.entrySet()
			.stream()
			.filter(entryx -> (Integer)entryx.getKey() >= i)
			.sorted(Entry.comparingByKey(comparator))
			.toList()) {
			List<VoteResults.OptionResult> list4 = (List<VoteResults.OptionResult>)entry.getValue();
			if (!unpackOptions.onlyApplyOptionsWithVotes || (Integer)entry.getKey() != 0) {
				if (list4.size() > 1) {
					TieResolutionStrategy tieResolutionStrategy = unpackOptions.tieStrategy;
					if (!bl) {
						bl = true;
						consumer.accept(Component.translatable("vote.tie", tieResolutionStrategy.getDisplayName()));
					}

					switch (tieResolutionStrategy) {
						case PICK_LOW:
							list4.stream().min(INDEX_COMPARATOR).ifPresent(list2::add);
							break;
						case PICK_HIGH:
							list4.stream().max(INDEX_COMPARATOR).ifPresent(list2::add);
							break;
						case PICK_RANDOM:
							Util.getRandomSafe(list4, unpackOptions.random).ifPresent(list2::add);
							break;
						case PICK_ALL:
							list4.stream().sorted(INDEX_COMPARATOR).forEach(list2::add);
						case PICK_NONE:
						default:
							break;
						case FAIL:
							list2.clear();
							return list2;
					}
				} else {
					list2.addAll(list4);
				}

				if (list2.size() >= unpackOptions.maxSelectedOptions) {
					break;
				}
			}
		}

		return list2;
	}

	private int calculateVotesNeeded(Consumer<Component> consumer, FinishedVote.UnpackOptions unpackOptions) {
		int i = Math.round(unpackOptions.absoluteMajorityVotes * (float)this.results.total().votesCount());
		if (i > 0) {
			consumer.accept(Component.translatable("vote.vote_count.minimum", i));
		}

		return i;
	}

	private boolean checkQuorum(Consumer<Component> consumer, FinishedVote.UnpackOptions unpackOptions) {
		int i = unpackOptions.requireAtLeastOnePlayer ? 1 : 0;
		int j = Math.max(Math.round(unpackOptions.quorum * (float)unpackOptions.playerCount), i);
		int k = this.results.total().votersCount();
		if (k < j) {
			consumer.accept(Component.translatable("vote.quorum.not_reached", j));
			return false;
		} else {
			consumer.accept(Component.translatable("vote.quorum.passed", k, j));
			return true;
		}
	}

	private boolean checkRandomFallback(FinishedVote.UnpackOptions unpackOptions) {
		return unpackOptions.pickRandomOnFail && (!unpackOptions.requireAtLeastOnePlayer || this.results.total().votersCount() > 0);
	}

	private void applyPassedResults(Consumer<ServerVote.Effect> consumer, Consumer<Component> consumer2, List<VoteResults.OptionResult> list) {
		record ResultPair(OptionId id, ServerVote.Option option) {
		}

		List<ResultPair> list2 = new ArrayList();

		for (VoteResults.OptionResult optionResult : list) {
			OptionId optionId = optionResult.optionId();
			ServerVote.Option option = this.getOption(optionId);
			if (option != null) {
				list2.add(new ResultPair(optionId, option));
			}
		}

		boolean bl = list2.stream().anyMatch(arg -> !arg.option.changes().isEmpty());
		if (!bl) {
			consumer2.accept(Component.translatable("vote.no_change"));
		} else {
			for (ResultPair lv : list2) {
				if (lv.option.changes().isEmpty()) {
					consumer2.accept(Component.translatable("vote.option_won.no_effect", lv.id.index() + 1, lv.option.displayName()).withStyle(ChatFormatting.GRAY));
				} else {
					consumer2.accept(Component.translatable("vote.option_won", lv.id.index() + 1, lv.option.displayName()).withStyle(ChatFormatting.GREEN));

					for (ServerVote.Effect effect : lv.option.changes()) {
						consumer2.accept(effect.description());
						consumer.accept(effect);
					}
				}
			}
		}
	}

	public static record UnpackOptions(
		RandomSource random,
		boolean requireAtLeastOnePlayer,
		int playerCount,
		float quorum,
		float absoluteMajorityVotes,
		boolean showTotals,
		boolean showVoters,
		boolean pickRandomOnFail,
		boolean selectTopVotes,
		boolean onlyApplyOptionsWithVotes,
		int maxSelectedOptions,
		TieResolutionStrategy tieStrategy
	) {

		public FinishedVote.UnpackOptions requireAtLeastOnePlayer(boolean bl) {
			return new FinishedVote.UnpackOptions(
				this.random,
				bl,
				this.playerCount,
				this.quorum,
				this.absoluteMajorityVotes,
				this.showTotals,
				this.showVoters,
				this.pickRandomOnFail,
				this.selectTopVotes,
				this.onlyApplyOptionsWithVotes,
				this.maxSelectedOptions,
				this.tieStrategy
			);
		}

		public FinishedVote.UnpackOptions quorum(float f) {
			return new FinishedVote.UnpackOptions(
				this.random,
				this.requireAtLeastOnePlayer,
				this.playerCount,
				f,
				this.absoluteMajorityVotes,
				this.showTotals,
				this.showVoters,
				this.pickRandomOnFail,
				this.selectTopVotes,
				this.onlyApplyOptionsWithVotes,
				this.maxSelectedOptions,
				this.tieStrategy
			);
		}

		public FinishedVote.UnpackOptions absoluteMajorityVotes(float f) {
			return new FinishedVote.UnpackOptions(
				this.random,
				this.requireAtLeastOnePlayer,
				this.playerCount,
				this.quorum,
				f,
				this.showTotals,
				this.showVoters,
				this.pickRandomOnFail,
				this.selectTopVotes,
				this.onlyApplyOptionsWithVotes,
				this.maxSelectedOptions,
				this.tieStrategy
			);
		}

		public FinishedVote.UnpackOptions showTotals(boolean bl) {
			return new FinishedVote.UnpackOptions(
				this.random,
				this.requireAtLeastOnePlayer,
				this.playerCount,
				this.quorum,
				this.absoluteMajorityVotes,
				bl,
				this.showVoters,
				this.pickRandomOnFail,
				this.selectTopVotes,
				this.onlyApplyOptionsWithVotes,
				this.maxSelectedOptions,
				this.tieStrategy
			);
		}

		public FinishedVote.UnpackOptions showVoters(boolean bl) {
			return new FinishedVote.UnpackOptions(
				this.random,
				this.requireAtLeastOnePlayer,
				this.playerCount,
				this.quorum,
				this.absoluteMajorityVotes,
				this.showTotals,
				bl,
				this.pickRandomOnFail,
				this.selectTopVotes,
				this.onlyApplyOptionsWithVotes,
				this.maxSelectedOptions,
				this.tieStrategy
			);
		}

		public FinishedVote.UnpackOptions pickRandomOnFail(boolean bl) {
			return new FinishedVote.UnpackOptions(
				this.random,
				this.requireAtLeastOnePlayer,
				this.playerCount,
				this.quorum,
				this.absoluteMajorityVotes,
				this.showTotals,
				this.showVoters,
				bl,
				this.selectTopVotes,
				this.onlyApplyOptionsWithVotes,
				this.maxSelectedOptions,
				this.tieStrategy
			);
		}

		public FinishedVote.UnpackOptions selectTopVotes(boolean bl) {
			return new FinishedVote.UnpackOptions(
				this.random,
				this.requireAtLeastOnePlayer,
				this.playerCount,
				this.quorum,
				this.absoluteMajorityVotes,
				this.showTotals,
				this.showVoters,
				this.pickRandomOnFail,
				bl,
				this.onlyApplyOptionsWithVotes,
				this.maxSelectedOptions,
				this.tieStrategy
			);
		}

		public FinishedVote.UnpackOptions onlyApplyOptionsWithVotes(boolean bl) {
			return new FinishedVote.UnpackOptions(
				this.random,
				this.requireAtLeastOnePlayer,
				this.playerCount,
				this.quorum,
				this.absoluteMajorityVotes,
				this.showTotals,
				this.showVoters,
				this.pickRandomOnFail,
				this.selectTopVotes,
				bl,
				this.maxSelectedOptions,
				this.tieStrategy
			);
		}

		public FinishedVote.UnpackOptions maxSelectedOptions(int i) {
			return new FinishedVote.UnpackOptions(
				this.random,
				this.requireAtLeastOnePlayer,
				this.playerCount,
				this.quorum,
				this.absoluteMajorityVotes,
				this.showTotals,
				this.showVoters,
				this.pickRandomOnFail,
				this.selectTopVotes,
				this.onlyApplyOptionsWithVotes,
				i,
				this.tieStrategy
			);
		}

		public FinishedVote.UnpackOptions tieStrategy(TieResolutionStrategy tieResolutionStrategy) {
			return new FinishedVote.UnpackOptions(
				this.random,
				this.requireAtLeastOnePlayer,
				this.playerCount,
				this.quorum,
				this.absoluteMajorityVotes,
				this.showTotals,
				this.showVoters,
				this.pickRandomOnFail,
				this.selectTopVotes,
				this.onlyApplyOptionsWithVotes,
				this.maxSelectedOptions,
				tieResolutionStrategy
			);
		}
	}
}
