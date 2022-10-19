package net.minecraft.client.multiplayer.chat;

import java.time.Instant;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;

@Environment(EnvType.CLIENT)
public enum ChatTrustLevel {
	SECURE,
	MODIFIED,
	NOT_SECURE;

	public static ChatTrustLevel evaluate(PlayerChatMessage playerChatMessage, Component component, Instant instant) {
		if (!playerChatMessage.hasSignature() || playerChatMessage.hasExpiredClient(instant)) {
			return NOT_SECURE;
		} else {
			return isModified(playerChatMessage, component) ? MODIFIED : SECURE;
		}
	}

	private static boolean isModified(PlayerChatMessage playerChatMessage, Component component) {
		if (!component.getString().contains(playerChatMessage.signedContent())) {
			return true;
		} else {
			Component component2 = playerChatMessage.unsignedContent();
			return component2 == null ? false : containsModifiedStyle(component2);
		}
	}

	private static boolean containsModifiedStyle(Component component) {
		return (Boolean)component.visit((style, string) -> isModifiedStyle(style) ? Optional.of(true) : Optional.empty(), Style.EMPTY).orElse(false);
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
