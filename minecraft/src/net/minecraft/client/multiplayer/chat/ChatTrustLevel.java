package net.minecraft.client.multiplayer.chat;

import java.time.Instant;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.world.entity.player.ProfilePublicKey;

@Environment(EnvType.CLIENT)
public enum ChatTrustLevel {
	SECURE,
	MODIFIED,
	NOT_SECURE;

	public static ChatTrustLevel evaluate(PlayerChatMessage playerChatMessage, Component component, @Nullable PlayerInfo playerInfo) {
		if (playerChatMessage.hasExpiredClient(Instant.now())) {
			return NOT_SECURE;
		} else {
			ProfilePublicKey profilePublicKey = Util.mapNullable(playerInfo, PlayerInfo::getProfilePublicKey);
			if (profilePublicKey == null || !playerChatMessage.verify(profilePublicKey)) {
				return NOT_SECURE;
			} else if (playerChatMessage.unsignedContent().isPresent()) {
				return MODIFIED;
			} else {
				return !component.contains(playerChatMessage.signedContent()) ? MODIFIED : SECURE;
			}
		}
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
