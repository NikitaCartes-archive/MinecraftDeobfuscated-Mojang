package net.minecraft.network.chat;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.players.PlayerList;

public interface OutgoingPlayerChatMessage {
	PlayerChatMessage original();

	ClientboundPlayerChatPacket packetForPlayer(ServerPlayer serverPlayer, ChatType.Bound bound);

	void sendHeadersToRemainingPlayers(PlayerList playerList);

	static OutgoingPlayerChatMessage create(PlayerChatMessage playerChatMessage, ChatSender chatSender) {
		if (playerChatMessage.signer().isSystem()) {
			return new OutgoingPlayerChatMessage.NotTracked(playerChatMessage);
		} else {
			return (OutgoingPlayerChatMessage)(!playerChatMessage.signer().profileId().equals(chatSender.profileId())
				? new OutgoingPlayerChatMessage.Disguised(playerChatMessage)
				: new OutgoingPlayerChatMessage.Tracked(playerChatMessage));
		}
	}

	static FilteredText<OutgoingPlayerChatMessage> createFromFiltered(FilteredText<PlayerChatMessage> filteredText, ChatSender chatSender) {
		return filteredText.mapWithEquality(playerChatMessage -> create(filteredText.raw(), chatSender), OutgoingPlayerChatMessage.NotTracked::new);
	}

	public static class Disguised implements OutgoingPlayerChatMessage {
		private final PlayerChatMessage signedMessage;
		private final PlayerChatMessage unsignedMessage;

		public Disguised(PlayerChatMessage playerChatMessage) {
			this.signedMessage = playerChatMessage;
			this.unsignedMessage = PlayerChatMessage.unsigned(MessageSigner.system(), playerChatMessage.serverContent());
		}

		@Override
		public PlayerChatMessage original() {
			return this.signedMessage;
		}

		@Override
		public ClientboundPlayerChatPacket packetForPlayer(ServerPlayer serverPlayer, ChatType.Bound bound) {
			RegistryAccess registryAccess = serverPlayer.level.registryAccess();
			ChatType.BoundNetwork boundNetwork = bound.toNetwork(registryAccess);
			return new ClientboundPlayerChatPacket(this.unsignedMessage, boundNetwork);
		}

		@Override
		public void sendHeadersToRemainingPlayers(PlayerList playerList) {
			playerList.broadcastMessageHeader(this.signedMessage, Set.of());
		}
	}

	public static class NotTracked implements OutgoingPlayerChatMessage {
		private final PlayerChatMessage message;

		public NotTracked(PlayerChatMessage playerChatMessage) {
			this.message = playerChatMessage;
		}

		@Override
		public PlayerChatMessage original() {
			return this.message;
		}

		@Override
		public ClientboundPlayerChatPacket packetForPlayer(ServerPlayer serverPlayer, ChatType.Bound bound) {
			RegistryAccess registryAccess = serverPlayer.level.registryAccess();
			ChatType.BoundNetwork boundNetwork = bound.toNetwork(registryAccess);
			return new ClientboundPlayerChatPacket(this.message, boundNetwork);
		}

		@Override
		public void sendHeadersToRemainingPlayers(PlayerList playerList) {
		}
	}

	public static class Tracked implements OutgoingPlayerChatMessage {
		private final PlayerChatMessage message;
		private final Set<ServerPlayer> playersWithFullMessage = Sets.newIdentityHashSet();

		public Tracked(PlayerChatMessage playerChatMessage) {
			this.message = playerChatMessage;
		}

		@Override
		public PlayerChatMessage original() {
			return this.message;
		}

		@Override
		public ClientboundPlayerChatPacket packetForPlayer(ServerPlayer serverPlayer, ChatType.Bound bound) {
			this.playersWithFullMessage.add(serverPlayer);
			RegistryAccess registryAccess = serverPlayer.level.registryAccess();
			ChatType.BoundNetwork boundNetwork = bound.toNetwork(registryAccess);
			return new ClientboundPlayerChatPacket(this.message, boundNetwork);
		}

		@Override
		public void sendHeadersToRemainingPlayers(PlayerList playerList) {
			playerList.broadcastMessageHeader(this.message, this.playersWithFullMessage);
		}
	}
}
