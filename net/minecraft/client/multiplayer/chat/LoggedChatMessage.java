/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer.chat;

import com.mojang.authlib.GameProfile;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Objects;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.chat.ChatTrustLevel;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import net.minecraft.client.multiplayer.chat.LoggedChatMessageLink;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageHeader;

@Environment(value=EnvType.CLIENT)
public interface LoggedChatMessage
extends LoggedChatEvent {
    public static Player player(GameProfile gameProfile, Component component, PlayerChatMessage playerChatMessage, ChatTrustLevel chatTrustLevel) {
        return new Player(gameProfile, component, playerChatMessage, chatTrustLevel);
    }

    public static System system(Component component, Instant instant) {
        return new System(component, instant);
    }

    public Component toContentComponent();

    default public Component toNarrationComponent() {
        return this.toContentComponent();
    }

    public boolean canReport(UUID var1);

    @Environment(value=EnvType.CLIENT)
    public record Player(GameProfile profile, Component displayName, PlayerChatMessage message, ChatTrustLevel trustLevel) implements LoggedChatMessage,
    LoggedChatMessageLink
    {
        private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

        @Override
        public Component toContentComponent() {
            if (!this.message.filterMask().isEmpty()) {
                Component component = this.message.filterMask().apply(this.message.signedContent());
                return Objects.requireNonNullElse(component, CommonComponents.EMPTY);
            }
            return this.message.serverContent();
        }

        @Override
        public Component toNarrationComponent() {
            Component component = this.toContentComponent();
            Component component2 = this.getTimeComponent();
            return Component.translatable("gui.chatSelection.message.narrate", this.displayName, component, component2);
        }

        public Component toHeadingComponent() {
            Component component = this.getTimeComponent();
            return Component.translatable("gui.chatSelection.heading", this.displayName, component);
        }

        private Component getTimeComponent() {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(this.message.timeStamp(), ZoneOffset.systemDefault());
            return Component.literal(localDateTime.format(TIME_FORMATTER)).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
        }

        @Override
        public boolean canReport(UUID uUID) {
            return this.message.hasSignatureFrom(uUID);
        }

        @Override
        public SignedMessageHeader header() {
            return this.message.signedHeader();
        }

        @Override
        public byte[] bodyDigest() {
            return this.message.signedBody().hash().asBytes();
        }

        @Override
        public MessageSignature headerSignature() {
            return this.message.headerSignature();
        }

        public UUID profileId() {
            return this.profile.getId();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record System(Component message, Instant timeStamp) implements LoggedChatMessage
    {
        @Override
        public Component toContentComponent() {
            return this.message;
        }

        @Override
        public boolean canReport(UUID uUID) {
            return false;
        }
    }
}

