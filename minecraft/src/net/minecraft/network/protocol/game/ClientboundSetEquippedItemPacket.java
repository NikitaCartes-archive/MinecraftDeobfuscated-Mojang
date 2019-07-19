package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ClientboundSetEquippedItemPacket implements Packet<ClientGamePacketListener> {
	private int entity;
	private EquipmentSlot slot;
	private ItemStack itemStack = ItemStack.EMPTY;

	public ClientboundSetEquippedItemPacket() {
	}

	public ClientboundSetEquippedItemPacket(int i, EquipmentSlot equipmentSlot, ItemStack itemStack) {
		this.entity = i;
		this.slot = equipmentSlot;
		this.itemStack = itemStack.copy();
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.entity = friendlyByteBuf.readVarInt();
		this.slot = friendlyByteBuf.readEnum(EquipmentSlot.class);
		this.itemStack = friendlyByteBuf.readItem();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.entity);
		friendlyByteBuf.writeEnum(this.slot);
		friendlyByteBuf.writeItem(this.itemStack);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetEquippedItem(this);
	}

	@Environment(EnvType.CLIENT)
	public ItemStack getItem() {
		return this.itemStack;
	}

	@Environment(EnvType.CLIENT)
	public int getEntity() {
		return this.entity;
	}

	@Environment(EnvType.CLIENT)
	public EquipmentSlot getSlot() {
		return this.slot;
	}
}
