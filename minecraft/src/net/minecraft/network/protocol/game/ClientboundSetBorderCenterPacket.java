package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderCenterPacket implements Packet<ClientGamePacketListener> {
	private final double newCenterX;
	private final double newCenterZ;

	public ClientboundSetBorderCenterPacket(WorldBorder worldBorder) {
		this.newCenterX = worldBorder.getCenterX();
		this.newCenterZ = worldBorder.getCenterZ();
	}

	public ClientboundSetBorderCenterPacket(FriendlyByteBuf friendlyByteBuf) {
		this.newCenterX = friendlyByteBuf.readDouble();
		this.newCenterZ = friendlyByteBuf.readDouble();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeDouble(this.newCenterX);
		friendlyByteBuf.writeDouble(this.newCenterZ);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetBorderCenter(this);
	}

	@Environment(EnvType.CLIENT)
	public double getNewCenterZ() {
		return this.newCenterZ;
	}

	@Environment(EnvType.CLIENT)
	public double getNewCenterX() {
		return this.newCenterX;
	}
}
