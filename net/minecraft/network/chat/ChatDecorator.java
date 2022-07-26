/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import java.util.concurrent.CompletableFuture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ChatDecorator {
    public static final ChatDecorator PLAIN = (serverPlayer, component) -> CompletableFuture.completedFuture(component);

    public CompletableFuture<Component> decorate(@Nullable ServerPlayer var1, Component var2);

    default public CompletableFuture<PlayerChatMessage> decorate(@Nullable ServerPlayer serverPlayer, PlayerChatMessage playerChatMessage) {
        if (playerChatMessage.signedContent().isDecorated()) {
            return CompletableFuture.completedFuture(playerChatMessage);
        }
        return this.decorate(serverPlayer, playerChatMessage.serverContent()).thenApply(playerChatMessage::withUnsignedContent);
    }

    public static PlayerChatMessage attachIfNotDecorated(PlayerChatMessage playerChatMessage, Component component) {
        if (!playerChatMessage.signedContent().isDecorated()) {
            return playerChatMessage.withUnsignedContent(component);
        }
        return playerChatMessage;
    }
}

