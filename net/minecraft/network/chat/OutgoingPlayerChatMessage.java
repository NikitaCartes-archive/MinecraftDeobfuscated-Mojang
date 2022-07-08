/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.players.PlayerList;

public interface OutgoingPlayerChatMessage {
    public PlayerChatMessage original();

    public ClientboundPlayerChatPacket packetForPlayer(ServerPlayer var1, ChatType.Bound var2);

    public void sendHeadersToRemainingPlayers(PlayerList var1);

    public static OutgoingPlayerChatMessage create(PlayerChatMessage playerChatMessage, ChatSender chatSender) {
        if (playerChatMessage.signer().isSystem()) {
            return new NotTracked(playerChatMessage);
        }
        if (!playerChatMessage.signer().profileId().equals(chatSender.profileId())) {
            return new Disguised(playerChatMessage);
        }
        return new Tracked(playerChatMessage);
    }

    public static FilteredText<OutgoingPlayerChatMessage> createFromFiltered(FilteredText<PlayerChatMessage> filteredText, ChatSender chatSender) {
        return filteredText.map(playerChatMessage -> OutgoingPlayerChatMessage.create((PlayerChatMessage)filteredText.raw(), chatSender), NotTracked::new);
    }

    public static class NotTracked
    implements OutgoingPlayerChatMessage {
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

    public static class Disguised
    implements OutgoingPlayerChatMessage {
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

    public static class Tracked
    implements OutgoingPlayerChatMessage {
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

