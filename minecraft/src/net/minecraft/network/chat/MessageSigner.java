package net.minecraft.network.chat;

import java.security.Signature;
import java.security.SignatureException;
import java.time.Instant;
import java.util.UUID;
import net.minecraft.util.Crypt;

public record MessageSigner(UUID sender, Instant timeStamp, long salt) {
	public static MessageSigner create(UUID uUID) {
		return new MessageSigner(uUID, Instant.now(), Crypt.SaltSupplier.getLong());
	}

	public MessageSignature sign(Signature signature, Component component) throws SignatureException {
		MessageSignature.updateSignature(signature, component, this.sender, this.timeStamp, this.salt);
		return new MessageSignature(this.sender, this.timeStamp, new Crypt.SaltSignaturePair(this.salt, signature.sign()));
	}

	public MessageSignature sign(Signature signature, String string) throws SignatureException {
		return this.sign(signature, Component.literal(string));
	}
}
