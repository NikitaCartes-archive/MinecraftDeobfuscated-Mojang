package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ClientboundSetEquipmentPacket implements Packet<ClientGamePacketListener> {
	private int entity;
	private final List<Pair<EquipmentSlot, ItemStack>> slots;

	public ClientboundSetEquipmentPacket() {
		this.slots = Lists.<Pair<EquipmentSlot, ItemStack>>newArrayList();
	}

	public ClientboundSetEquipmentPacket(int i, List<Pair<EquipmentSlot, ItemStack>> list) {
		this.entity = i;
		this.slots = list;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.entity = friendlyByteBuf.readVarInt();
		EquipmentSlot[] equipmentSlots = EquipmentSlot.values();

		int i;
		do {
			i = friendlyByteBuf.readByte();
			EquipmentSlot equipmentSlot = equipmentSlots[i & 127];
			ItemStack itemStack = friendlyByteBuf.readItem();
			this.slots.add(Pair.of(equipmentSlot, itemStack));
		} while ((i & -128) != 0);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.entity);
		int i = this.slots.size();

		for (int j = 0; j < i; j++) {
			Pair<EquipmentSlot, ItemStack> pair = (Pair<EquipmentSlot, ItemStack>)this.slots.get(j);
			EquipmentSlot equipmentSlot = pair.getFirst();
			boolean bl = j != i - 1;
			int k = equipmentSlot.ordinal();
			friendlyByteBuf.writeByte(bl ? k | -128 : k);
			friendlyByteBuf.writeItem(pair.getSecond());
		}
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetEquipment(this);
	}

	@Environment(EnvType.CLIENT)
	public int getEntity() {
		return this.entity;
	}

	@Environment(EnvType.CLIENT)
	public List<Pair<EquipmentSlot, ItemStack>> getSlots() {
		return this.slots;
	}
}
