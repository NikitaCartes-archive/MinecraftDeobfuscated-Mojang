package net.minecraft.network.chat;

import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface ChatDecorator {
	ChatDecorator PLAIN = (serverPlayer, component) -> CompletableFuture.completedFuture(component);

	CompletableFuture<Component> decorate(@Nullable ServerPlayer serverPlayer, Component component);

	default CompletableFuture<PlayerChatMessage> decorate(@Nullable ServerPlayer serverPlayer, PlayerChatMessage playerChatMessage) {
		return playerChatMessage.signedContent().isDecorated()
			? CompletableFuture.completedFuture(playerChatMessage)
			: this.decorate(serverPlayer, playerChatMessage.serverContent()).thenApply(playerChatMessage::withUnsignedContent);
	}

	static PlayerChatMessage attachIfNotDecorated(PlayerChatMessage playerChatMessage, Component component) {
		return !playerChatMessage.signedContent().isDecorated() ? playerChatMessage.withUnsignedContent(component) : playerChatMessage;
	}
}
