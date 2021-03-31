/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.InteractionHand;

public class ClientboundOpenBookPacket
implements Packet<ClientGamePacketListener> {
    private final InteractionHand hand;

    public ClientboundOpenBookPacket(InteractionHand interactionHand) {
        this.hand = interactionHand;
    }

    public ClientboundOpenBookPacket(FriendlyByteBuf friendlyByteBuf) {
        this.hand = friendlyByteBuf.readEnum(InteractionHand.class);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeEnum(this.hand);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleOpenBook(this);
    }

    public InteractionHand getHand() {
        return this.hand;
    }
}

