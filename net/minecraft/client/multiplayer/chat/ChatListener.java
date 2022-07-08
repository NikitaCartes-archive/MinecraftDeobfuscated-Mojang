/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer.chat;

import com.google.common.collect.Queues;
import com.mojang.authlib.GameProfile;
import java.time.Instant;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.ChatTrustLevel;
import net.minecraft.client.multiplayer.chat.LoggedChat;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageHeader;
import net.minecraft.util.StringDecomposer;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChatListener {
    private final Minecraft minecraft;
    private final Deque<Message> delayedMessageQueue = Queues.newArrayDeque();
    private long messageDelay;
    private long previousMessageTime;

    public ChatListener(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void tick() {
        if (this.messageDelay == 0L) {
            return;
        }
        if (Util.getMillis() >= this.previousMessageTime + this.messageDelay) {
            Message message = this.delayedMessageQueue.poll();
            while (message != null && !message.accept()) {
                message = this.delayedMessageQueue.poll();
            }
        }
    }

    public void setMessageDelay(double d) {
        long l = (long)(d * 1000.0);
        if (l == 0L && this.messageDelay > 0L) {
            this.delayedMessageQueue.forEach(Message::accept);
            this.delayedMessageQueue.clear();
        }
        this.messageDelay = l;
    }

    public void acceptNextDelayedMessage() {
        this.delayedMessageQueue.remove().accept();
    }

    public Collection<?> delayedMessageQueue() {
        return this.delayedMessageQueue;
    }

    public boolean removeFromDelayedMessageQueue(MessageSignature messageSignature) {
        Iterator<Message> iterator = this.delayedMessageQueue.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().getHeaderSignature().equals(messageSignature)) continue;
            iterator.remove();
            return true;
        }
        return false;
    }

    private boolean willDelayMessages() {
        return this.messageDelay > 0L && Util.getMillis() < this.previousMessageTime + this.messageDelay;
    }

    private void handleMessage(Message message) {
        if (this.willDelayMessages()) {
            this.delayedMessageQueue.add(message);
        } else {
            message.accept();
        }
    }

    public void handleChatMessage(PlayerChatMessage playerChatMessage, ChatType.Bound bound) {
        boolean bl = this.minecraft.options.onlyShowSecureChat().get();
        PlayerChatMessage playerChatMessage2 = bl ? playerChatMessage.removeUnsignedContent() : playerChatMessage;
        Component component = bound.decorate(playerChatMessage2.serverContent());
        MessageSigner messageSigner = playerChatMessage.signer();
        if (!messageSigner.isSystem()) {
            PlayerInfo playerInfo = this.resolveSenderPlayer(messageSigner.profileId());
            ChatTrustLevel chatTrustLevel = this.evaluateTrustLevel(playerChatMessage, component, playerInfo);
            if (bl && chatTrustLevel.isNotSecure()) {
                return;
            }
            this.handleMessage(new Message(playerChatMessage.headerSignature(), () -> this.processPlayerChatMessage(bound, playerChatMessage, component, playerInfo, chatTrustLevel)));
        } else {
            this.handleMessage(new Message(playerChatMessage.headerSignature(), () -> this.processNonPlayerChatMessage(bound, playerChatMessage2, component)));
        }
    }

    public void handleChatHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
        this.handleMessage(new Message(messageSignature, () -> this.processPlayerChatHeader(signedMessageHeader, messageSignature, bs)));
    }

    private boolean processPlayerChatMessage(ChatType.Bound bound, PlayerChatMessage playerChatMessage, Component component, @Nullable PlayerInfo playerInfo, ChatTrustLevel chatTrustLevel) {
        if (this.minecraft.isBlocked(playerChatMessage.signer().profileId())) {
            return false;
        }
        GuiMessageTag guiMessageTag = chatTrustLevel.createTag(playerChatMessage);
        this.minecraft.gui.getChat().addMessage(component, playerChatMessage.headerSignature(), guiMessageTag);
        this.narrateChatMessage(bound, playerChatMessage);
        this.logPlayerMessage(playerChatMessage, bound, playerInfo, chatTrustLevel);
        this.previousMessageTime = Util.getMillis();
        return true;
    }

    private boolean processNonPlayerChatMessage(ChatType.Bound bound, PlayerChatMessage playerChatMessage, Component component) {
        this.minecraft.gui.getChat().addMessage(component, GuiMessageTag.system());
        this.narrateChatMessage(bound, playerChatMessage);
        this.logSystemMessage(component, playerChatMessage.timeStamp());
        this.previousMessageTime = Util.getMillis();
        return true;
    }

    private boolean processPlayerChatHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
        PlayerInfo playerInfo = this.resolveSenderPlayer(signedMessageHeader.sender());
        if (playerInfo != null) {
            playerInfo.getMessageValidator().validateHeader(signedMessageHeader, messageSignature, bs);
        }
        this.logPlayerHeader(signedMessageHeader, messageSignature, bs);
        return false;
    }

    private void narrateChatMessage(ChatType.Bound bound, PlayerChatMessage playerChatMessage) {
        this.minecraft.getNarrator().sayChatNow(() -> bound.decorateNarration(playerChatMessage.serverContent()));
    }

    private ChatTrustLevel evaluateTrustLevel(PlayerChatMessage playerChatMessage, Component component, @Nullable PlayerInfo playerInfo) {
        if (this.isSenderLocalPlayer(playerChatMessage.signer().profileId())) {
            return ChatTrustLevel.SECURE;
        }
        return ChatTrustLevel.evaluate(playerChatMessage, component, playerInfo);
    }

    private void logPlayerMessage(PlayerChatMessage playerChatMessage, ChatType.Bound bound, @Nullable PlayerInfo playerInfo, ChatTrustLevel chatTrustLevel) {
        GameProfile gameProfile = playerInfo != null ? playerInfo.getProfile() : new GameProfile(playerChatMessage.signer().profileId(), bound.name().getString());
        ChatLog chatLog = this.minecraft.getReportingContext().chatLog();
        chatLog.push(LoggedChat.player(gameProfile, bound.name(), playerChatMessage, chatTrustLevel));
    }

    private void logPlayerHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
    }

    @Nullable
    private PlayerInfo resolveSenderPlayer(UUID uUID) {
        ClientPacketListener clientPacketListener = this.minecraft.getConnection();
        return clientPacketListener != null ? clientPacketListener.getPlayerInfo(uUID) : null;
    }

    public void handleSystemMessage(Component component, boolean bl) {
        if (this.minecraft.options.hideMatchedNames().get().booleanValue() && this.minecraft.isBlocked(this.guessChatUUID(component))) {
            return;
        }
        if (bl) {
            this.minecraft.gui.setOverlayMessage(component, false);
        } else {
            this.minecraft.gui.getChat().addMessage(component, GuiMessageTag.system());
            this.logSystemMessage(component, Instant.now());
        }
        this.minecraft.getNarrator().sayNow(component);
    }

    private UUID guessChatUUID(Component component) {
        String string = StringDecomposer.getPlainText(component);
        String string2 = StringUtils.substringBetween(string, "<", ">");
        if (string2 == null) {
            return Util.NIL_UUID;
        }
        return this.minecraft.getPlayerSocialManager().getDiscoveredUUID(string2);
    }

    private void logSystemMessage(Component component, Instant instant) {
        ChatLog chatLog = this.minecraft.getReportingContext().chatLog();
        chatLog.push(LoggedChat.system(component, instant));
    }

    private boolean isSenderLocalPlayer(UUID uUID) {
        if (this.minecraft.isLocalServer() && this.minecraft.player != null) {
            UUID uUID2 = this.minecraft.player.getGameProfile().getId();
            return uUID2.equals(uUID);
        }
        return false;
    }

    @Environment(value=EnvType.CLIENT)
    record Message(MessageSignature headerSignature, BooleanSupplier processMessage) {
        MessageSignature getHeaderSignature() {
            return this.headerSignature;
        }

        public boolean accept() {
            return this.processMessage.getAsBoolean();
        }
    }
}

