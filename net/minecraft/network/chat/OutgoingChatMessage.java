/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;

public interface OutgoingChatMessage {
    public Component content();

    public void sendToPlayer(ServerPlayer var1, boolean var2, ChatType.Bound var3);

    public static OutgoingChatMessage create(PlayerChatMessage playerChatMessage) {
        if (playerChatMessage.isSystem()) {
            return new Disguised(playerChatMessage.decoratedContent());
        }
        return new Player(playerChatMessage);
    }

    public record Disguised(Component content) implements OutgoingChatMessage
    {
        @Override
        public void sendToPlayer(ServerPlayer serverPlayer, boolean bl, ChatType.Bound bound) {
            serverPlayer.connection.sendDisguisedChatMessage(this.content, bound);
        }
    }

    public record Player(PlayerChatMessage message) implements OutgoingChatMessage
    {
        @Override
        public Component content() {
            return this.message.decoratedContent();
        }

        @Override
        public void sendToPlayer(ServerPlayer serverPlayer, boolean bl, ChatType.Bound bound) {
            PlayerChatMessage playerChatMessage = this.message.filter(bl);
            if (!playerChatMessage.isFullyFiltered()) {
                serverPlayer.connection.sendPlayerChatMessage(playerChatMessage, bound);
            }
        }
    }
}

