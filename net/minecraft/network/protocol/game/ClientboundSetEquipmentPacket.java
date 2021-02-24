/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ClientboundSetEquipmentPacket
implements Packet<ClientGamePacketListener> {
    private final int entity;
    private final List<Pair<EquipmentSlot, ItemStack>> slots;

    public ClientboundSetEquipmentPacket(int i, List<Pair<EquipmentSlot, ItemStack>> list) {
        this.entity = i;
        this.slots = list;
    }

    public ClientboundSetEquipmentPacket(FriendlyByteBuf friendlyByteBuf) {
        byte i;
        this.entity = friendlyByteBuf.readVarInt();
        EquipmentSlot[] equipmentSlots = EquipmentSlot.values();
        this.slots = Lists.newArrayList();
        do {
            i = friendlyByteBuf.readByte();
            EquipmentSlot equipmentSlot = equipmentSlots[i & 0x7F];
            ItemStack itemStack = friendlyByteBuf.readItem();
            this.slots.add(Pair.of(equipmentSlot, itemStack));
        } while ((i & 0xFFFFFF80) != 0);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.entity);
        int i = this.slots.size();
        for (int j = 0; j < i; ++j) {
            Pair<EquipmentSlot, ItemStack> pair = this.slots.get(j);
            EquipmentSlot equipmentSlot = pair.getFirst();
            boolean bl = j != i - 1;
            int k = equipmentSlot.ordinal();
            friendlyByteBuf.writeByte(bl ? k | 0xFFFFFF80 : k);
            friendlyByteBuf.writeItem(pair.getSecond());
        }
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetEquipment(this);
    }

    @Environment(value=EnvType.CLIENT)
    public int getEntity() {
        return this.entity;
    }

    @Environment(value=EnvType.CLIENT)
    public List<Pair<EquipmentSlot, ItemStack>> getSlots() {
        return this.slots;
    }
}

