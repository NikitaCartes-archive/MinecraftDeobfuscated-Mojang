package net.minecraft.network.chat;

import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;

@FunctionalInterface
public interface ChatDecorator {
	ChatDecorator PLAIN = (serverPlayer, component) -> CompletableFuture.completedFuture(component);

	CompletableFuture<Component> decorate(@Nullable ServerPlayer serverPlayer, Component component);

	default CompletableFuture<FilteredText<Component>> decorateFiltered(@Nullable ServerPlayer serverPlayer, FilteredText<Component> filteredText) {
		CompletableFuture<Component> completableFuture = this.decorate(serverPlayer, filteredText.raw());
		if (filteredText.filtered() == null) {
			return completableFuture.thenApply(FilteredText::fullyFiltered);
		} else if (!filteredText.isFiltered()) {
			return completableFuture.thenApply(FilteredText::passThrough);
		} else {
			CompletableFuture<Component> completableFuture2 = this.decorate(serverPlayer, filteredText.filtered());
			return CompletableFuture.allOf(completableFuture, completableFuture2)
				.thenApply(void_ -> new FilteredText<>((Component)completableFuture.join(), (Component)completableFuture2.join()));
		}
	}

	static FilteredText<PlayerChatMessage> attachUnsignedDecoration(FilteredText<PlayerChatMessage> filteredText, FilteredText<Component> filteredText2) {
		return filteredText.map(
			playerChatMessage -> playerChatMessage.withUnsignedContent(filteredText2.raw()),
			playerChatMessage -> filteredText2.filtered() != null ? playerChatMessage.withUnsignedContent(filteredText2.filtered()) : playerChatMessage
		);
	}
}
