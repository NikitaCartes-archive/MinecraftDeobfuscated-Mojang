package net.minecraft.network.chat;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.Signer;
import net.minecraft.world.entity.player.ProfileKeyPair;

public record LocalChatSession(UUID sessionId, @Nullable ProfileKeyPair keyPair) {
	public static LocalChatSession create(@Nullable ProfileKeyPair profileKeyPair) {
		return new LocalChatSession(UUID.randomUUID(), profileKeyPair);
	}

	public SignedMessageChain.Encoder createMessageEncoder(UUID uUID) {
		Signer signer = this.createSigner();
		return signer != null ? new SignedMessageChain(uUID, this.sessionId).encoder(signer) : SignedMessageChain.Encoder.UNSIGNED;
	}

	@Nullable
	public Signer createSigner() {
		return this.keyPair != null ? Signer.from(this.keyPair.privateKey(), "SHA256withRSA") : null;
	}

	public RemoteChatSession asRemote() {
		return new RemoteChatSession(this.sessionId, Util.mapNullable(this.keyPair, ProfileKeyPair::publicKey));
	}
}
