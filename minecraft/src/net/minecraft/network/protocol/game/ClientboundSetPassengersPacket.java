package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;

public class ClientboundSetPassengersPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundSetPassengersPacket> STREAM_CODEC = Packet.codec(
		ClientboundSetPassengersPacket::write, ClientboundSetPassengersPacket::new
	);
	private final int vehicle;
	private final int[] passengers;

	public ClientboundSetPassengersPacket(Entity entity) {
		this.vehicle = entity.getId();
		List<Entity> list = entity.getPassengers();
		this.passengers = new int[list.size()];

		for (int i = 0; i < list.size(); i++) {
			this.passengers[i] = ((Entity)list.get(i)).getId();
		}
	}

	private ClientboundSetPassengersPacket(FriendlyByteBuf friendlyByteBuf) {
		this.vehicle = friendlyByteBuf.readVarInt();
		this.passengers = friendlyByteBuf.readVarIntArray();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.vehicle);
		friendlyByteBuf.writeVarIntArray(this.passengers);
	}

	@Override
	public PacketType<ClientboundSetPassengersPacket> type() {
		return GamePacketTypes.CLIENTBOUND_SET_PASSENGERS;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetEntityPassengersPacket(this);
	}

	public int[] getPassengers() {
		return this.passengers;
	}

	public int getVehicle() {
		return this.vehicle;
	}
}
