/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.item.Item;

public class ClientboundCooldownPacket
implements Packet<ClientGamePacketListener> {
    private final Item item;
    private final int duration;

    public ClientboundCooldownPacket(Item item, int i) {
        this.item = item;
        this.duration = i;
    }

    public ClientboundCooldownPacket(FriendlyByteBuf friendlyByteBuf) {
        this.item = friendlyByteBuf.readById(BuiltInRegistries.ITEM);
        this.duration = friendlyByteBuf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeId(BuiltInRegistries.ITEM, this.item);
        friendlyByteBuf.writeVarInt(this.duration);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleItemCooldown(this);
    }

    public Item getItem() {
        return this.item;
    }

    public int getDuration() {
        return this.duration;
    }
}

