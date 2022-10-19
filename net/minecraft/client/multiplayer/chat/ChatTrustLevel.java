/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer.chat;

import java.time.Instant;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public enum ChatTrustLevel {
    SECURE,
    MODIFIED,
    NOT_SECURE;


    public static ChatTrustLevel evaluate(PlayerChatMessage playerChatMessage, Component component, Instant instant) {
        if (!playerChatMessage.hasSignature() || playerChatMessage.hasExpiredClient(instant)) {
            return NOT_SECURE;
        }
        if (ChatTrustLevel.isModified(playerChatMessage, component)) {
            return MODIFIED;
        }
        return SECURE;
    }

    private static boolean isModified(PlayerChatMessage playerChatMessage, Component component) {
        if (!component.getString().contains(playerChatMessage.signedContent())) {
            return true;
        }
        Component component2 = playerChatMessage.unsignedContent();
        if (component2 == null) {
            return false;
        }
        return ChatTrustLevel.containsModifiedStyle(component2);
    }

    private static boolean containsModifiedStyle(Component component) {
        return component.visit((style, string) -> {
            if (ChatTrustLevel.isModifiedStyle(style)) {
                return Optional.of(true);
            }
            return Optional.empty();
        }, Style.EMPTY).orElse(false);
    }

    private static boolean isModifiedStyle(Style style) {
        return !style.getFont().equals(Style.DEFAULT_FONT);
    }

    public boolean isNotSecure() {
        return this == NOT_SECURE;
    }

    @Nullable
    public GuiMessageTag createTag(PlayerChatMessage playerChatMessage) {
        return switch (this) {
            case MODIFIED -> GuiMessageTag.chatModified(playerChatMessage.signedContent());
            case NOT_SECURE -> GuiMessageTag.chatNotSecure();
            default -> null;
        };
    }
}

