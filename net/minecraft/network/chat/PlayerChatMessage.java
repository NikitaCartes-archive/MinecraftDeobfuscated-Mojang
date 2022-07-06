/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record PlayerChatMessage(Component signedContent, MessageSignature signature, Optional<Component> unsignedContent) {
    public static final Duration MESSAGE_EXPIRES_AFTER_SERVER = Duration.ofMinutes(5L);
    public static final Duration MESSAGE_EXPIRES_AFTER_CLIENT = MESSAGE_EXPIRES_AFTER_SERVER.plus(Duration.ofMinutes(2L));

    public static PlayerChatMessage signed(Component component, MessageSignature messageSignature) {
        return new PlayerChatMessage(component, messageSignature, Optional.empty());
    }

    public static PlayerChatMessage signed(String string, MessageSignature messageSignature) {
        return PlayerChatMessage.signed(Component.literal(string), messageSignature);
    }

    public static PlayerChatMessage signed(Component component, Component component2, MessageSignature messageSignature, boolean bl) {
        if (component.equals(component2)) {
            return PlayerChatMessage.signed(component, messageSignature);
        }
        if (!bl) {
            return PlayerChatMessage.signed(component, messageSignature).withUnsignedContent(component2);
        }
        return PlayerChatMessage.signed(component2, messageSignature);
    }

    public static FilteredText<PlayerChatMessage> filteredSigned(FilteredText<Component> filteredText, FilteredText<Component> filteredText2, MessageSignature messageSignature, boolean bl) {
        Component component2 = filteredText.raw();
        Component component22 = filteredText2.raw();
        PlayerChatMessage playerChatMessage = PlayerChatMessage.signed(component2, component22, messageSignature, bl);
        if (filteredText2.isFiltered()) {
            UUID uUID = messageSignature.sender();
            PlayerChatMessage playerChatMessage2 = Util.mapNullable(filteredText2.filtered(), component -> PlayerChatMessage.unsigned(uUID, component));
            return new FilteredText<PlayerChatMessage>(playerChatMessage, playerChatMessage2);
        }
        return FilteredText.passThrough(playerChatMessage);
    }

    public static PlayerChatMessage unsigned(UUID uUID, Component component) {
        return new PlayerChatMessage(component, MessageSignature.unsigned(uUID), Optional.empty());
    }

    public PlayerChatMessage withUnsignedContent(Component component) {
        return new PlayerChatMessage(this.signedContent, this.signature, Optional.of(component));
    }

    public PlayerChatMessage removeUnsignedContent() {
        if (this.unsignedContent.isPresent()) {
            return new PlayerChatMessage(this.signedContent, this.signature, Optional.empty());
        }
        return this;
    }

    public boolean verify(ProfilePublicKey profilePublicKey) {
        return this.signature.verify(profilePublicKey.createSignatureValidator(), this.signedContent);
    }

    public boolean verify(ServerPlayer serverPlayer) {
        ProfilePublicKey profilePublicKey = serverPlayer.getProfilePublicKey();
        return profilePublicKey == null || this.verify(profilePublicKey);
    }

    public boolean verify(CommandSourceStack commandSourceStack) {
        ServerPlayer serverPlayer = commandSourceStack.getPlayer();
        return serverPlayer == null || this.verify(serverPlayer);
    }

    public Component serverContent() {
        return this.unsignedContent.orElse(this.signedContent);
    }

    public boolean hasExpiredServer(Instant instant) {
        return instant.isAfter(this.signature.timeStamp().plus(MESSAGE_EXPIRES_AFTER_SERVER));
    }

    public boolean hasExpiredClient(Instant instant) {
        return instant.isAfter(this.signature.timeStamp().plus(MESSAGE_EXPIRES_AFTER_CLIENT));
    }

    public boolean isSignedBy(ChatSender chatSender) {
        return chatSender.isPlayer() && this.signature.sender().equals(chatSender.profileId());
    }
}

