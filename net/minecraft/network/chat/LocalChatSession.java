/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import java.util.UUID;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.util.Signer;
import net.minecraft.world.entity.player.ProfileKeyPair;

public record LocalChatSession(UUID sessionId, ProfileKeyPair keyPair) {
    public static LocalChatSession create(ProfileKeyPair profileKeyPair) {
        return new LocalChatSession(UUID.randomUUID(), profileKeyPair);
    }

    public SignedMessageChain.Encoder createMessageEncoder(UUID uUID) {
        return new SignedMessageChain(uUID, this.sessionId).encoder(Signer.from(this.keyPair.privateKey(), "SHA256withRSA"));
    }

    public RemoteChatSession asRemote() {
        return new RemoteChatSession(this.sessionId, this.keyPair.publicKey());
    }
}

