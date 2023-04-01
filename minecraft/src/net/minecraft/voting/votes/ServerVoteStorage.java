package net.minecraft.voting.votes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.Rule;
import net.minecraft.voting.rules.RuleAction;
import net.minecraft.voting.rules.RuleChange;
import net.minecraft.world.entity.Entity;

public class ServerVoteStorage {
	private static final long RAMP_UP_TICKS = 18000L;
	private final RegistryAccess registryAccess;
	private final Map<UUID, ServerVote> pendingVotes = new HashMap();
	final VoterMap votes = new VoterMap();
	private int proposalCount;

	public ServerVoteStorage(RegistryAccess registryAccess) {
		this.registryAccess = registryAccess;
	}

	public void load(VoteStorage voteStorage) {
		this.pendingVotes.clear();
		this.pendingVotes.putAll(voteStorage.pending());
		this.proposalCount = voteStorage.totalProposalCount();
		this.votes.load(voteStorage.votes());

		for (RuleChange ruleChange : voteStorage.approved()) {
			ruleChange.update(RuleAction.APPROVE);
		}
	}

	public void resetAllRules() {
		this.registryAccess.registryOrThrow(Registries.RULE).stream().forEach(rule -> rule.repealAll(true));
	}

	public VoteStorage save() {
		return VoteStorage.fromApprovedRules(this.registryAccess.registryOrThrow(Registries.RULE).holders(), this.pendingVotes, this.votes.save(), this.proposalCount);
	}

	public void tick(
		long l,
		MinecraftServer minecraftServer,
		ServerVote.VoteGenerationOptions voteGenerationOptions,
		Consumer<FinishedVote> consumer,
		BiConsumer<UUID, ServerVote> biConsumer
	) {
		boolean bl = this.checkRampUpFactor(minecraftServer, voteGenerationOptions.random()) && voteGenerationOptions.canSpawnVoteInThisTick();
		boolean bl2 = voteGenerationOptions.shouldBeRepeal();
		int i = 0;
		Set<Rule> set = new HashSet();
		Iterator<Entry<UUID, ServerVote>> iterator = this.pendingVotes.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<UUID, ServerVote> entry = (Entry<UUID, ServerVote>)iterator.next();
			UUID uUID = (UUID)entry.getKey();
			ServerVote serverVote = (ServerVote)entry.getValue();
			if (l >= serverVote.header().end()) {
				iterator.remove();
				OptionVoteStorage optionVoteStorage = this.votes.collectVotes(serverVote, true);
				consumer.accept(new FinishedVote(uUID, serverVote, optionVoteStorage.getVotingResults()));
			} else if (bl) {
				extractVoteRules(set, serverVote);
				if (bl2) {
					if (serverVote.containsAction(RuleAction.REPEAL)) {
						i++;
					}
				} else if (serverVote.containsAction(RuleAction.APPROVE)) {
					i++;
				}
			}
		}

