package net.minecraft.network.chat;

import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;

@FunctionalInterface
public interface ChatDecorator {
	ChatDecorator PLAIN = (serverPlayer, component) -> CompletableFuture.completedFuture(component);

	CompletableFuture<Component> decorate(@Nullable ServerPlayer serverPlayer, Component component);

	default CompletableFuture<FilteredText<Component>> rebuildFiltered(
		@Nullable ServerPlayer serverPlayer, FilteredText<Component> filteredText, Component component
	) {
		return filteredText.rebuildIfNeededAsync(component, componentx -> this.decorate(serverPlayer, componentx));
	}

	static FilteredText<PlayerChatMessage> attachUnsignedDecoration(FilteredText<PlayerChatMessage> filteredText, FilteredText<Component> filteredText2) {
		return filteredText.map(
			playerChatMessage -> playerChatMessage.withUnsignedContent(filteredText2.raw()),
			playerChatMessage -> filteredText2.filtered() != null ? playerChatMessage.withUnsignedContent(filteredText2.filtered()) : playerChatMessage
		);
	}
}
