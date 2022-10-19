package net.minecraft.network.chat;

import com.mojang.authlib.GameProfile;
import java.time.Duration;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record RemoteChatSession(UUID sessionId, @Nullable ProfilePublicKey profilePublicKey) {
	public static final RemoteChatSession UNVERIFIED = new RemoteChatSession(Util.NIL_UUID, null);

	public SignedMessageValidator createMessageValidator() {
		return (SignedMessageValidator)(this.profilePublicKey != null
			? new SignedMessageValidator.KeyBased(this.profilePublicKey.createSignatureValidator())
			: SignedMessageValidator.ACCEPT_UNSIGNED);
	}

	public SignedMessageChain.Decoder createMessageDecoder(UUID uUID) {
		return this.profilePublicKey != null
			? new SignedMessageChain(uUID, this.sessionId).decoder(this.profilePublicKey)
			: SignedMessageChain.Decoder.unsigned(uUID);
	}

	public RemoteChatSession.Data asData() {
		return new RemoteChatSession.Data(this.sessionId, Util.mapNullable(this.profilePublicKey, ProfilePublicKey::data));
	}

	public boolean verifiable() {
		return this.profilePublicKey != null;
	}

	public static record Data(UUID sessionId, @Nullable ProfilePublicKey.Data profilePublicKey) {
		public static final RemoteChatSession.Data UNVERIFIED = RemoteChatSession.UNVERIFIED.asData();

		public static RemoteChatSession.Data read(FriendlyByteBuf friendlyByteBuf) {
			return new RemoteChatSession.Data(friendlyByteBuf.readUUID(), friendlyByteBuf.readNullable(ProfilePublicKey.Data::new));
		}

		public static void write(FriendlyByteBuf friendlyByteBuf, RemoteChatSession.Data data) {
			friendlyByteBuf.writeUUID(data.sessionId);
			friendlyByteBuf.writeNullable(data.profilePublicKey, (friendlyByteBufx, datax) -> datax.write(friendlyByteBufx));
		}

		public RemoteChatSession validate(GameProfile gameProfile, SignatureValidator signatureValidator, Duration duration) throws ProfilePublicKey.ValidationException {
			return this.profilePublicKey == null
				? RemoteChatSession.UNVERIFIED
				: new RemoteChatSession(this.sessionId, ProfilePublicKey.createValidated(signatureValidator, gameProfile.getId(), this.profilePublicKey, duration));
		}
	}
}
