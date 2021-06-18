/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public class ServerboundContainerClickPacket
implements Packet<ServerGamePacketListener> {
    private final int containerId;
    private final int stateId;
    private final int slotNum;
    private final int buttonNum;
    private final ClickType clickType;
    private final ItemStack carriedItem;
    private final Int2ObjectMap<ItemStack> changedSlots;

    public ServerboundContainerClickPacket(int i, int j, int k, int l, ClickType clickType, ItemStack itemStack, Int2ObjectMap<ItemStack> int2ObjectMap) {
        this.containerId = i;
        this.stateId = j;
        this.slotNum = k;
        this.buttonNum = l;
        this.clickType = clickType;
        this.carriedItem = itemStack;
        this.changedSlots = Int2ObjectMaps.unmodifiable(int2ObjectMap);
    }

    public ServerboundContainerClickPacket(FriendlyByteBuf friendlyByteBuf2) {
        this.containerId = friendlyByteBuf2.readByte();
        this.stateId = friendlyByteBuf2.readVarInt();
        this.slotNum = friendlyByteBuf2.readShort();
        this.buttonNum = friendlyByteBuf2.readByte();
        this.clickType = friendlyByteBuf2.readEnum(ClickType.class);
        this.changedSlots = Int2ObjectMaps.unmodifiable(friendlyByteBuf2.readMap(Int2ObjectOpenHashMap::new, friendlyByteBuf -> friendlyByteBuf.readShort(), FriendlyByteBuf::readItem));
        this.carriedItem = friendlyByteBuf2.readItem();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByte(this.containerId);
        friendlyByteBuf.writeVarInt(this.stateId);
        friendlyByteBuf.writeShort(this.slotNum);
        friendlyByteBuf.writeByte(this.buttonNum);
        friendlyByteBuf.writeEnum(this.clickType);
        friendlyByteBuf.writeMap(this.changedSlots, FriendlyByteBuf::writeShort, FriendlyByteBuf::writeItem);
        friendlyByteBuf.writeItem(this.carriedItem);
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleContainerClick(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getSlotNum() {
        return this.slotNum;
    }

    public int getButtonNum() {
        return this.buttonNum;
    }

    public ItemStack getCarriedItem() {
        return this.carriedItem;
    }

    public Int2ObjectMap<ItemStack> getChangedSlots() {
        return this.changedSlots;
    }

    public ClickType getClickType() {
        return this.clickType;
    }

    public int getStateId() {
        return this.stateId;
    }
}

