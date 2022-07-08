/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import java.util.concurrent.CompletableFuture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ChatDecorator {
    public static final ChatDecorator PLAIN = (serverPlayer, component) -> CompletableFuture.completedFuture(component);

    public CompletableFuture<Component> decorate(@Nullable ServerPlayer var1, Component var2);

    default public CompletableFuture<FilteredText<Component>> decorateFiltered(@Nullable ServerPlayer serverPlayer, FilteredText<Component> filteredText) {
        CompletableFuture<Component> completableFuture = this.decorate(serverPlayer, filteredText.raw());
        if (filteredText.filtered() == null) {
            return completableFuture.thenApply(FilteredText::fullyFiltered);
        }
        if (!filteredText.isFiltered()) {
            return completableFuture.thenApply(FilteredText::passThrough);
        }
        CompletableFuture<Component> completableFuture2 = this.decorate(serverPlayer, filteredText.filtered());
        return CompletableFuture.allOf(completableFuture, completableFuture2).thenApply(void_ -> new FilteredText<Component>((Component)completableFuture.join(), (Component)completableFuture2.join()));
    }

    default public CompletableFuture<FilteredText<PlayerChatMessage>> decorateSignedChat(@Nullable ServerPlayer serverPlayer, FilteredText<PlayerChatMessage> filteredText) {
        FilteredText<Component> filteredText22 = filteredText.map(PlayerChatMessage::signedContent);
        return this.decorateFiltered(serverPlayer, filteredText22).thenApply(filteredText2 -> ChatDecorator.attachDecoration(filteredText, filteredText2));
    }

    public static FilteredText<PlayerChatMessage> attachDecoration(FilteredText<PlayerChatMessage> filteredText, FilteredText<Component> filteredText2) {
        return filteredText.map(playerChatMessage -> playerChatMessage.withDecoratedContent((Component)filteredText2.raw()), playerChatMessage -> filteredText2.filtered() != null ? playerChatMessage.withDecoratedContent((Component)filteredText2.filtered()) : playerChatMessage);
    }
}

