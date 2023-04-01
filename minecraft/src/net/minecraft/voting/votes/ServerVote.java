package net.minecraft.voting.votes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.voting.rules.Rule;
import net.minecraft.voting.rules.RuleAction;
import net.minecraft.voting.rules.RuleChange;
import net.minecraft.voting.rules.Rules;
import org.apache.commons.lang3.mutable.MutableInt;

public record ServerVote(CommonVoteData header, Map<OptionId, ServerVote.Option> options) {
	public static final Codec<ServerVote> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					CommonVoteData.CODEC.forGetter(ServerVote::header),
					Codec.unboundedMap(OptionId.STRING_CODEC, ServerVote.Option.CODEC).fieldOf("options").forGetter(ServerVote::options)
				)
				.apply(instance, ServerVote::new)
	);
	public static final Component NOTHING_RULE = Component.translatable("rule.nothing");

	public boolean containsAction(RuleAction ruleAction) {
		return this.options.values().stream().anyMatch(option -> option.containsAction(ruleAction));
	}

	public ClientVote toClientVote() {
		return new ClientVote(
			this.header,
			(Map<OptionId, ClientVote.ClientOption>)this.options
				.entrySet()
				.stream()
				.collect(
					Collectors.toUnmodifiableMap(
						Entry::getKey,
						entry -> new ClientVote.ClientOption(((ServerVote.Option)entry.getValue()).displayName(), ((ServerVote.Option)entry.getValue()).changes.isEmpty())
					)
				)
		);
	}

	public static Optional<ServerVote> createRandomApproveVote(
		UUID uUID, Set<Rule> set, MinecraftServer minecraftServer, ServerVote.VoteGenerationOptions voteGenerationOptions
	) {
		return randomVoteStream(set, voteGenerationOptions)
			.findAny()
			.flatMap(reference -> createRandomApproveVote(uUID, minecraftServer, voteGenerationOptions, (Rule)reference.value()));
	}

	private static Stream<Holder.Reference<Rule>> randomVoteStream(Set<Rule> set, ServerVote.VoteGenerationOptions voteGenerationOptions) {
		return Stream.generate(() -> Rules.getRandomRule(voteGenerationOptions.random)).limit(1000L).filter(reference -> !set.contains(reference.value()));
	}

	private static Stream<Holder.Reference<Rule>> randomUnweightedVoteStream(Set<Rule> set, ServerVote.VoteGenerationOptions voteGenerationOptions) {
		return Stream.generate(() -> Rules.getRandomRuleUnweighted(voteGenerationOptions.random)).limit(1000L).filter(reference -> !set.contains(reference.value()));
	}

	private static ServerVote createVote(
		UUID uUID, MinecraftServer minecraftServer, ServerVote.VoteGenerationOptions voteGenerationOptions, List<ServerVote.Option> list
	) {
		Map<OptionId, ServerVote.Option> map = new HashMap();

		for (int i = 0; i < list.size(); i++) {
			map.put(new OptionId(uUID, i), (ServerVote.Option)list.get(i));
		}

		int i = minecraftServer.getVoteStorage().nextProposalCount();
		Component component = Component.translatable("rule.proposal", i + 1);
		long l = minecraftServer.getWorldData().overworldData().getGameTime();
		long m = (long)voteGenerationOptions.sampleDurationTicks();
		return new ServerVote(new CommonVoteData(component, l, m, voteGenerationOptions.voteCost), map);
	}

	public static Optional<ServerVote> createRandomApproveVote(
		UUID uUID, MinecraftServer minecraftServer, ServerVote.VoteGenerationOptions voteGenerationOptions, Rule rule
	) {
		int i = voteGenerationOptions.sampleMaxApproveOptions();
		List<List<RuleChange>> list = new ArrayList(i);
		rule.randomApprovableChanges(minecraftServer, voteGenerationOptions.random, i).forEach(ruleChange -> {
			List<RuleChange> list2x = new ArrayList();
			list2x.add(ruleChange);
			list.add(list2x);
		});
		if (list.isEmpty()) {
			return Optional.empty();
		} else {
			Set<Rule> set = new HashSet();
			set.add(rule);

			for (int j = 0; j < voteGenerationOptions.maxExtraOptions && voteGenerationOptions.canAddExtraOption(); j++) {
				int k = list.size();
				MutableInt mutableInt = new MutableInt();
				Stream.generate(() -> Rules.getRandomRule(voteGenerationOptions.random))
					.filter(reference -> !set.contains(reference.value()))
					.flatMap(reference -> ((Rule)reference.value()).randomApprovableChanges(minecraftServer, voteGenerationOptions.random, k))
					.limit((long)k)
					.forEachOrdered(ruleChange -> {
						set.add(ruleChange.rule());
						((List)list.get(mutableInt.getAndIncrement())).add(ruleChange);
					});
			}

			if (voteGenerationOptions.alwaysAddOptOutVote() || list.size() == 1) {
				list.add(List.of());
			}

			RuleAction ruleAction = RuleAction.APPROVE;
			List<ServerVote.Option> list2 = list.stream().map(listx -> {
				List<ServerVote.Effect> list2x = listx.stream().map(ruleChange -> new ServerVote.Effect(ruleChange, ruleAction)).toList();
				return new ServerVote.Option(getRuleDisplayName(list2x), list2x);
			}).toList();
			return Optional.of(createVote(uUID, minecraftServer, voteGenerationOptions, list2));
		}
	}

	public static Optional<ServerVote> createRandomRepealVote(
		UUID uUID, Set<Rule> set, MinecraftServer minecraftServer, ServerVote.VoteGenerationOptions voteGenerationOptions
	) {
		int i = voteGenerationOptions.sampleMaxRepealOptions();
		List<ServerVote.Option> list = (List<ServerVote.Option>)randomUnweightedVoteStream(set, voteGenerationOptions)
			.distinct()
			.flatMap(reference -> ((Rule)reference.value()).repealableChanges())
			.limit((long)i)
			.map(ruleChange -> {
				List<ServerVote.Effect> listx = List.of(new ServerVote.Effect(ruleChange, RuleAction.REPEAL));
				return new ServerVote.Option(getRuleDisplayName(listx), listx);
			})
			.collect(Collectors.toCollection(ArrayList::new));
		if (list.isEmpty()) {
			return Optional.empty();
		} else {
			if (voteGenerationOptions.alwaysAddOptOutVote() || list.size() == 1) {
				list.add(new ServerVote.Option(getRuleDisplayName(List.of()), List.of()));
			}

			return Optional.of(createVote(uUID, minecraftServer, voteGenerationOptions, list));
		}
	}

	private static Component getRuleDisplayName(List<ServerVote.Effect> list) {
		return (Component)list.stream()
			.map(ServerVote.Effect::description)
			.reduce((component, component2) -> Component.translatable("rule.connector", component, component2))
			.orElse(NOTHING_RULE);
	}

	public static record Effect(RuleChange change, RuleAction action) {
		public static final Codec<ServerVote.Effect> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						RuleChange.CODEC.fieldOf("change").forGetter(ServerVote.Effect::change), RuleAction.CODEC.fieldOf("action").forGetter(ServerVote.Effect::action)
					)
					.apply(instance, ServerVote.Effect::new)
		);

		public void apply(MinecraftServer minecraftServer) {
			this.change.apply(this.action, minecraftServer);
		}

		public Component description() {
			return this.change.description(this.action);
		}
	}

	public static record Option(Component displayName, List<ServerVote.Effect> changes) {
		public static final Codec<ServerVote.Option> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.COMPONENT.fieldOf("display_name").forGetter(ServerVote.Option::displayName),
						ServerVote.Effect.CODEC.listOf().fieldOf("changes").forGetter(ServerVote.Option::changes)
					)
					.apply(instance, ServerVote.Option::new)
		);

		public boolean containsAction(RuleAction ruleAction) {
			return this.changes.stream().anyMatch(effect -> effect.action == ruleAction);
		}
	}

	public static record VoteGenerationOptions(
		RandomSource random,
		float newVoteChancePerTick,
		IntProvider optionsPerApproveVote,
		IntProvider optionsPerRepealVote,
		IntProvider durationMinutes,
		float extraOptionChance,
		int maxExtraOptions,
		List<VotingMaterial.Cost> voteCost,
		boolean alwaysAddOptOutVote,
		int maxApproveVoteCount,
		int maxRepealVoteCount,
		float repealVoteChance
	) {

		public static ServerVote.VoteGenerationOptions createFromRules(RandomSource randomSource) {
			return new ServerVote.VoteGenerationOptions(
				randomSource,
				1.0F / (float)((Integer)Rules.NEW_VOTE_CHANCE_PER_TICK.get()).intValue(),
				Rules.NEW_APPROVE_VOTE_OPTION_COUNT.get(),
				Rules.NEW_REPEAL_VOTE_OPTION_COUNT.get(),
				Rules.NEW_VOTE_DURATION_MINUTES.get(),
				(float)((Integer)Rules.NEW_VOTE_EXTRA_EFFECT_CHANCE.get()).intValue() / 100.0F,
				(Integer)Rules.NEW_VOTE_EXTRA_EFFECT_MAX_COUNT.get(),
				Rules.NEW_VOTE_COST.get(),
				!Rules.NEW_VOTE_NO_OPT_OUT.get(),
				(Integer)Rules.NEW_VOTE_MAX_APPROVE_VOTE_COUNT.get(),
				(Integer)Rules.NEW_VOTE_MAX_REPEAL_VOTE_COUNT.get(),
				(float)((Integer)Rules.NEW_VOTE_REPEAL_VOTE_CHANCE.get()).intValue() / 100.0F
			);
		}

		public int sampleDurationTicks() {
			return this.durationMinutes.sample(this.random) * 1200;
		}

		public boolean canSpawnVoteInThisTick() {
			return this.random.nextFloat() < this.newVoteChancePerTick;
		}

		public boolean shouldBeRepeal() {
			return this.random.nextFloat() < this.repealVoteChance;
		}

		public boolean canAddExtraOption() {
			return this.random.nextFloat() < this.extraOptionChance;
		}

		public int sampleMaxApproveOptions() {
			return this.optionsPerApproveVote.sample(this.random);
		}

		public int sampleMaxRepealOptions() {
			return this.optionsPerRepealVote.sample(this.random);
		}
	}
}
