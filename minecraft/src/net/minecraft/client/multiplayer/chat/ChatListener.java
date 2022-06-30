package net.minecraft.client.multiplayer.chat;

import com.mojang.authlib.GameProfile;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.util.StringDecomposer;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class ChatListener {
	private final Minecraft minecraft;

	public ChatListener(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void handleChatMessage(ChatType chatType, PlayerChatMessage playerChatMessage, ChatSender chatSender) {
		if (!this.minecraft.isBlocked(chatSender.profileId())) {
			boolean bl = this.minecraft.options.onlyShowSecureChat().get();
			PlayerChatMessage playerChatMessage2 = bl ? playerChatMessage.removeUnsignedContent() : playerChatMessage;
			Component component = chatType.chat().decorate(playerChatMessage2.serverContent(), chatSender);
			PlayerInfo playerInfo = this.resolveSenderPlayer(chatSender);
			ChatTrustLevel chatTrustLevel = this.evaluateTrustLevel(chatSender, playerChatMessage2, component, playerInfo);
			if (!chatTrustLevel.isNotSecure() || !bl) {
				GuiMessageTag guiMessageTag = chatTrustLevel.createTag(playerChatMessage2);
				this.minecraft.gui.getChat().enqueueMessage(component, guiMessageTag);
				this.minecraft.getNarrator().sayChatNow(() -> chatType.narration().decorate(playerChatMessage2.serverContent(), chatSender));
				if (chatSender.isPlayer()) {
					this.logPlayerMessage(playerChatMessage, chatSender, playerInfo, chatTrustLevel);
				} else {
					this.logSystemMessage(component, playerChatMessage.signature().timeStamp());
				}
			}
		}
	}

	private ChatTrustLevel evaluateTrustLevel(ChatSender chatSender, PlayerChatMessage playerChatMessage, Component component, @Nullable PlayerInfo playerInfo) {
		if (chatSender.isPlayer()) {
			return this.isSenderLocalPlayer(chatSender) ? ChatTrustLevel.SECURE : ChatTrustLevel.evaluate(playerChatMessage, component, playerInfo);
		} else {
			return ChatTrustLevel.UNKNOWN;
		}
	}

	private void logPlayerMessage(PlayerChatMessage playerChatMessage, ChatSender chatSender, @Nullable PlayerInfo playerInfo, ChatTrustLevel chatTrustLevel) {
		GameProfile gameProfile;
		if (playerInfo != null) {
			gameProfile = playerInfo.getProfile();
		} else {
			gameProfile = new GameProfile(chatSender.profileId(), chatSender.name().getString());
		}

		ChatLog chatLog = this.minecraft.getReportingContext().chatLog();
		chatLog.push(LoggedChat.player(gameProfile, chatSender.name(), playerChatMessage, chatTrustLevel));
	}

	@Nullable
	private PlayerInfo resolveSenderPlayer(ChatSender chatSender) {
		ClientPacketListener clientPacketListener = this.minecraft.getConnection();
		return clientPacketListener != null ? clientPacketListener.getPlayerInfo(chatSender.profileId()) : null;
	}

	public void handleSystemMessage(Component component, boolean bl) {
		if (!this.minecraft.options.hideMatchedNames().get() || !this.minecraft.isBlocked(this.guessChatUUID(component))) {
			if (bl) {
				this.minecraft.gui.setOverlayMessage(component, false);
			} else {
				this.minecraft.gui.getChat().addMessage(component);
				this.logSystemMessage(component, Instant.now());
			}

			this.minecraft.getNarrator().sayNow(component);
		}
	}

	private UUID guessChatUUID(Component component) {
		String string = StringDecomposer.getPlainText(component);
		String string2 = StringUtils.substringBetween(string, "<", ">");
		return string2 == null ? Util.NIL_UUID : this.minecraft.getPlayerSocialManager().getDiscoveredUUID(string2);
	}

	private void logSystemMessage(Component component, Instant instant) {
		ChatLog chatLog = this.minecraft.getReportingContext().chatLog();
		chatLog.push(LoggedChat.system(component, instant));
	}

	private boolean isSenderLocalPlayer(ChatSender chatSender) {
		if (this.minecraft.isLocalServer() && this.minecraft.player != null) {
			UUID uUID = this.minecraft.player.getGameProfile().getId();
			return uUID.equals(chatSender.profileId());
		} else {
			return false;
		}
	}
}
