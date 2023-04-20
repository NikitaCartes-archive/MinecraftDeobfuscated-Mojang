package net.minecraft.network.chat;

import com.mojang.authlib.GameProfile;
import java.time.Duration;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record RemoteChatSession(UUID sessionId, ProfilePublicKey profilePublicKey) {
	public SignedMessageValidator createMessageValidator() {
		return new SignedMessageValidator.KeyBased(this.profilePublicKey.createSignatureValidator());
	}

	public SignedMessageChain.Decoder createMessageDecoder(UUID uUID) {
		return new SignedMessageChain(uUID, this.sessionId).decoder(this.profilePublicKey);
	}

	public RemoteChatSession.Data asData() {
		return new RemoteChatSession.Data(this.sessionId, this.profilePublicKey.data());
	}

	public boolean hasExpired() {
		return this.profilePublicKey.data().hasExpired();
	}

	public static record Data(UUID sessionId, ProfilePublicKey.Data profilePublicKey) {
		public static RemoteChatSession.Data read(FriendlyByteBuf friendlyByteBuf) {
			return new RemoteChatSession.Data(friendlyByteBuf.readUUID(), new ProfilePublicKey.Data(friendlyByteBuf));
		}

		public static void write(FriendlyByteBuf friendlyByteBuf, RemoteChatSession.Data data) {
			friendlyByteBuf.writeUUID(data.sessionId);
			data.profilePublicKey.write(friendlyByteBuf);
		}

		public RemoteChatSession validate(GameProfile gameProfile, SignatureValidator signatureValidator, Duration duration) throws ProfilePublicKey.ValidationException {
			return new RemoteChatSession(this.sessionId, ProfilePublicKey.createValidated(signatureValidator, gameProfile.getId(), this.profilePublicKey, duration));
		}
	}
}
