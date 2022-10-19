/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.mojang.authlib.GameProfile;
import java.time.Duration;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.chat.SignedMessageValidator;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.jetbrains.annotations.Nullable;

public record RemoteChatSession(UUID sessionId, @Nullable ProfilePublicKey profilePublicKey) {
    public static final RemoteChatSession UNVERIFIED = new RemoteChatSession(Util.NIL_UUID, null);

    public SignedMessageValidator createMessageValidator() {
        if (this.profilePublicKey != null) {
            return new SignedMessageValidator.KeyBased(this.profilePublicKey.createSignatureValidator());
        }
        return SignedMessageValidator.ACCEPT_UNSIGNED;
    }

    public SignedMessageChain.Decoder createMessageDecoder(UUID uUID) {
        if (this.profilePublicKey != null) {
            return new SignedMessageChain(uUID, this.sessionId).decoder(this.profilePublicKey);
        }
        return SignedMessageChain.Decoder.unsigned(uUID);
    }

    public Data asData() {
        return new Data(this.sessionId, Util.mapNullable(this.profilePublicKey, ProfilePublicKey::data));
    }

    public boolean verifiable() {
        return this.profilePublicKey != null;
    }

    @Nullable
    public ProfilePublicKey profilePublicKey() {
        return this.profilePublicKey;
    }

    public record Data(UUID sessionId, @Nullable ProfilePublicKey.Data profilePublicKey) {
        public static final Data UNVERIFIED = UNVERIFIED.asData();

        public static Data read(FriendlyByteBuf friendlyByteBuf) {
            return new Data(friendlyByteBuf.readUUID(), (ProfilePublicKey.Data)friendlyByteBuf.readNullable(ProfilePublicKey.Data::new));
        }

        public static void write(FriendlyByteBuf friendlyByteBuf2, Data data2) {
            friendlyByteBuf2.writeUUID(data2.sessionId);
            friendlyByteBuf2.writeNullable(data2.profilePublicKey, (friendlyByteBuf, data) -> data.write((FriendlyByteBuf)friendlyByteBuf));
        }

        public RemoteChatSession validate(GameProfile gameProfile, SignatureValidator signatureValidator, Duration duration) throws ProfilePublicKey.ValidationException {
            if (this.profilePublicKey == null) {
                return UNVERIFIED;
            }
            return new RemoteChatSession(this.sessionId, ProfilePublicKey.createValidated(signatureValidator, gameProfile.getId(), this.profilePublicKey, duration));
        }

        @Nullable
        public ProfilePublicKey.Data profilePublicKey() {
            return this.profilePublicKey;
        }
    }
}

