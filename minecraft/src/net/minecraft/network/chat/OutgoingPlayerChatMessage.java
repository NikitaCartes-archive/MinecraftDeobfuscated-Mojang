package net.minecraft.network.chat;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.game.ClientboundPlayerChatHeaderPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public interface OutgoingPlayerChatMessage {
	Component serverContent();

	void sendToPlayer(ServerPlayer serverPlayer, boolean bl, ChatType.Bound bound);

	void sendHeadersToRemainingPlayers(PlayerList playerList);

	static OutgoingPlayerChatMessage create(PlayerChatMessage playerChatMessage) {
		return (OutgoingPlayerChatMessage)(playerChatMessage.signer().isSystem()
			? new OutgoingPlayerChatMessage.NotTracked(playerChatMessage)
			: new OutgoingPlayerChatMessage.Tracked(playerChatMessage));
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
		public void sendToPlayer(ServerPlayer serverPlayer, boolean bl, ChatType.Bound bound) {
			PlayerChatMessage playerChatMessage = this.message.filter(bl);
			if (!playerChatMessage.isFullyFiltered()) {
				RegistryAccess registryAccess = serverPlayer.level.registryAccess();
				ChatType.BoundNetwork boundNetwork = bound.toNetwork(registryAccess);
				serverPlayer.connection.send(new ClientboundPlayerChatPacket(playerChatMessage, boundNetwork));
				serverPlayer.connection.addPendingMessage(playerChatMessage);
			}
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
		public void sendToPlayer(ServerPlayer serverPlayer, boolean bl, ChatType.Bound bound) {
			PlayerChatMessage playerChatMessage = this.message.filter(bl);
			if (!playerChatMessage.isFullyFiltered()) {
				this.playersWithFullMessage.add(serverPlayer);
				RegistryAccess registryAccess = serverPlayer.level.registryAccess();
				ChatType.BoundNetwork boundNetwork = bound.toNetwork(registryAccess);
				serverPlayer.connection
					.send(
						new ClientboundPlayerChatPacket(playerChatMessage, boundNetwork),
						PacketSendListener.exceptionallySend(() -> new ClientboundPlayerChatHeaderPacket(this.message))
					);
				serverPlayer.connection.addPendingMessage(playerChatMessage);
			}
		}

		@Override
		public void sendHeadersToRemainingPlayers(PlayerList playerList) {
			playerList.broadcastMessageHeader(this.message, this.playersWithFullMessage);
		}
	}
}
