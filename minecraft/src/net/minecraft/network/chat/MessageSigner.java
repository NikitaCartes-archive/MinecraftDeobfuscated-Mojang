package net.minecraft.network.chat;

import java.security.SignatureException;
import java.time.Instant;
import java.util.UUID;
import net.minecraft.util.Crypt;
import net.minecraft.util.Signer;

public record MessageSigner(UUID sender, Instant timeStamp, long salt) {
	public static MessageSigner create(UUID uUID) {
		return new MessageSigner(uUID, Instant.now(), Crypt.SaltSupplier.getLong());
	}

	public MessageSignature sign(Signer signer, Component component) {
		byte[] bs = signer.sign(output -> MessageSignature.updateSignature(output, component, this.sender, this.timeStamp, this.salt));
		return new MessageSignature(this.sender, this.timeStamp, new Crypt.SaltSignaturePair(this.salt, bs));
	}

	public MessageSignature sign(Signer signer, String string) throws SignatureException {
		return this.sign(signer, Component.literal(string));
	}
}
