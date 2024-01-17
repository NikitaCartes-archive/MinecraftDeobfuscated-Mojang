package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.ItemStack;

public class ClientboundContainerSetContentPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundContainerSetContentPacket> STREAM_CODEC = Packet.codec(
		ClientboundContainerSetContentPacket::write, ClientboundContainerSetContentPacket::new
	);
	private final int containerId;
	private final int stateId;
	private final List<ItemStack> items;
	private final ItemStack carriedItem;

	public ClientboundContainerSetContentPacket(int i, int j, NonNullList<ItemStack> nonNullList, ItemStack itemStack) {
		this.containerId = i;
		this.stateId = j;
		this.items = NonNullList.<ItemStack>withSize(nonNullList.size(), ItemStack.EMPTY);

		for (int k = 0; k < nonNullList.size(); k++) {
			this.items.set(k, nonNullList.get(k).copy());
		}

		this.carriedItem = itemStack.copy();
	}

	private ClientboundContainerSetContentPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		this.containerId = registryFriendlyByteBuf.readUnsignedByte();
		this.stateId = registryFriendlyByteBuf.readVarInt();
		this.items = ItemStack.LIST_STREAM_CODEC.decode(registryFriendlyByteBuf);
		this.carriedItem = ItemStack.STREAM_CODEC.decode(registryFriendlyByteBuf);
	}

	private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		registryFriendlyByteBuf.writeByte(this.containerId);
		registryFriendlyByteBuf.writeVarInt(this.stateId);
		ItemStack.LIST_STREAM_CODEC.encode(registryFriendlyByteBuf, this.items);
		ItemStack.STREAM_CODEC.encode(registryFriendlyByteBuf, this.carriedItem);
	}

	@Override
	public PacketType<ClientboundContainerSetContentPacket> type() {
		return GamePacketTypes.CLIENTBOUND_CONTAINER_SET_CONTENT;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleContainerContent(this);
	}

	public int getContainerId() {
		return this.containerId;
	}

	public List<ItemStack> getItems() {
		return this.items;
	}

	public ItemStack getCarriedItem() {
		return this.carriedItem;
	}

	public int getStateId() {
		return this.stateId;
	}
}
