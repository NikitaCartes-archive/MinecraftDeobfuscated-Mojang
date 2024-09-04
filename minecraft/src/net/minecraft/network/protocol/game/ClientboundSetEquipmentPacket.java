package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ClientboundSetEquipmentPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSetEquipmentPacket> STREAM_CODEC = Packet.codec(
		ClientboundSetEquipmentPacket::write, ClientboundSetEquipmentPacket::new
	);
	private static final byte CONTINUE_MASK = -128;
	private final int entity;
	private final List<Pair<EquipmentSlot, ItemStack>> slots;

	public ClientboundSetEquipmentPacket(int i, List<Pair<EquipmentSlot, ItemStack>> list) {
		this.entity = i;
		this.slots = list;
	}

	private ClientboundSetEquipmentPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		this.entity = registryFriendlyByteBuf.readVarInt();
		this.slots = Lists.<Pair<EquipmentSlot, ItemStack>>newArrayList();

		int i;
		do {
			i = registryFriendlyByteBuf.readByte();
			EquipmentSlot equipmentSlot = (EquipmentSlot)EquipmentSlot.VALUES.get(i & 127);
			ItemStack itemStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(registryFriendlyByteBuf);
			this.slots.add(Pair.of(equipmentSlot, itemStack));
		} while ((i & -128) != 0);
	}

	private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		registryFriendlyByteBuf.writeVarInt(this.entity);
		int i = this.slots.size();

		for (int j = 0; j < i; j++) {
			Pair<EquipmentSlot, ItemStack> pair = (Pair<EquipmentSlot, ItemStack>)this.slots.get(j);
			EquipmentSlot equipmentSlot = pair.getFirst();
			boolean bl = j != i - 1;
			int k = equipmentSlot.ordinal();
			registryFriendlyByteBuf.writeByte(bl ? k | -128 : k);
			ItemStack.OPTIONAL_STREAM_CODEC.encode(registryFriendlyByteBuf, pair.getSecond());
		}
	}

	@Override
	public PacketType<ClientboundSetEquipmentPacket> type() {
		return GamePacketTypes.CLIENTBOUND_SET_EQUIPMENT;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetEquipment(this);
	}

	public int getEntity() {
		return this.entity;
	}

	public List<Pair<EquipmentSlot, ItemStack>> getSlots() {
		return this.slots;
	}
}
