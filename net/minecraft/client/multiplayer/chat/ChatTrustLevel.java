/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer.chat;

import java.time.Instant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public enum ChatTrustLevel {
    SECURE,
    MODIFIED,
    NOT_SECURE;


    public static ChatTrustLevel evaluate(PlayerChatMessage playerChatMessage, Component component, @Nullable PlayerInfo playerInfo, Instant instant) {
        if (playerChatMessage.hasExpiredClient(instant)) {
            return NOT_SECURE;
        }
        if (playerInfo == null || !playerInfo.getMessageValidator().validateMessage(playerChatMessage)) {
            return NOT_SECURE;
        }
        if (playerChatMessage.unsignedContent().isPresent()) {
            return MODIFIED;
        }
        if (!component.contains(playerChatMessage.signedContent().decorated())) {
            return MODIFIED;
        }
        return SECURE;
    }

    public boolean isNotSecure() {
        return this == NOT_SECURE;
    }

    @Nullable
    public GuiMessageTag createTag(PlayerChatMessage playerChatMessage) {
        return switch (this) {
            case MODIFIED -> GuiMessageTag.chatModified(playerChatMessage.signedContent().plain());
            case NOT_SECURE -> GuiMessageTag.chatNotSecure();
            default -> null;
        };
    }
}

