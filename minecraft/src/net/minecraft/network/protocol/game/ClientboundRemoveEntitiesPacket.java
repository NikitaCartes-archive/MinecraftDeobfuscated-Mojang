package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundRemoveEntitiesPacket implements Packet<ClientGamePacketListener> {
	private int[] entityIds;

	public ClientboundRemoveEntitiesPacket() {
	}

	public ClientboundRemoveEntitiesPacket(int... is) {
		this.entityIds = is;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.entityIds = new int[friendlyByteBuf.readVarInt()];

		for (int i = 0; i < this.entityIds.length; i++) {
			this.entityIds[i] = friendlyByteBuf.readVarInt();
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.entityIds.length);

		for (int i : this.entityIds) {
			friendlyByteBuf.writeVarInt(i);
		}
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleRemoveEntity(this);
	}

	@Environment(EnvType.CLIENT)
	public int[] getEntityIds() {
		return this.entityIds;
	}
}
