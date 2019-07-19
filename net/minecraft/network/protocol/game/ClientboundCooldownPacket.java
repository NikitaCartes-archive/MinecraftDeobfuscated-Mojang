/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.item.Item;

public class ClientboundCooldownPacket
implements Packet<ClientGamePacketListener> {
    private Item item;
    private int duration;

    public ClientboundCooldownPacket() {
    }

    public ClientboundCooldownPacket(Item item, int i) {
        this.item = item;
        this.duration = i;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.item = Item.byId(friendlyByteBuf.readVarInt());
        this.duration = friendlyByteBuf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeVarInt(Item.getId(this.item));
        friendlyByteBuf.writeVarInt(this.duration);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleItemCooldown(this);
    }

    @Environment(value=EnvType.CLIENT)
    public Item getItem() {
        return this.item;
    }

    @Environment(value=EnvType.CLIENT)
    public int getDuration() {
        return this.duration;
    }
}

