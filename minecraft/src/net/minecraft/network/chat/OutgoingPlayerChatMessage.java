package net.minecraft.network.chat;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.players.PlayerList;

public interface OutgoingPlayerChatMessage {
	Component serverContent();

	ClientboundPlayerChatPacket packetForPlayer(ServerPlayer serverPlayer, ChatType.Bound bound);

	void sendHeadersToRemainingPlayers(PlayerList playerList);

	static OutgoingPlayerChatMessage create(PlayerChatMessage playerChatMessage) {
		return (OutgoingPlayerChatMessage)(playerChatMessage.signer().isSystem()
			? new OutgoingPlayerChatMessage.NotTracked(playerChatMessage)
			: new OutgoingPlayerChatMessage.Tracked(playerChatMessage));
	}

	static FilteredText<OutgoingPlayerChatMessage> createFromFiltered(FilteredText<PlayerChatMessage> filteredText) {
		OutgoingPlayerChatMessage outgoingPlayerChatMessage = create(filteredText.raw());
		return filteredText.rebuildIfNeeded(outgoingPlayerChatMessage, OutgoingPlayerChatMessage.NotTracked::new);
	}

	public static class NotTracked implements OutgoingPlayerChatMessage {
		private final PlayerChatMessage message;

		public NotTracked(PlayerChatMessage playerChatMessage) {
			this.message = playerChatMessage;
		}

		@Override
		public Component serverContent() {
			return this.message.serverContent();
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
		public Component serverContent() {
			return this.message.serverContent();
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
