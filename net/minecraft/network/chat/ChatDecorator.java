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

    default public CompletableFuture<FilteredText<Component>> rebuildFiltered(@Nullable ServerPlayer serverPlayer, FilteredText<Component> filteredText, Component component2) {
        return filteredText.rebuildIfNeededAsync(component2, component -> this.decorate(serverPlayer, (Component)component));
    }

    public static FilteredText<PlayerChatMessage> attachUnsignedDecoration(FilteredText<PlayerChatMessage> filteredText, FilteredText<Component> filteredText2) {
        return filteredText.map(playerChatMessage -> playerChatMessage.withUnsignedContent((Component)filteredText2.raw()), playerChatMessage -> filteredText2.filtered() != null ? playerChatMessage.withUnsignedContent((Component)filteredText2.filtered()) : playerChatMessage);
    }
}