		if (bl) {
			if (bl2) {
				int j = voteGenerationOptions.maxRepealVoteCount();
				if (i < j) {
					UUID uUID = UUID.randomUUID();
					ServerVote.createRandomRepealVote(uUID, set, minecraftServer, voteGenerationOptions).ifPresent(serverVotex -> biConsumer.accept(uUID, serverVotex));
				}
			} else {
				int j = voteGenerationOptions.maxApproveVoteCount();
				if (i < j) {
					UUID uUID = UUID.randomUUID();
					ServerVote.createRandomApproveVote(uUID, set, minecraftServer, voteGenerationOptions).ifPresent(serverVotex -> biConsumer.accept(uUID, serverVotex));
				}
			}
		}
	}

	private boolean checkRampUpFactor(MinecraftServer minecraftServer, RandomSource randomSource) {
		long l = minecraftServer.getWorldData().overworldData().getGameTime();
		float f = (float)l / 18000.0F;
		if (f > 1.0F) {
			return true;
		} else {
			float g = f * 7.0F;
			float h = (float)this.pendingVotes.size() - g;
			if (h < 0.0F) {
				return true;
			} else {
				float i = (float)Math.pow(0.1, (double)h);
				return randomSource.nextFloat() < i;
			}
		}
	}

	public int nextProposalCount() {
		return this.proposalCount;
	}

	public void startVote(UUID uUID, ServerVote serverVote) {
		this.pendingVotes.put(uUID, serverVote);
		this.proposalCount++;
	}

	@Nullable
	public FinishedVote finishVote(UUID uUID) {
		ServerVote serverVote = (ServerVote)this.pendingVotes.remove(uUID);
		if (serverVote != null) {
			OptionVoteStorage optionVoteStorage = this.votes.collectVotes(serverVote, true);
			return new FinishedVote(uUID, serverVote, optionVoteStorage.getVotingResults());
		} else {
			return null;
		}
	}

	public Stream<UUID> getPendingVotesIds() {
		return this.pendingVotes.keySet().stream();
	}

	public void visitAllPendingVotes(BiConsumer<UUID, ServerVote> biConsumer) {
		this.pendingVotes.forEach(biConsumer);
	}

	public void visitVotesFromPlayer(UUID uUID, BiConsumer<OptionId, Voter> biConsumer) {
		this.votes.visitVotesFromPlayer(uUID, biConsumer);
	}

	@Nullable
	public ServerVoteStorage.OptionAccess getOptionAccess(OptionId optionId) {
		ServerVote serverVote = (ServerVote)this.pendingVotes.get(optionId.voteId());
		return serverVote == null ? null : new ServerVoteStorage.OptionAccess(optionId, serverVote);
	}

	public OptionVotes getCurrentVoteCountForOption(OptionId optionId) {
		return this.votes.collectVotes(optionId, false);
	}

	public Set<Rule> rulesWithPendingCommands() {
		Set<Rule> set = new HashSet();

		for (ServerVote serverVote : this.pendingVotes.values()) {
			extractVoteRules(set, serverVote);
		}

		return set;
	}

	private static void extractVoteRules(Set<Rule> set, ServerVote serverVote) {
		for (ServerVote.Option option : serverVote.options().values()) {
			for (ServerVote.Effect effect : option.changes()) {
				set.add(effect.change().rule());
			}
		}
	}

	public class OptionAccess {
		private final OptionId optionId;
		private final ServerVote vote;

		public OptionAccess(OptionId optionId, ServerVote serverVote) {
			this.optionId = optionId;
			this.vote = serverVote;
		}

		public ServerVoteStorage.VoteResult consumeResources(ServerPlayer serverPlayer) {
			List<VotingMaterial.Cost> list = this.vote.header().cost();

			for (VotingMaterial.Cost cost : list) {
				if (cost.material() == VotingMaterial.VOTES_PER_PROPOSAL) {
					int i = ServerVoteStorage.this.votes.getVotes(this.vote.options().keySet(), serverPlayer.getUUID());
					if (i >= cost.count()) {
						return ServerVoteStorage.VoteResult.NO_MORE_VOTES;
					}
				} else if (cost.material() == VotingMaterial.VOTES_PER_OPTION) {
					int i = ServerVoteStorage.this.votes.getVotes(this.optionId, serverPlayer.getUUID());
					if (i > cost.count()) {
						return ServerVoteStorage.VoteResult.NO_MORE_VOTES;
					}
				} else if (!cost.deduct(serverPlayer, true)) {
					return ServerVoteStorage.VoteResult.NOT_ENOUGH_RESOURCES;
				}
			}

			for (VotingMaterial.Cost costx : list) {
				if (costx.material() != VotingMaterial.VOTES_PER_PROPOSAL && costx.material() != VotingMaterial.VOTES_PER_OPTION) {
					costx.deduct(serverPlayer, false);
				}
			}

			return ServerVoteStorage.VoteResult.OK;
		}

		public void addVotes(Entity entity, int i) {
			ServerVoteStorage.this.votes.addVote(this.optionId, entity.getUUID(), entity.getName(), i);
		}

		public Component proposalName() {
			return this.vote.header().displayName();
		}

		public Component optionName() {
			ServerVote.Option option = (ServerVote.Option)this.vote.options().get(this.optionId);
			return (Component)(option != null ? option.displayName() : Component.empty());
		}
	}

	public static enum VoteResult {
		OK,
		NO_MORE_VOTES,
		NOT_ENOUGH_RESOURCES;
	}
}
