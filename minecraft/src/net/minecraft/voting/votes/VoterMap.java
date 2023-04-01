package net.minecraft.voting.votes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;

public class VoterMap {
	private final Map<OptionId, Map<UUID, Voter>> options = new HashMap();

	public void addVote(OptionId optionId, UUID uUID, Component component, int i) {
		Map<UUID, Voter> map = (Map<UUID, Voter>)this.options.computeIfAbsent(optionId, optionIdx -> new HashMap());
		map.compute(uUID, (uUIDx, voter) -> Voter.update(voter, component, i));
	}

	public int getVotes(OptionId optionId, UUID uUID) {
		Map<UUID, Voter> map = (Map<UUID, Voter>)this.options.get(optionId);
		if (map == null) {
			return 0;
		} else {
			Voter voter = (Voter)map.get(uUID);
			return voter != null ? voter.voteCount() : 0;
		}
	}

	public int getVotes(Set<OptionId> set, UUID uUID) {
		return set.stream().mapToInt(optionId -> this.getVotes(optionId, uUID)).sum();
	}

	public void load(OptionVoteStorage optionVoteStorage) {
		this.options.clear();
		optionVoteStorage.options().forEach((optionId, optionVotes) -> this.options.put(optionId, new HashMap(optionVotes.voters())));
	}

	public OptionVoteStorage save() {
		return new OptionVoteStorage(
			(Map<OptionId, OptionVotes>)this.options.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> freeze((Map<UUID, Voter>)entry.getValue())))
		);
	}

	private static OptionVotes freeze(Map<UUID, Voter> map) {
		return new OptionVotes(Map.copyOf(map));
	}

	public OptionVoteStorage collectVotes(ServerVote serverVote, boolean bl) {
		Map<OptionId, OptionVotes> map = new HashMap();
		serverVote.options().keySet().forEach(optionId -> {
			Map<UUID, Voter> map2 = bl ? (Map)this.options.remove(optionId) : (Map)this.options.get(optionId);
			if (map2 != null) {
				map.put(optionId, freeze(map2));
			}
		});
		return new OptionVoteStorage(map);
	}

	public OptionVotes collectVotes(OptionId optionId, boolean bl) {
		Map<UUID, Voter> map = bl ? (Map)this.options.remove(optionId) : (Map)this.options.get(optionId);
		return map != null ? freeze(map) : OptionVotes.EMPTY;
	}

	public void visitVotesFromPlayer(UUID uUID, BiConsumer<OptionId, Voter> biConsumer) {
		this.options.forEach((optionId, map) -> {
			Voter voter = (Voter)map.get(uUID);
			if (voter != null) {
				biConsumer.accept(optionId, voter);
			}
		});
	}
}
