package net.minecraft.client.multiplayer.chat;

import java.time.Instant;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageValidator;

@Environment(EnvType.CLIENT)
public enum ChatTrustLevel {
	SECURE,
	MODIFIED,
	NOT_SECURE,
	BROKEN_CHAIN;

	public static ChatTrustLevel evaluate(PlayerChatMessage playerChatMessage, Component component, @Nullable PlayerInfo playerInfo, Instant instant) {
		if (playerInfo != null && !playerChatMessage.hasExpiredClient(instant)) {
			SignedMessageValidator.State state = playerInfo.getMessageValidator().validateMessage(playerChatMessage);
			if (state == SignedMessageValidator.State.BROKEN_CHAIN) {
				return BROKEN_CHAIN;
			} else if (state == SignedMessageValidator.State.NOT_SECURE) {
				return NOT_SECURE;
			} else if (playerChatMessage.unsignedContent().isPresent()) {
				return MODIFIED;
			} else {
				return !component.contains(playerChatMessage.signedContent().decorated()) ? MODIFIED : SECURE;
			}
		} else {
			return NOT_SECURE;
		}
	}

	public boolean isNotSecure() {
		return this == NOT_SECURE || this == BROKEN_CHAIN;
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
