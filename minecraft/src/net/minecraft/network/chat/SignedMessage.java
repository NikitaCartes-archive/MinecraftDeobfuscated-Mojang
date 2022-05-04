package net.minecraft.network.chat;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.SignatureException;
import net.minecraft.util.CryptException;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record SignedMessage(Component content, MessageSignature signature) {
	public SignedMessage(String string, MessageSignature messageSignature) {
		this(Component.literal(string), messageSignature);
	}

	public boolean verify(Signature signature) throws SignatureException {
		return this.signature.verify(signature, this.content);
	}

	public boolean verify(ProfilePublicKey profilePublicKey) {
		try {
			return this.verify(profilePublicKey.verifySignature());
		} catch (CryptException | GeneralSecurityException var3) {
			return false;
		}
	}
}
