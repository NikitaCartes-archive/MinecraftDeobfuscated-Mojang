/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;

public class ServerboundCustomPayloadPacket
implements Packet<ServerGamePacketListener> {
    private static final int MAX_PAYLOAD_SIZE = Short.MAX_VALUE;
    public static final ResourceLocation BRAND = new ResourceLocation("brand");
    private final ResourceLocation identifier;
    private final FriendlyByteBuf data;

    public ServerboundCustomPayloadPacket(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
        this.identifier = resourceLocation;
        this.data = friendlyByteBuf;
    }

    public ServerboundCustomPayloadPacket(FriendlyByteBuf friendlyByteBuf) {
        this.identifier = friendlyByteBuf.readResourceLocation();
        int i = friendlyByteBuf.readableBytes();
        if (i < 0 || i > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Payload may not be larger than 32767 bytes");
        }
        this.data = new FriendlyByteBuf(friendlyByteBuf.readBytes(i));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeResourceLocation(this.identifier);
        friendlyByteBuf.writeBytes(this.data);
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleCustomPayload(this);
        this.data.release();
    }

    public ResourceLocation getIdentifier() {
        return this.identifier;
    }

    public FriendlyByteBuf getData() {
        return this.data;
    }
}

