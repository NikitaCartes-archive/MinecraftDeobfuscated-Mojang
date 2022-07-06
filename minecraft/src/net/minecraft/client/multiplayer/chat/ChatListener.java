package net.minecraft.client.multiplayer.chat;

import com.google.common.collect.Queues;
import com.mojang.authlib.GameProfile;
import java.time.Instant;
import java.util.Collection;
import java.util.Deque;
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
	private final Deque<ChatListener.Message> delayedMessageQueue = Queues.<ChatListener.Message>newArrayDeque();
	private long messageDelay;
	private long previousMessageTime;

	public ChatListener(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void tick() {
		if (this.messageDelay != 0L) {
			if (Util.getMillis() >= this.previousMessageTime + this.messageDelay) {
				ChatListener.Message message = (ChatListener.Message)this.delayedMessageQueue.poll();

				while (message != null && !message.accept()) {
					message = (ChatListener.Message)this.delayedMessageQueue.poll();
				}
			}
		}
	}

	public void setMessageDelay(double d) {
		long l = (long)(d * 1000.0);
		if (l == 0L && this.messageDelay > 0L) {
			this.delayedMessageQueue.forEach(ChatListener.Message::accept);
			this.delayedMessageQueue.clear();
		}

		this.messageDelay = l;
	}

	public void acceptNextDelayedMessage() {
		((ChatListener.Message)this.delayedMessageQueue.remove()).accept();
	}

	public Collection<?> delayedMessageQueue() {
		return this.delayedMessageQueue;
	}

	private boolean willDelayMessages() {
		return this.messageDelay > 0L && Util.getMillis() < this.previousMessageTime + this.messageDelay;
	}

	public void handleChatMessage(ChatType chatType, PlayerChatMessage playerChatMessage, ChatSender chatSender) {
		boolean bl = this.minecraft.options.onlyShowSecureChat().get();
		PlayerChatMessage playerChatMessage2 = bl ? playerChatMessage.removeUnsignedContent() : playerChatMessage;
		Component component = chatType.chat().decorate(playerChatMessage2.serverContent(), chatSender);
		if (chatSender.isPlayer()) {
			PlayerInfo playerInfo = this.resolveSenderPlayer(chatSender);
			ChatTrustLevel chatTrustLevel = this.evaluateTrustLevel(chatSender, playerChatMessage, component, playerInfo);
			if (bl && chatTrustLevel.isNotSecure()) {
				return;
			}

			if (this.willDelayMessages()) {
				this.delayedMessageQueue
					.add((ChatListener.Message)() -> this.processPlayerChatMessage(chatType, chatSender, playerChatMessage, component, playerInfo, chatTrustLevel));
				return;
			}

			this.processPlayerChatMessage(chatType, chatSender, playerChatMessage, component, playerInfo, chatTrustLevel);
		} else {
			if (this.willDelayMessages()) {
				this.delayedMessageQueue.add((ChatListener.Message)() -> this.processNonPlayerChatMessage(chatType, chatSender, playerChatMessage2, component));
				return;
			}

			this.processNonPlayerChatMessage(chatType, chatSender, playerChatMessage2, component);
		}
	}

	private boolean processPlayerChatMessage(
		ChatType chatType,
		ChatSender chatSender,
		PlayerChatMessage playerChatMessage,
		Component component,
		@Nullable PlayerInfo playerInfo,
		ChatTrustLevel chatTrustLevel
	) {
		if (this.minecraft.isBlocked(chatSender.profileId())) {
			return false;
		} else {
			GuiMessageTag guiMessageTag = chatTrustLevel.createTag(playerChatMessage);
			this.minecraft.gui.getChat().addMessage(component, guiMessageTag);
			this.narrateChatMessage(chatType, playerChatMessage, chatSender);
			this.logPlayerMessage(playerChatMessage, chatSender, playerInfo, chatTrustLevel);
			this.previousMessageTime = Util.getMillis();
			return true;
		}
	}

	private boolean processNonPlayerChatMessage(ChatType chatType, ChatSender chatSender, PlayerChatMessage playerChatMessage, Component component) {
		this.minecraft.gui.getChat().addMessage(component, GuiMessageTag.system());
		this.narrateChatMessage(chatType, playerChatMessage, chatSender);
		this.logSystemMessage(component, playerChatMessage.signature().timeStamp());
		this.previousMessageTime = Util.getMillis();
		return true;
	}

	private void narrateChatMessage(ChatType chatType, PlayerChatMessage playerChatMessage, ChatSender chatSender) {
		this.minecraft.getNarrator().sayChatNow(() -> chatType.narration().decorate(playerChatMessage.serverContent(), chatSender));
	}

	private ChatTrustLevel evaluateTrustLevel(ChatSender chatSender, PlayerChatMessage playerChatMessage, Component component, @Nullable PlayerInfo playerInfo) {
		return this.isSenderLocalPlayer(chatSender) ? ChatTrustLevel.SECURE : ChatTrustLevel.evaluate(playerChatMessage, component, playerInfo);
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
				this.minecraft.gui.getChat().addMessage(component, GuiMessageTag.system());
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

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	interface Message {
		boolean accept();
	}
}
