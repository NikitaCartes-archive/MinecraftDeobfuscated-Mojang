package net.minecraft.network.chat;

import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record PlayerChatMessage(Component signedContent, MessageSignature signature, Optional<Component> unsignedContent) {
	public static PlayerChatMessage signed(Component component, MessageSignature messageSignature) {
		return new PlayerChatMessage(component, messageSignature, Optional.empty());
	}

	public static PlayerChatMessage signed(String string, MessageSignature messageSignature) {
		return signed(Component.literal(string), messageSignature);
	}

	public static PlayerChatMessage signed(Component component, Component component2, MessageSignature messageSignature, boolean bl) {
		if (component.equals(component2)) {
			return signed(component, messageSignature);
		} else {
			return !bl ? signed(component, messageSignature).withUnsignedContent(component2) : signed(component2, messageSignature);
		}
	}

	public static FilteredText<PlayerChatMessage> filteredSigned(
		FilteredText<Component> filteredText, FilteredText<Component> filteredText2, MessageSignature messageSignature, boolean bl
	) {
		Component component = filteredText.raw();
		Component component2 = filteredText2.raw();
		PlayerChatMessage playerChatMessage = signed(component, component2, messageSignature, bl);
		if (filteredText2.isFiltered()) {
			PlayerChatMessage playerChatMessage2 = Util.mapNullable(filteredText2.filtered(), PlayerChatMessage::unsigned);
			return new FilteredText<>(playerChatMessage, playerChatMessage2);
		} else {
			return FilteredText.passThrough(playerChatMessage);
		}
	}

	public static PlayerChatMessage unsigned(Component component) {
		return new PlayerChatMessage(component, MessageSignature.unsigned(), Optional.empty());
	}

	public PlayerChatMessage withUnsignedContent(Component component) {
		return new PlayerChatMessage(this.signedContent, this.signature, Optional.of(component));
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
		return (Component)this.unsignedContent.orElse(this.signedContent);
	}
}
