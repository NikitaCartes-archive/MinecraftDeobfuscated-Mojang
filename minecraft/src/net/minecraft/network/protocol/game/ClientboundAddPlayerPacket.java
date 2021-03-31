package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;

public class ClientboundAddPlayerPacket implements Packet<ClientGamePacketListener> {
	private final int entityId;
	private final UUID playerId;
	private final double x;
	private final double y;
	private final double z;
	private final byte yRot;
	private final byte xRot;

	public ClientboundAddPlayerPacket(Player player) {
		this.entityId = player.getId();
		this.playerId = player.getGameProfile().getId();
		this.x = player.getX();
		this.y = player.getY();
		this.z = player.getZ();
		this.yRot = (byte)((int)(player.yRot * 256.0F / 360.0F));
		this.xRot = (byte)((int)(player.xRot * 256.0F / 360.0F));
	}

	public ClientboundAddPlayerPacket(FriendlyByteBuf friendlyByteBuf) {
		this.entityId = friendlyByteBuf.readVarInt();
		this.playerId = friendlyByteBuf.readUUID();
		this.x = friendlyByteBuf.readDouble();
		this.y = friendlyByteBuf.readDouble();
		this.z = friendlyByteBuf.readDouble();
		this.yRot = friendlyByteBuf.readByte();
		this.xRot = friendlyByteBuf.readByte();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.entityId);
		friendlyByteBuf.writeUUID(this.playerId);
		friendlyByteBuf.writeDouble(this.x);
		friendlyByteBuf.writeDouble(this.y);
		friendlyByteBuf.writeDouble(this.z);
		friendlyByteBuf.writeByte(this.yRot);
		friendlyByteBuf.writeByte(this.xRot);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAddPlayer(this);
	}

	public int getEntityId() {
		return this.entityId;
	}

	public UUID getPlayerId() {
		return this.playerId;
	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public double getZ() {
		return this.z;
	}

	public byte getyRot() {
		return this.yRot;
	}

	public byte getxRot() {
		return this.xRot;
	}
}
