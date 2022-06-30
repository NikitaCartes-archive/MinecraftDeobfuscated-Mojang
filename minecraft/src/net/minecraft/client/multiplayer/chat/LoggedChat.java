package net.minecraft.client.multiplayer.chat;

import com.mojang.authlib.GameProfile;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;

@Environment(EnvType.CLIENT)
public interface LoggedChat {
	static LoggedChat player(GameProfile gameProfile, Component component, PlayerChatMessage playerChatMessage, ChatTrustLevel chatTrustLevel) {
		return new LoggedChat.Player(gameProfile, component, playerChatMessage, chatTrustLevel);
	}

	static LoggedChat system(Component component, Instant instant) {
		return new LoggedChat.System(component, instant);
	}

	Component toContentComponent();

	default Component toNarrationComponent() {
		return this.toContentComponent();
	}

	boolean canReport(UUID uUID);

	@Environment(EnvType.CLIENT)
	public static record Player(GameProfile profile, Component displayName, PlayerChatMessage message, ChatTrustLevel trustLevel) implements LoggedChat {
		private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

		@Override
		public Component toContentComponent() {
			return this.message.serverContent();
		}

		@Override
		public Component toNarrationComponent() {
			Component component = this.message.serverContent();
			Component component2 = this.getTimeComponent();
			return Component.translatable("gui.chatSelection.message.narrate", this.displayName, component, component2);
		}

		public Component toHeadingComponent() {
			Component component = this.getTimeComponent();
			return Component.translatable("gui.chatSelection.heading", this.displayName, component);
		}

		private Component getTimeComponent() {
			LocalDateTime localDateTime = LocalDateTime.ofInstant(this.message.signature().timeStamp(), ZoneOffset.systemDefault());
			return Component.literal(localDateTime.format(TIME_FORMATTER)).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
		}

		@Override
		public boolean canReport(UUID uUID) {
			return this.profileId().equals(uUID);
		}

		public UUID profileId() {
			return this.profile.getId();
		}
	}

	@Environment(EnvType.CLIENT)
	public static record System(Component message, Instant timeStamp) implements LoggedChat {
		@Override
		public Component toContentComponent() {
			return this.message;
		}

		@Override
		public boolean canReport(UUID uUID) {
			return false;
		}
	}

	@Environment(EnvType.CLIENT)
	public static record WithId(int id, LoggedChat message) {
	}
}
