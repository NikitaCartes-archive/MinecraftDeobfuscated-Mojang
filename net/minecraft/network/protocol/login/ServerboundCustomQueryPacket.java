/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import org.jetbrains.annotations.Nullable;

public class ServerboundCustomQueryPacket
implements Packet<ServerLoginPacketListener> {
    private static final int MAX_PAYLOAD_SIZE = 0x100000;
    private final int transactionId;
    @Nullable
    private final FriendlyByteBuf data;

    public ServerboundCustomQueryPacket(int i, @Nullable FriendlyByteBuf friendlyByteBuf) {
        this.transactionId = i;
        this.data = friendlyByteBuf;
    }

    public ServerboundCustomQueryPacket(FriendlyByteBuf friendlyByteBuf2) {
        this.transactionId = friendlyByteBuf2.readVarInt();
        this.data = (FriendlyByteBuf)friendlyByteBuf2.readNullable(friendlyByteBuf -> {
            int i = friendlyByteBuf.readableBytes();
            if (i < 0 || i > 0x100000) {
                throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
            }
            return new FriendlyByteBuf(friendlyByteBuf.readBytes(i));
        });
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf3) {
        friendlyByteBuf3.writeVarInt(this.transactionId);
        friendlyByteBuf3.writeNullable(this.data, (friendlyByteBuf, friendlyByteBuf2) -> friendlyByteBuf.writeBytes(friendlyByteBuf2.slice()));
    }

    @Override
    public void handle(ServerLoginPacketListener serverLoginPacketListener) {
        serverLoginPacketListener.handleCustomQueryPacket(this);
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    @Nullable
    public FriendlyByteBuf getData() {
        return this.data;
    }
}

