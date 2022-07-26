package net.minecraft.client.multiplayer.chat;

import com.google.common.collect.Queues;
import com.mojang.authlib.GameProfile;
import java.time.Instant;
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
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageHeader;
import net.minecraft.network.chat.SignedMessageValidator;
import net.minecraft.util.StringDecomposer;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class ChatListener {
	private static final Component CHAT_VALIDATION_FAILED_ERROR = Component.translatable("multiplayer.disconnect.chat_validation_failed");
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

	public long queueSize() {
		return this.delayedMessageQueue.stream().filter(ChatListener.Message::isVisible).count();
	}

	public void clearQueue() {
		this.delayedMessageQueue.forEach(message -> {
			message.remove();
			message.accept();
		});
		this.delayedMessageQueue.clear();
	}

	public boolean removeFromDelayedMessageQueue(MessageSignature messageSignature) {
		for (ChatListener.Message message : this.delayedMessageQueue) {
			if (message.removeIfSignatureMatches(messageSignature)) {
				return true;
			}
		}

		return false;
	}

	private boolean willDelayMessages() {
		return this.messageDelay > 0L && Util.getMillis() < this.previousMessageTime + this.messageDelay;
	}

	private void handleMessage(ChatListener.Message message) {
		if (this.willDelayMessages()) {
			this.delayedMessageQueue.add(message);
		} else {
			message.accept();
		}
	}

	public void handleChatMessage(PlayerChatMessage playerChatMessage, ChatType.Bound bound) {
		final boolean bl = this.minecraft.options.onlyShowSecureChat().get();
		final PlayerChatMessage playerChatMessage2 = bl ? playerChatMessage.removeUnsignedContent() : playerChatMessage;
		final Component component = bound.decorate(playerChatMessage2.serverContent());
		MessageSigner messageSigner = playerChatMessage.signer();
		if (!messageSigner.isSystem()) {
			final PlayerInfo playerInfo = this.resolveSenderPlayer(messageSigner.profileId());
			final Instant instant = Instant.now();
			this.handleMessage(new ChatListener.Message() {
				private boolean removed;

				@Override
				public boolean accept() {
					if (this.removed) {
						byte[] bs = playerChatMessage.signedBody().hash().asBytes();
						ChatListener.this.processPlayerChatHeader(playerChatMessage.signedHeader(), playerChatMessage.headerSignature(), bs);
						return false;
					} else {
						return ChatListener.this.processPlayerChatMessage(bound, playerChatMessage, component, playerInfo, bl, instant);
					}
				}

				@Override
				public boolean removeIfSignatureMatches(MessageSignature messageSignature) {
					if (playerChatMessage.headerSignature().equals(messageSignature)) {
						this.removed = true;
						return true;
					} else {
						return false;
					}
				}

				@Override
				public void remove() {
					this.removed = true;
				}

				@Override
				public boolean isVisible() {
					return !this.removed;
				}
			});
		} else {
			this.handleMessage(new ChatListener.Message() {
				@Override
				public boolean accept() {
					return ChatListener.this.processNonPlayerChatMessage(bound, playerChatMessage2, component);
				}

				@Override
				public boolean isVisible() {
					return true;
				}
			});
		}
	}

	public void handleChatHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
		this.handleMessage(() -> this.processPlayerChatHeader(signedMessageHeader, messageSignature, bs));
	}

	boolean processPlayerChatMessage(
		ChatType.Bound bound, PlayerChatMessage playerChatMessage, Component component, @Nullable PlayerInfo playerInfo, boolean bl, Instant instant
	) {
		boolean bl2 = this.showMessageToPlayer(bound, playerChatMessage, component, playerInfo, bl, instant);
		ClientPacketListener clientPacketListener = this.minecraft.getConnection();
		if (clientPacketListener != null) {
			clientPacketListener.markMessageAsProcessed(playerChatMessage, bl2);
		}

		return bl2;
	}

	private boolean showMessageToPlayer(
		ChatType.Bound bound, PlayerChatMessage playerChatMessage, Component component, @Nullable PlayerInfo playerInfo, boolean bl, Instant instant
	) {
		ChatTrustLevel chatTrustLevel = this.evaluateTrustLevel(playerChatMessage, component, playerInfo, instant);
		if (chatTrustLevel == ChatTrustLevel.BROKEN_CHAIN) {
			this.onChatChainBroken();
			return true;
		} else if (bl && chatTrustLevel.isNotSecure()) {
			return false;
		} else if (!this.minecraft.isBlocked(playerChatMessage.signer().profileId()) && !playerChatMessage.isFullyFiltered()) {
			GuiMessageTag guiMessageTag = chatTrustLevel.createTag(playerChatMessage);
			MessageSignature messageSignature = playerChatMessage.headerSignature();
			FilterMask filterMask = playerChatMessage.filterMask();
			if (filterMask.isEmpty()) {
				this.minecraft.gui.getChat().addMessage(component, messageSignature, guiMessageTag);
				this.narrateChatMessage(bound, playerChatMessage.serverContent());
			} else {
				Component component2 = filterMask.apply(playerChatMessage.signedContent());
				if (component2 != null) {
					this.minecraft.gui.getChat().addMessage(bound.decorate(component2), messageSignature, guiMessageTag);
					this.narrateChatMessage(bound, component2);
				}
			}

			this.logPlayerMessage(playerChatMessage, bound, playerInfo, chatTrustLevel);
			this.previousMessageTime = Util.getMillis();
			return true;
		} else {
			return false;
		}
	}

	boolean processNonPlayerChatMessage(ChatType.Bound bound, PlayerChatMessage playerChatMessage, Component component) {
		this.minecraft.gui.getChat().addMessage(component);
		this.narrateChatMessage(bound, playerChatMessage.serverContent());
		this.logSystemMessage(component, playerChatMessage.timeStamp());
		this.previousMessageTime = Util.getMillis();
		return true;
	}

	boolean processPlayerChatHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
		PlayerInfo playerInfo = this.resolveSenderPlayer(signedMessageHeader.sender());
		if (playerInfo != null) {
			SignedMessageValidator.State state = playerInfo.getMessageValidator().validateHeader(signedMessageHeader, messageSignature, bs);
			if (state == SignedMessageValidator.State.BROKEN_CHAIN) {
				this.onChatChainBroken();
				return true;
			}
		}

		this.logPlayerHeader(signedMessageHeader, messageSignature, bs);
		return false;
	}

	private void onChatChainBroken() {
		ClientPacketListener clientPacketListener = this.minecraft.getConnection();
		if (clientPacketListener != null) {
			clientPacketListener.getConnection().disconnect(CHAT_VALIDATION_FAILED_ERROR);
		}
	}

	private void narrateChatMessage(ChatType.Bound bound, Component component) {
		this.minecraft.getNarrator().sayChatNow(() -> bound.decorateNarration(component));
	}

	private ChatTrustLevel evaluateTrustLevel(PlayerChatMessage playerChatMessage, Component component, @Nullable PlayerInfo playerInfo, Instant instant) {
		return this.isSenderLocalPlayer(playerChatMessage.signer().profileId())
			? ChatTrustLevel.SECURE
			: ChatTrustLevel.evaluate(playerChatMessage, component, playerInfo, instant);
	}

	private void logPlayerMessage(PlayerChatMessage playerChatMessage, ChatType.Bound bound, @Nullable PlayerInfo playerInfo, ChatTrustLevel chatTrustLevel) {
		GameProfile gameProfile;
		if (playerInfo != null) {
			gameProfile = playerInfo.getProfile();
		} else {
			gameProfile = new GameProfile(playerChatMessage.signer().profileId(), bound.name().getString());
		}

		ChatLog chatLog = this.minecraft.getReportingContext().chatLog();
		chatLog.push(LoggedChatMessage.player(gameProfile, bound.name(), playerChatMessage, chatTrustLevel));
	}

	private void logSystemMessage(Component component, Instant instant) {
		ChatLog chatLog = this.minecraft.getReportingContext().chatLog();
		chatLog.push(LoggedChatMessage.system(component, instant));
	}

	private void logPlayerHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
		ChatLog chatLog = this.minecraft.getReportingContext().chatLog();
		chatLog.push(LoggedChatMessageLink.header(signedMessageHeader, messageSignature, bs));
	}

	@Nullable
	private PlayerInfo resolveSenderPlayer(UUID uUID) {
		ClientPacketListener clientPacketListener = this.minecraft.getConnection();
		return clientPacketListener != null ? clientPacketListener.getPlayerInfo(uUID) : null;
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

	private boolean isSenderLocalPlayer(UUID uUID) {
		if (this.minecraft.isLocalServer() && this.minecraft.player != null) {
			UUID uUID2 = this.minecraft.player.getGameProfile().getId();
			return uUID2.equals(uUID);
		} else {
			return false;
		}
	}

	@Environment(EnvType.CLIENT)
	interface Message {
		default boolean removeIfSignatureMatches(MessageSignature messageSignature) {
			return false;
		}

		default void remove() {
		}

		boolean accept();

		default boolean isVisible() {
			return false;
		}
	}
}
