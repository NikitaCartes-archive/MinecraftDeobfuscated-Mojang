package net.minecraft.client.voting;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.voting.votes.ClientVote;
import net.minecraft.voting.votes.OptionId;
import net.minecraft.voting.votes.OptionVotes;
import net.minecraft.voting.votes.Voter;
import net.minecraft.voting.votes.VotingMaterial;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientVoteStorage {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final ClientVoteStorage.StyledCodepoint DOT = new ClientVoteStorage.StyledCodepoint(46, Style.EMPTY.withColor(ChatFormatting.WHITE));
	private final Map<UUID, ClientVoteStorage.VoteInfo> votes = new HashMap();
	private final Int2ObjectMap<ClientVoteStorage.VoteResultCallback> votingCallbacks = new Int2ObjectOpenHashMap<>();
	private int transactionCount;

	private static FormattedText wrap(List<ClientVoteStorage.StyledCodepoint> list) {
		return new FormattedText() {
			@Override
			public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
				for (ClientVoteStorage.StyledCodepoint styledCodepoint : list) {
					Optional<T> optional = contentConsumer.accept(Character.toString(styledCodepoint.codepoint));
					if (optional.isPresent()) {
						return optional;
					}
				}

				return Optional.empty();
			}

			@Override
			public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
				for (ClientVoteStorage.StyledCodepoint styledCodepoint : list) {
					Optional<T> optional = styledContentConsumer.accept(style.applyTo(styledCodepoint.style), Character.toString(styledCodepoint.codepoint));
					if (optional.isPresent()) {
						return optional;
					}
				}

				return Optional.empty();
			}
		};
	}

	public void startVote(UUID uUID, ClientVote clientVote) {
		Component component = clientVote.header().displayName();
		List<List<ClientVoteStorage.StyledCodepoint>> list = (List<List<ClientVoteStorage.StyledCodepoint>>)clientVote.options()
			.values()
			.stream()
			.filter(clientOption -> !clientOption.irregular())
			.map(
				clientOption -> {
					List<ClientVoteStorage.StyledCodepoint> listx = new ArrayList();
					Component.translatable("vote.option_display", component, clientOption.displayName())
						.getVisualOrderText()
						.accept((ix, style, jx) -> listx.add(new ClientVoteStorage.StyledCodepoint(jx, style)));
					return listx;
				}
			)
			.collect(Collectors.toList());
		int i = Util.calculatePrefixSize(list);
		FormattedText formattedText;
		if (i == 0) {
			formattedText = FormattedText.EMPTY;
		} else {
			List<ClientVoteStorage.StyledCodepoint> list2 = new ArrayList((Collection)list.get(0));
			int j = i - 1;

			while (j >= 0 && Character.isSpaceChar(((ClientVoteStorage.StyledCodepoint)list2.get(j)).codepoint)) {
				j--;
			}

			List<ClientVoteStorage.StyledCodepoint> list3 = new ArrayList(list2.subList(0, j + 1));
			list3.add(DOT);
			list3.add(DOT);
			list3.add(DOT);
			formattedText = wrap(list3);
		}

		ClientVoteStorage.VoteInfo voteInfo = new ClientVoteStorage.VoteInfo(clientVote, formattedText);
		this.votes.put(uUID, voteInfo);
	}

	public void stopVote(UUID uUID) {
		this.votes.remove(uUID);
	}

	public boolean hasPendingVotes() {
		return !this.votes.isEmpty();
	}

	public void updateVoteCounts(OptionId optionId, OptionVotes optionVotes) {
		ClientVoteStorage.VoteInfo voteInfo = (ClientVoteStorage.VoteInfo)this.votes.get(optionId.voteId());
		if (voteInfo != null) {
			voteInfo.storeVotes(optionId.index(), optionVotes);
		}
	}

	public void visitVotes(BiConsumer<UUID, ClientVoteStorage.VoteInfo> biConsumer) {
		this.votes.forEach(biConsumer);
	}

	@Nullable
	public ClientVoteStorage.VoteInfo getVote(UUID uUID) {
		return (ClientVoteStorage.VoteInfo)this.votes.get(uUID);
	}

	public int voteFor(ClientVoteStorage.VoteResultCallback voteResultCallback) {
		int i = this.transactionCount++;
		this.votingCallbacks.put(i, voteResultCallback);
		return i;
	}

	public void onVoteFailed(int i, Optional<Component> optional) {
		ClientVoteStorage.VoteResultCallback voteResultCallback = this.votingCallbacks.remove(i);
		if (voteResultCallback == null) {
			LOGGER.warn("Received response for unknown vote: {}, {}", this.transactionCount, optional.map(Component::getString).orElse("<none>"));
		} else {
			voteResultCallback.run(i, optional);
		}
	}

	@Environment(EnvType.CLIENT)
	public static record CountAndLimit(int count, OptionalInt limit) {
		public boolean canVote() {
			return this.limit.isEmpty() || this.count < this.limit.getAsInt();
		}
	}

	@Environment(EnvType.CLIENT)
	static record StyledCodepoint(int codepoint, Style style) {
	}

	@Environment(EnvType.CLIENT)
	public static class VoteInfo {
		private final ClientVote voteInfo;
		private final Int2ObjectMap<Map<UUID, Voter>> optionToVoters = new Int2ObjectOpenHashMap<>();
		private final FormattedText title;
		private final OptionalInt limitPerOption;
		private final OptionalInt limitPerProposal;

		VoteInfo(ClientVote clientVote, FormattedText formattedText) {
			this.voteInfo = clientVote;
			OptionalInt optionalInt = OptionalInt.empty();
			OptionalInt optionalInt2 = OptionalInt.empty();

			for (VotingMaterial.Cost cost : clientVote.header().cost()) {
				if (cost.material() == VotingMaterial.VOTES_PER_PROPOSAL) {
					optionalInt2 = OptionalInt.of(cost.count());
				} else if (cost.material() == VotingMaterial.VOTES_PER_OPTION) {
					optionalInt = OptionalInt.of(cost.count());
				}
			}

			this.limitPerOption = optionalInt;
			this.limitPerProposal = optionalInt2;
			this.title = formattedText;
		}

		public void storeVotes(int i, OptionVotes optionVotes) {
			this.optionToVoters.computeIfAbsent(i, (Int2ObjectFunction<? extends Map<UUID, Voter>>)(ix -> new HashMap())).putAll(optionVotes.voters());
		}

		public FormattedText title() {
			return this.title;
		}

		public ClientVote voteInfo() {
			return this.voteInfo;
		}

		public long leftoverTicks(long l) {
			long m = l - this.voteInfo.header().start();
			return this.voteInfo.header().duration() - m;
		}

		private int voteCount(UUID uUID, OptionId optionId) {
			Map<UUID, Voter> map = this.optionToVoters.get(optionId.index());
			if (map == null) {
				return 0;
			} else {
				Voter voter = (Voter)map.get(uUID);
				return voter != null ? voter.voteCount() : 0;
			}
		}

		public ClientVoteStorage.CountAndLimit optionVoteCount(UUID uUID, OptionId optionId) {
			int i = this.voteCount(uUID, optionId);
			return new ClientVoteStorage.CountAndLimit(i, this.limitPerOption);
		}

		public ClientVoteStorage.CountAndLimit proposalVoteCount(UUID uUID) {
			int i = this.voteInfo.options().keySet().stream().mapToInt(optionId -> this.voteCount(uUID, optionId)).sum();
			return new ClientVoteStorage.CountAndLimit(i, this.limitPerProposal);
		}

		public boolean hasAnyVotesLeft(UUID uUID) {
			if (!this.proposalVoteCount(uUID).canVote()) {
				return false;
			} else {
				for (OptionId optionId : this.voteInfo.options().keySet()) {
					if (this.optionVoteCount(uUID, optionId).canVote()) {
						return true;
					}
				}

				return false;
			}
		}
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface VoteResultCallback {
		void run(int i, Optional<Component> optional);
	}
}
