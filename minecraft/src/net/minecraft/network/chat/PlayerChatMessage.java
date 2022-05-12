package net.minecraft.network.chat;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Optional;
import net.minecraft.util.CryptException;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record PlayerChatMessage(Component signedContent, MessageSignature signature, Optional<Component> unsignedContent) {
	public static PlayerChatMessage signed(Component component, MessageSignature messageSignature) {
		return new PlayerChatMessage(component, messageSignature, Optional.empty());
	}

	public static PlayerChatMessage signed(String string, MessageSignature messageSignature) {
		return signed(Component.literal(string), messageSignature);
	}

	public static PlayerChatMessage unsigned(Component component) {
		return new PlayerChatMessage(component, MessageSignature.unsigned(), Optional.empty());
	}

	public PlayerChatMessage withUnsignedContent(Component component) {
		return new PlayerChatMessage(this.signedContent, this.signature, Optional.of(component));
	}

	public boolean verify(Signature signature) throws SignatureException {
		return this.signature.verify(signature, this.signedContent);
	}

	public boolean verify(ProfilePublicKey profilePublicKey) {
		if (!this.signature.isValid()) {
			return false;
		} else {
			try {
				return this.verify(profilePublicKey.verifySignature());
			} catch (CryptException | GeneralSecurityException var3) {
				return false;
			}
		}
	}

	public Component serverContent() {
		return (Component)this.unsignedContent.orElse(this.signedContent);
	}
}
