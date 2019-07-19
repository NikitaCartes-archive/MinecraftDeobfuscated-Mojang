package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ClientboundSetPassengersPacket implements Packet<ClientGamePacketListener> {
	private int vehicle;
	private int[] passengers;

	public ClientboundSetPassengersPacket() {
	}

	public ClientboundSetPassengersPacket(Entity entity) {
		this.vehicle = entity.getId();
		List<Entity> list = entity.getPassengers();
		this.passengers = new int[list.size()];

		for (int i = 0; i < list.size(); i++) {
			this.passengers[i] = ((Entity)list.get(i)).getId();
		}
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.vehicle = friendlyByteBuf.readVarInt();
		this.passengers = friendlyByteBuf.readVarIntArray();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.vehicle);
		friendlyByteBuf.writeVarIntArray(this.passengers);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetEntityPassengersPacket(this);
	}

	@Environment(EnvType.CLIENT)
	public int[] getPassengers() {
		return this.passengers;
	}

	@Environment(EnvType.CLIENT)
	public int getVehicle() {
		return this.vehicle;
	}
}
