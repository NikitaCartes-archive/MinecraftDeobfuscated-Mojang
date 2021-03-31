package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderWarningDistancePacket implements Packet<ClientGamePacketListener> {
	private final int warningBlocks;

	public ClientboundSetBorderWarningDistancePacket(WorldBorder worldBorder) {
		this.warningBlocks = worldBorder.getWarningBlocks();
	}

	public ClientboundSetBorderWarningDistancePacket(FriendlyByteBuf friendlyByteBuf) {
		this.warningBlocks = friendlyByteBuf.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.warningBlocks);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetBorderWarningDistance(this);
	}

	public int getWarningBlocks() {
		return this.warningBlocks;
	}
}
