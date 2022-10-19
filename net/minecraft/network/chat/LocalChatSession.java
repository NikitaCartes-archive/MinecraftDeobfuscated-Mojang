/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.util.Signer;
import net.minecraft.world.entity.player.ProfileKeyPair;
import org.jetbrains.annotations.Nullable;

public record LocalChatSession(UUID sessionId, @Nullable ProfileKeyPair keyPair) {
    public static LocalChatSession create(@Nullable ProfileKeyPair profileKeyPair) {
        return new LocalChatSession(UUID.randomUUID(), profileKeyPair);
    }

    public SignedMessageChain.Encoder createMessageEncoder(UUID uUID) {
        Signer signer = this.createSigner();
        if (signer != null) {
            return new SignedMessageChain(uUID, this.sessionId).encoder(signer);
        }
        return SignedMessageChain.Encoder.UNSIGNED;
    }

    @Nullable
    public Signer createSigner() {
        if (this.keyPair != null) {
            return Signer.from(this.keyPair.privateKey(), "SHA256withRSA");
        }
        return null;
    }

    public RemoteChatSession asRemote() {
        return new RemoteChatSession(this.sessionId, Util.mapNullable(this.keyPair, ProfileKeyPair::publicKey));
    }

    @Nullable
    public ProfileKeyPair keyPair() {
        return this.keyPair;
    }
}

